package modules.movie

import java.sql.Date

import javax.inject.{Inject, Singleton}
import modules.util.{Page}
import modules.utility.database.ExtendedPostgresProfile
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MovieRepository @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider
)(
    implicit ec: ExecutionContext
) extends MovieTableComponent with HasDatabaseConfigProvider[ExtendedPostgresProfile]{
  import profile.api._

  private val logger = Logger(this.getClass)

  def filterLogic(
      title: String = "%",
      description: String = "%",
      releaseDate: Option[Date]
  ) = {
    val firstQuery = movies
      .filter(movie => movie.title.toLowerCase like title.toLowerCase)
      .filter(movie => movie.description.toLowerCase like description.toLowerCase)
    val dateFilteredQuery = releaseDate match {
      case Some(date) => firstQuery.filter(movie => movie.releaseDate === date)
      case None => firstQuery
    }
    dateFilteredQuery
  }

  def count(
      title: String = "%",
      description: String = "%",
      releaseDate: Option[Date]
  ): Future[Int] = db.run(filterLogic(title, description, releaseDate).length.result)

  def list(
      page: Int = 1,
      pageSize: Int = 8,
      title: String = "%",
      description: String = "%",
      releaseDate: Option[Date]
  ): Future[Page[Movie]] = {
    val offset = (page - 1) * pageSize
    val filteredQuery = filterLogic(title, description, releaseDate)
    for {
      totalRows <- count(title, description, releaseDate)
      movieList <- db.run {
        filteredQuery
          .drop(offset)
          .take(pageSize)
          .result
      }
    } yield Page(movieList, page, offset, totalRows)
  }

  def findById(id: Long): Future[Option[Movie]] = db.run {
    movies.filter(m => m.id === id).result.headOption
  }

  def create(newMovie: CreateMovieForm): Future[Movie] = db.run {
    (movies.map(m => (m.title, m.description, m.releaseDate, m.country, m.language)) returning movies.map(_.id)
      into ((movieForm, id) => Movie(id, movieForm._1, movieForm._2, movieForm._3, movieForm._4, movieForm._5))) +=
      (newMovie.title, newMovie.description, newMovie.releaseDate, newMovie.country, newMovie.language)
  }

  def update(id: Long, newMovie: CreateMovieForm): Future[Movie] = {
    db.run {
      (movies returning movies)
        .insertOrUpdate(
          Movie(id, newMovie.title, newMovie.description, newMovie.releaseDate, newMovie.country, newMovie.language)
        )
    }.map {
      case Some(movie) =>
        logger.debug(s"Created new movie $movie")
        movie
      case None =>
        logger.debug(s"Updated existing movie $newMovie")
        Movie(id, newMovie.title, newMovie.description, newMovie.releaseDate, newMovie.country, newMovie.language)
    }
  }

  def delete(id: Long): Future[Int] = db.run {
    movies.filter(m => m.id === id).delete
  }
}
