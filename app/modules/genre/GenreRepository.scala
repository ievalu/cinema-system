package modules.genre

import javax.inject.{Inject, Singleton}
import modules.utility.database.ExtendedPostgresProfile
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GenreRepository @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
) (
    implicit ec: ExecutionContext
) extends GenreTableComponent with HasDatabaseConfigProvider[ExtendedPostgresProfile] {
  import profile.api._

  private val logger = Logger(this.getClass)

  def list(): Future[Seq[Genre]] = db.run {
    genres.result
  }

  def create(newGenre: CreateGenreForm): Future[Genre] = db.run {
    (genres.map(g => (g.title)) returning genres.map(_.id)
      into ((genreTitle, id) => Genre(id, genreTitle))) += (newGenre.title)
  }
}
