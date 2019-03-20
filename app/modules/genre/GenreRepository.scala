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

  def findById(id: Long): Future[Option[Genre]] = db.run {
    genres.filter(g => g.id === id).result.headOption
  }

  def create(newGenre: CreateGenreForm): Future[Genre] = db.run {
    (genres.map(g => (g.title)) returning genres.map(_.id)
      into ((genreTitle, id) => Genre(id, genreTitle))) += (newGenre.title)
  }

  def update(id: Long, newGenre: CreateGenreForm): Future[Genre] = {
    db.run {
      (genres returning genres)
        .insertOrUpdate(
          Genre(id, newGenre.title)
        )
    }.map {
      case Some(genre) =>
        logger.debug(s"Created new genre $genre")
        genre
      case None =>
        logger.debug(s"Updated existing genre $newGenre")
        Genre(id, newGenre.title)
    }
  }

  def delete(id: Long): Future[Int] = db.run {
    genres.filter(g => g.id === id).delete
  }
}
