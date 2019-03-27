package modules.genre

import javax.inject.{Inject, Singleton}
import modules.util.{Page, SortOrder}
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

  def filterLogic(title: String) = genres.filter(genre => genre.title ilike title)

  def sortLogic(
      genreTable: GenreTable,
      orderBy: SortableField.Value,
      order: SortOrder.Value
  ) = {
    (orderBy, order) match {
      case (SortableField.title, SortOrder.asc) => genreTable.title.toLowerCase.asc
      case (SortableField.title, SortOrder.desc) => genreTable.title.toLowerCase.desc
      case _ => genreTable.id.asc
    }
  }

  def count(title: String): Future[Int] = db.run(filterLogic(title).length.result)

  def list(
      page: Int = 1,
      pageSize: Int = 8,
      title: String = "%",
      orderBy: SortableField.Value,
      order: SortOrder.Value
  ): Future[Page[Genre]] = {
    val offset = (page - 1) * pageSize
    val filteredQuery = filterLogic(title)
    val sortedQuery = filteredQuery.sortBy{ g => sortLogic(g, orderBy, order) }
    for {
      totalRows <- count(title)
      genreList <- db.run(
        sortedQuery
          .drop(offset)
          .take(pageSize)
          .result
      )
    } yield Page(genreList, page, offset, totalRows)
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
