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
}
