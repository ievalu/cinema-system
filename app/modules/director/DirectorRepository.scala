package modules.director

import java.sql.Date

import javax.inject.{Inject, Singleton}
import modules.util.Page
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
      heightMin: Int,
      heightMax: Int
  ) = {
    val nameArr = name.toLowerCase.split(" ")
    val firstQuery = directors
      .filter(
        director => nameArr.length match {
          case 1 => {
            (director.firstName.toLowerCase like nameArr(0)) ||
              (director.lastName.toLowerCase like nameArr(0))
          }
          case 2 => {
            (director.firstName.toLowerCase like nameArr(0)) ||
            (director.lastName.toLowerCase like nameArr(0)) ||
            (director.firstName.toLowerCase like nameArr(1)) ||
            (director.lastName.toLowerCase like nameArr(1))
          }
        }
      )
      .filter(director => director.height >= heightMin && director.height <= heightMax)
    val dateFilteredQuery = birthDate match {
      case Some(date) => firstQuery.filter(director => director.birthDate === date)
      case None => firstQuery
    }
    dateFilteredQuery
  }

  def count(
      name: String = "%",
      birthDate: Option[Date],
      heightMin: Int,
      heightMax: Int
  ): Future[Int] = db.run(filterLogic(name, birthDate, heightMin, heightMax).length.result)

  def list(
      page: Int = 1,
      pageSize: Int = 8,
      name: String = "%",
      birthDate: Option[Date],
      heightMin: Int,
      heightMax: Int
  ): Future[Page[Director]] = {
    val offset = (page - 1) * pageSize
    val filteredQuery = filterLogic(name, birthDate, heightMin, heightMax)
    for {
      totalRows <- count(name, birthDate, heightMin, heightMax)
      directorList <- db.run(
        filteredQuery
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
