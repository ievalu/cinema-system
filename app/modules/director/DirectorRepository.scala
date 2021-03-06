package modules.director

import java.sql.Date

import javax.inject.{Inject, Singleton}
import modules.util.{Country, Gender, Page, SortOrder}
import modules.utility.database.ExtendedPostgresProfile
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.lifted.ColumnOrdered

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DirectorRepository @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(
    implicit ec: ExecutionContext
) extends DirectorTableComponent with HasDatabaseConfigProvider[ExtendedPostgresProfile]{
  import profile.api._

  private val logger = Logger(this.getClass)

  private def filterLogic(
      name: Option[String],
      birthDate: Option[Date],
      nationality: Country.Value,
      heightMin: Int,
      heightMax: Int,
      gender: Gender.Value
  ) = {
    val firstQuery = name.map {
      nameVal =>
        directors.filter(
          director =>
            nameVal.split(" ")
              .map { word =>
                director.firstName.ilike("%" + word + "%") || director.lastName.ilike("%" + word + "%")
              }
              .reduceLeftOption(_ || _)
              .getOrElse(true: Rep[Boolean])
        )
    }.getOrElse(directors)
      .filter(director => director.height >= heightMin && director.height <= heightMax)
    val dateFilteredQuery = birthDate match {
      case Some(date) => firstQuery.filter(director => director.birthDate === date)
      case None => firstQuery
    }
    val nationalityFilteredQuery = nationality match {
      case Country.NoCountry => dateFilteredQuery
      case _ => dateFilteredQuery.filter(director => director.nationality === nationality)
    }
    val genderFilteredQuery = gender match {
      case Gender.Other => nationalityFilteredQuery
      case _ => nationalityFilteredQuery.filter(director => director.gender === gender)
    }
    genderFilteredQuery
  }

  private def sortLogic(
      directorTable: DirectorTable,
      orderBy: SortableField.Value,
      order: SortOrder.Value
  ) = {
    val ordering = if (order == SortOrder.desc) slick.ast.Ordering.Desc else slick.ast.Ordering.Asc
    orderBy match {
      case SortableField.name => ColumnOrdered(directorTable.firstName.toLowerCase, slick.ast.Ordering(ordering))
      case SortableField.birthDate => ColumnOrdered(directorTable.birthDate, slick.ast.Ordering(ordering))
      case SortableField.nationality => ColumnOrdered({
        Country.values.drop(1)
          .foldLeft {
            Case
              .If(directorTable.nationality === Country.values.head)
              .Then(Some(Country.values.head.nationality): Rep[Option[String]])
          } {
            case(acc, enum) =>
              acc.If(directorTable.nationality === enum).Then(Some(enum.nationality): Rep[Option[String]])
          }
          .Else(Option.empty[String]: Rep[Option[String]])
      }, slick.ast.Ordering(ordering))
      case SortableField.height => ColumnOrdered(directorTable.height, slick.ast.Ordering(ordering))
      case _ => directorTable.id.asc
    }
  }

  def count(
      name: Option[String],
      birthDate: Option[Date],
      nationality: Country.Value,
      heightMin: Int,
      heightMax: Int,
      gender: Gender.Value
  ): Future[Int] = db.run(filterLogic(name, birthDate, nationality, heightMin, heightMax, gender).length.result)

  def list(
      page: Int = 1,
      pageSize: Int = 8,
      name: Option[String],
      birthDate: Option[Date],
      nationality: Country.Value,
      heightMin: Int,
      heightMax: Int,
      gender: Gender.Value,
      orderBy: SortableField.Value,
      order: SortOrder.Value
  ): Future[Page[Director]] = {
    val offset = (page - 1) * pageSize
    val filteredQuery = filterLogic(name, birthDate, nationality, heightMin, heightMax, gender)
    val sortedQuery = filteredQuery.sortBy{ d => sortLogic(d, orderBy, order) }
    for {
      totalRows <- count(name, birthDate, nationality, heightMin, heightMax, gender)
      directorList <- db.run(
        sortedQuery
          .drop(offset)
          .take(pageSize)
          .result
      )
    } yield Page(directorList, page, offset, totalRows)
  }

  def findById(id: Long): Future[Option[Director]] = db.run {
    directors.filter(d => d.id === id).result.headOption
  }

  def create(newDirector: CreateDirectorForm): Future[Director] = db.run {
    (directors.map(d => (
      d.firstName,
      d.lastName,
      d.birthDate,
      d.nationality,
      d.height,
      d.gender
    )) returning directors
      into ((_, director) =>
        director)) +=
        (
          newDirector.firstName,
          newDirector.lastName,
          newDirector.birthDate,
          newDirector.nationality,
          newDirector.height,
          newDirector.gender)
  }

  def delete(id: Long): Future[Int] = db.run {
    directors.filter(d => d.id === id).delete
  }

  def update(id: Long, newDirector: CreateDirectorForm): Future[Director] = {
    db.run {
      (directors returning directors)
        .insertOrUpdate(
          Director(
            id,
            newDirector.firstName,
            newDirector.lastName,
            newDirector.birthDate,
            newDirector.nationality,
            newDirector.height,
            newDirector.gender)
        )
    }.map {
      case Some(director) =>
        logger.debug(s"Created new director $director")
        director
      case None =>
        logger.debug(s"Updated existing director $newDirector")
        Director(
          id,
          newDirector.firstName,
          newDirector.lastName,
          newDirector.birthDate,
          newDirector.nationality,
          newDirector.height,
          newDirector.gender)
    }
  }
}
