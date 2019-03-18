package modules.movie

import javax.inject.{Inject, Singleton}
import modules.movie.Country.CountryVal
import modules.movie.Language.LanguageVal
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

  implicit val countryColumnType: BaseColumnType[CountryVal] = MappedColumnType.base[CountryVal, String](
    { enum => Country.getName(enum) },
    { string => Country.findByString(string).getOrElse(Country.NoCountry) }
  )

  implicit val languageColumnType: BaseColumnType[LanguageVal] = MappedColumnType.base[LanguageVal, String](
    { enum => Language.getName(enum) },
    { string => Language.findByString(string).getOrElse(Language.NoLanguage) }
  )

  private val logger = Logger(this.getClass)

  def list(): Future[Seq[Movie]] = db.run {
    movies.result
  }

  def create(newMovie: CreateMovieForm) = db.run {
    (movies.map(m => (m.title, m.description, m.releaseDate, m.country, m.language)) returning movies.map(_.id)
      into ((movieForm, id) => Movie(id, movieForm._1, movieForm._2, movieForm._3, movieForm._4, movieForm._5))) +=
      (newMovie.title, newMovie.description, newMovie.releaseDate, newMovie.country, newMovie.language)
  }
}
