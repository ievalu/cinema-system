package modules.movieRelations

import javax.inject.{Inject, Singleton}
import modules.utility.database.ExtendedPostgresProfile
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MovieDirectorRepository @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider
)(
    implicit ec: ExecutionContext
) extends MovieDirectorComponent with HasDatabaseConfigProvider[ExtendedPostgresProfile] {
  import profile.api._

  private val logger = Logger(this.getClass)

  def findDirectorsByMovieId(movieId: Long): Future[Seq[Long]] = db.run {
    movieDirectors
      .filter(movieDirector => movieDirector.movieId === movieId)
      .map(movieDirector => movieDirector.directorId)
      .result
  }
}
