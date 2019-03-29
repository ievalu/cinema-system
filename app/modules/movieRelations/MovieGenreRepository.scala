package modules.movieRelations

import javax.inject.{Inject, Singleton}
import modules.utility.database.ExtendedPostgresProfile
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MovieGenreRepository @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider
)(
    implicit ec: ExecutionContext
) extends MovieGenreComponent with HasDatabaseConfigProvider[ExtendedPostgresProfile] {
  import profile.api._

  private val logger = Logger(this.getClass)

  def findGenresByMovieId(movieId: Long): Future[Seq[Long]] = db.run {
    movieGenres
      .filter(movieGenre => movieGenre.movieId === movieId)
      .map(movieGenre => movieGenre.genreId)
      .result
  }
}
