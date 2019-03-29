package modules.movieRelations

import javax.inject.{Inject, Singleton}
import modules.utility.database.ExtendedPostgresProfile
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MovieActorRepository @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider
)(
    implicit ec: ExecutionContext
) extends MovieActorComponent with HasDatabaseConfigProvider[ExtendedPostgresProfile] {
  import profile.api._

  private val logger = Logger(this.getClass)

  def findActorsByMovieId(movieId: Long): Future[Seq[Long]] = db.run {
    movieActors
      .filter(movieActor => movieActor.movieId === movieId)
      .map(movieActor => movieActor.actorId)
      .result
  }
}
