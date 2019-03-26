package modules.movie

import java.sql.Date

import javax.inject.{Inject, Singleton}
import modules.util.Country.CountryVal
import modules.util.Language.LanguageVal
import modules.util.{Country, Language, Page, SortOrder}
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
      releaseDate: Option[Date],
      country: CountryVal,
      language: LanguageVal
  ) = {
    val firstQuery = movies
      .filter(movie => movie.title.toLowerCase like title.toLowerCase)
      .filter(movie => movie.description.toLowerCase like description.toLowerCase)
    val dateFilteredQuery = releaseDate match {
      case Some(date) => firstQuery.filter(movie => movie.releaseDate === date)
      case None => firstQuery
    }
    val countryFilteredQuery = country match {
      case Country.NoCountry => dateFilteredQuery
      case _ => dateFilteredQuery.filter(movie => movie.country === country)
    }
    val languageFilteredQuery = language match {
      case Language.NoLanguage => countryFilteredQuery
      case _ => countryFilteredQuery.filter(movie => movie.language === language)
    }
    languageFilteredQuery
  }

  def sortLogic(
      movieTable: MovieTable,
      orderBy: SortableField.Value,
      order: SortOrder.Value
  ) = {
    (orderBy, order) match {
      case (SortableField.title, SortOrder.asc) => movieTable.title.toLowerCase.asc
      case (SortableField.title, SortOrder.desc) => movieTable.title.toLowerCase.desc
      case (SortableField.description, SortOrder.asc) => movieTable.description.toLowerCase.asc
      case (SortableField.description, SortOrder.desc) => movieTable.description.toLowerCase.desc
      case (SortableField.releaseDate, SortOrder.asc) => movieTable.releaseDate.asc
      case (SortableField.releaseDate, SortOrder.desc) => movieTable.releaseDate.desc
      case (SortableField.country, SortOrder.asc) => movieTable.country.asc
      case (SortableField.country, SortOrder.desc) => movieTable.country.desc
      case (SortableField.language, SortOrder.asc) => movieTable.language.asc
      case (SortableField.language, SortOrder.desc) => movieTable.language.desc
      case _ => movieTable.id.asc
    }
  }

  def count(
      title: String = "%",
      description: String = "%",
      releaseDate: Option[Date],
      country: CountryVal,
      language: LanguageVal
  ): Future[Int] = db.run(filterLogic(title, description, releaseDate, country, language).length.result)

  def list(
      page: Int = 1,
      pageSize: Int = 8,
      title: String = "%",
      description: String = "%",
      releaseDate: Option[Date],
      country: CountryVal,
      language: LanguageVal,
      orderBy: SortableField.Value,
      order: SortOrder.Value
  ): Future[Page[Movie]] = {
    val offset = (page - 1) * pageSize
    val filteredQuery = filterLogic(title, description, releaseDate, country, language)
    val sortedQuery = filteredQuery.sortBy{ m => sortLogic(m, orderBy, order) }
    for {
      totalRows <- count(title, description, releaseDate, country, language)
      movieList <- db.run {
        sortedQuery
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
