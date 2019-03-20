package modules

import modules.util.Country.CountryVal
import modules.util.Gender.GenderVal
import modules.util.Language.LanguageVal
import play.api.data.FormError
import play.api.data.format.Formatter

package object util {

  def CountryFormatter: Formatter[CountryVal] = new Formatter[CountryVal] {
    def bind(key: String, data: Map[String, String]): Either[List[FormError], CountryVal] = {
      val value = data.getOrElse(key, "")
      Country
        .findByString(value)
        .map(Right(_))
        .getOrElse(Left(List(FormError(key, s"Country not found by value:'$value'"))))
    }
    def unbind(key: String, value: CountryVal): Map[String, String] = Map(key -> Country.getCountry(value))
  }

  def NationalityFormatter: Formatter[CountryVal] = new Formatter[CountryVal] {
    def bind(key: String, data: Map[String, String]): Either[List[FormError], CountryVal] = {
      val value = data.getOrElse(key, "")
      Country
        .findByString(value)
        .map(Right(_))
        .getOrElse(Left(List(FormError(key, s"Nationality not found by value:'$value'"))))
    }
    def unbind(key: String, value: CountryVal): Map[String, String] = Map(key -> Country.getNationality(value))
  }

  object Country extends Enumeration {
    sealed case class CountryVal private[Country](dbName: String, countryName: String, nationality: String) extends Val(dbName)

    val USA = CountryVal("40", "USA", "American")
    val Australia = CountryVal("2", "Australia", "Australian")
    val NewZealand = CountryVal("23", "New Zealand", "Kiwi")
    val Spain = CountryVal("32", "Spain", "Spanish")
    val Sweden = CountryVal("34", "Sweden", "Swedish")
    val Lithuania = CountryVal("97", "Lithuania", "Lithuanian")
    val NoCountry = CountryVal("", "No Country", "No nationality")

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

    def getCountry(countryVal: CountryVal): String = countryVal match {
      case USA => USA.dbName
      case Australia => Australia.dbName
      case NewZealand => NewZealand.dbName
      case Spain => Spain.dbName
      case Sweden => Sweden.dbName
      case Lithuania => Lithuania.dbName
      case _ => "No such country"
    }

    def getNationality(nationalityVal: CountryVal): String = nationalityVal match {
      case USA => USA.nationality
      case Australia => Australia.nationality
      case NewZealand => NewZealand.nationality
      case Spain => Spain.nationality
      case Sweden => Sweden.nationality
      case Lithuania => Lithuania.nationality
      case _ => "No such nationality"
    }

    def getCountryValues = Seq(USA, Australia, NewZealand, Spain, Sweden, Lithuania)
  }

  def GenderFormatter: Formatter[GenderVal] = new Formatter[GenderVal] {
    def bind(key: String, data: Map[String, String]): Either[List[FormError], GenderVal] = {
      val value = data.getOrElse(key, "")
      Gender
        .findByString(value)
        .map(Right(_))
        .getOrElse(Left(List(FormError(key, s"Gender not found by value:'$value'"))))
    }
    def unbind(key: String, value: GenderVal): Map[String, String] = Map(key -> Gender.getName(value))
  }

  object Gender extends Enumeration {
    sealed case class GenderVal private[Gender](dbName: String, actualName: String) extends Val(dbName)

    val Female = GenderVal("f", "Female")
    val Male = GenderVal("m", "Male")
    val Other = GenderVal("", "Other")

    def findByString(value: String) = Option(
      value match {
        case "f" => Female
        case "m" => Male
      }
    )

    def getName(genderVal: GenderVal) = genderVal match {
      case Female => Female.dbName
      case Male => Male.dbName
      case _ => "Other"
    }

    def getGenderValues = Seq(Female, Male)
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

    val english = LanguageVal("en", "English")
    val swedish = LanguageVal("sw", "Swedish")
    val russian = LanguageVal("ru", "Russian")
    val lithuanian = LanguageVal("lt", "Lithuanian")
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
        case this.english => english.dbName
        case this.swedish => swedish.dbName
        case this.russian => russian.dbName
        case this.lithuanian => lithuanian.dbName
        case _ => "No such language"
      }

    def getLanguageValues = Seq(english, swedish, russian, lithuanian)
  }

  case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
    lazy val prev = Option(page - 1).filter(_ > 0)
    lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
  }
}
