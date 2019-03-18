package modules

import java.sql.Date

import modules.movie.Country.CountryVal
import modules.movie.Language.LanguageVal
import play.api.data.FormError
import play.api.data.format.Formatter

package object movie {

  def CountryFormatter: Formatter[CountryVal] = new Formatter[CountryVal] {
    def bind(key: String, data: Map[String, String]): Either[List[FormError], CountryVal] = {
      val value = data.getOrElse(key, "")
      Country
        .findByString(value)
        .map(Right(_))
        .getOrElse(Left(List(FormError(key, s"Country not found by value:'$value'"))))
    }
    def unbind(key: String, value: CountryVal): Map[String, String] = Map(key -> Country.getName(value))
  }

  object Country extends Enumeration {
    sealed case class CountryVal private[Country](dbName: String, actualName: String) extends Val(dbName)

    val USA = CountryVal("40", "USA")
    val Australia = CountryVal("2", "Australia")
    val NewZealand = CountryVal("23", "New Zealand")
    val Spain = CountryVal("32", "Spain")
    val Sweden = CountryVal("34", "Sweden")
    val Lithuania = CountryVal("97", "Lithuania")
    val NoCountry = CountryVal("", "No Country")

    def findByString(value: String): Option[CountryVal] = {
      Option(
        value match {
          case "40" => USA
          case "2" => Australia
          case "23" => NewZealand
          case "32" => Spain
          case "34" => Sweden
          case "97" => Lithuania
        }
      )
    }

    def getName(countryVal: CountryVal): String = countryVal match {
      case USA => USA.actualName
      case Australia => Australia.actualName
      case NewZealand => NewZealand.actualName
      case Spain => Spain.actualName
      case Sweden => Sweden.actualName
      case Lithuania => Lithuania.actualName
      case _ => "No such country"
    }
  }

  def LanguageFormatter: Formatter[LanguageVal] = new Formatter[LanguageVal] {
    def bind(key: String, data: Map[String, String]): Either[List[FormError], LanguageVal] = {
      val value = data.getOrElse(key, "")
      Language
        .findByString(value)
        .map(Right(_))
        .getOrElse(Left(List(FormError(key, s"Language not found by value:'$value'"))))
    }
    def unbind(key: String, value: LanguageVal): Map[String, String] = Map(key -> Language.getName(value))
  }

  object Language extends Enumeration {
    sealed case class LanguageVal private[Language](dbName: String, actualName: String) extends Val(dbName)

    val english = LanguageVal("en", "english")
    val swedish = LanguageVal("sw", "swedish")
    val russian = LanguageVal("ru", "russian")
    val lithuanian = LanguageVal("lt", "lithuanian")
    val NoLanguage = LanguageVal("", "No language")

    def findByString(value: String): Option[LanguageVal] = {
      Option(
        value match {
          case "en" => english
          case "sw" => swedish
          case "ru" => russian
          case "lt" => lithuanian
        }
      )
    }

    def getName(languageVal: LanguageVal): String =
      languageVal match {
      case this.english => english.actualName
      case this.swedish => swedish.actualName
      case this.russian => russian.actualName
      case this.lithuanian => lithuanian.actualName
      case _ => "No such language"
    }
  }

  case class Movie(
      id: Long,
      title: String,
      description: String,
      releaseDate: Date,
      country: CountryVal,
      language: LanguageVal
  )

}
