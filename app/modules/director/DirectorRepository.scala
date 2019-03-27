package modules.director

import java.sql.Date

import javax.inject.{Inject, Singleton}
import modules.util.Country.CountryVal
import modules.util.Gender.GenderVal
import modules.util.{Country, Gender, Page, SortOrder}
import modules.utility.database.ExtendedPostgresProfile
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DirectorRepository @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(
    implicit ec: ExecutionContext
) extends DirectorTableComponent with HasDatabaseConfigProvider[ExtendedPostgresProfile]{
  import profile.api._

  private val logger = Logger(this.getClass)

  def filterLogic(
      name: String = "%",
      birthDate: Option[Date],
      nationality: CountryVal,
      heightMin: Int,
      heightMax: Int,
      gender: GenderVal
  ) = {
    val firstQuery = directors
      .filter(
        director =>
          name.trim().split(" ")
            .map { word =>
              director.firstName.ilike(word) || director.lastName.ilike(word)
            }
            .reduceLeftOption(_ || _)
            .getOrElse(true: Rep[Boolean])
      )
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

  def sortLogic(
      directorTable: DirectorTable,
      orderBy: SortableField.Value,
      order: SortOrder.Value
  ) = {
    (orderBy, order) match {
      case (SortableField.name, SortOrder.asc) => directorTable.firstName.toLowerCase.asc
      case (SortableField.name, SortOrder.desc) => directorTable.firstName.toLowerCase.desc
      case (SortableField.birthDate, SortOrder.asc) => directorTable.birthDate.asc
      case (SortableField.birthDate, SortOrder.desc) => directorTable.birthDate.desc
      case (SortableField.nationality, SortOrder.asc) => {
        Country.seqValues.drop(1)
          .foldLeft {
            Case
              .If(directorTable.nationality === Country.seqValues.head)
              .Then(Some(Country.seqValues.head.nationality): Rep[Option[String]])
          } {
            case(acc, enum) =>
              acc.If(directorTable.nationality === enum).Then(Some(enum.nationality): Rep[Option[String]])
          }
          .Else(Option.empty[String]: Rep[Option[String]])
          .asc
      }
      case (SortableField.nationality, SortOrder.desc) => {
        Country.seqValues.drop(1)
          .foldLeft {
            Case
              .If(directorTable.nationality === Country.seqValues.head)
              .Then(Some(Country.seqValues.head.nationality): Rep[Option[String]])
          } {
            case(acc, enum) =>
              acc.If(directorTable.nationality === enum).Then(Some(enum.nationality): Rep[Option[String]])
          }
          .Else(Option.empty[String]: Rep[Option[String]])
          .desc
      }
      case (SortableField.height, SortOrder.asc) => directorTable.height.asc
      case (SortableField.height, SortOrder.desc) => directorTable.height.desc
      case _ => directorTable.id.asc
    }
  }

  def count(
      name: String = "%",
      birthDate: Option[Date],
      nationality: CountryVal,
      heightMin: Int,
      heightMax: Int,
      gender: GenderVal
  ): Future[Int] = db.run(filterLogic(name, birthDate, nationality, heightMin, heightMax, gender).length.result)

  def list(
      page: Int = 1,
      pageSize: Int = 8,
      name: String = "%",
      birthDate: Option[Date],
      nationality: CountryVal,
      heightMin: Int,
      heightMax: Int,
      gender: GenderVal,
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
    )) returning directors.map(_.id)
      into ((directorForm, id) =>
        Director(
          id,
          directorForm._1,
          directorForm._2,
          directorForm._3,
          directorForm._4,
          directorForm._5,
          directorForm._6
        ))) +=
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
