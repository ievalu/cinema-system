package modules.director

import javax.inject.{Inject, Singleton}
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

  def list(): Future[Seq[Director]] = db.run {
    directors.result
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
