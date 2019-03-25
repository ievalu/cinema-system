package modules.movie

import java.sql.Date

import modules.util.{Country, Language}
import modules.util.Country.CountryVal
import modules.util.Language.LanguageVal
import modules.utility.database.ExtendedPostgresProfile
import play.api.db.slick.HasDatabaseConfig

trait MovieTableComponent {  self: HasDatabaseConfig[ExtendedPostgresProfile] =>
  import profile.api._

  implicit val countryColumnType: BaseColumnType[CountryVal] = MappedColumnType.base[CountryVal, String](
    { enum => enum.dbName },
    { string => Country.findByString(string).getOrElse(Country.NoCountry) }
  )

  implicit val languageColumnType: BaseColumnType[LanguageVal] = MappedColumnType.base[LanguageVal, String](
    { enum => enum.dbName },
    { string => Language.findByString(string).getOrElse(Language.NoLanguage) }
  )

  class MovieTable(tag: Tag) extends Table[Movie](tag, "movie") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def description = column[String]("description")
    def releaseDate = column[Date]("release-date")
    def country = column[CountryVal]("country")
    def language = column[LanguageVal]("language")

    def * = (id, title, description, releaseDate, country, language) <> ((Movie.apply _).tupled, Movie.unapply)
  }

  val movies = TableQuery[MovieTable]
}
