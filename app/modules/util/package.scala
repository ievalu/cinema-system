package modules

import java.sql.Date
import java.text.SimpleDateFormat

import modules.util.Country.CountryVal
import modules.util.Gender.GenderVal
import modules.util.Language.LanguageVal
import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.mvc.QueryStringBindable

import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

package object util {

  def CountryFormatter: Formatter[CountryVal] = new Formatter[CountryVal] {
    def bind(key: String, data: Map[String, String]): Either[List[FormError], CountryVal] = {
      val value = data.getOrElse(key, "")
      Country
        .findByString(value)
        .map(Right(_))
        .getOrElse(Left(List(FormError(key, s"Country not found by value:'$value'"))))
    }
    def unbind(key: String, value: CountryVal): Map[String, String] = Map(key -> value.dbName)
  }

  def NationalityFormatter: Formatter[CountryVal] = new Formatter[CountryVal] {
    def bind(key: String, data: Map[String, String]): Either[List[FormError], CountryVal] = {
      val value = data.getOrElse(key, "")
      Country
        .findByString(value)
        .map(Right(_))
        .getOrElse(Left(List(FormError(key, s"Nationality not found by value:'$value'"))))
    }
    def unbind(key: String, value: CountryVal): Map[String, String] = Map(key -> value.nationality)
  }

  object Country extends Enumeration {
    sealed case class CountryVal private[Country](dbName: String, countryName: String, nationality: String) extends Val(dbName)

    val USA = CountryVal("US", "USA", "American")
    val Australia = CountryVal("AU", "Australia", "Australian")
    val NewZealand = CountryVal("NZ", "New Zealand", "Kiwi")
    val Spain = CountryVal("ES", "Spain", "Spanish")
    val Sweden = CountryVal("SE", "Sweden", "Swedish")
    val Lithuania = CountryVal("LT", "Lithuania", "Lithuanian")
    val Italy = CountryVal("IT", "Italy", "Italian")
    val Afghanistan = CountryVal("AF", "Afghanistan", "Afghan")
    val Belgium = CountryVal("BE", "Belgium", "Belgian")
    val Brazil = CountryVal("BR", "Brazil", "Brazilian")
    val NoCountry = CountryVal("", "", "")

    implicit def valueToCountryVal(x: Value): CountryVal = x.asInstanceOf[CountryVal]

    def findByString(value: String): Option[CountryVal] = {
      Option(
        value match {
          case "US" => USA
          case "AU" => Australia
          case "NZ" => NewZealand
          case "ES" => Spain
          case "SE" => Sweden
          case "LT" => Lithuania
          case "IT" => Italy
          case "AF" => Afghanistan
          case "BE" => Belgium
          case "BR" => Brazil
          case _ => NoCountry
        }
      )
    }

    implicit def queryStringBinder(implicit stringBinder: QueryStringBindable[String]) =
      new QueryStringBindable[CountryVal] {
        override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, CountryVal]] = {
          stringBinder.bind(key, params)
            .map {
              case Right(s) =>
                Try(Country.findByString(s)) match {
                  case Success(country) =>
                    Right(country.get)
                  case Failure(_) =>
                    Left(s"Failed to parse country from '$s'")
                }
              case Left(baseBinderFailure) =>
                Left(baseBinderFailure)
            }
        }
        override def unbind(key: String, country: CountryVal): String = {
          stringBinder.unbind(key, country.dbName)
        }
      }
  }

  def GenderFormatter: Formatter[GenderVal] = new Formatter[GenderVal] {
    def bind(key: String, data: Map[String, String]): Either[List[FormError], GenderVal] = {
      val value = data.getOrElse(key, "")
      Gender
        .findByString(value)
        .map(Right(_))
        .getOrElse(Left(List(FormError(key, s"Gender not found by value:'$value'"))))
    }
    def unbind(key: String, value: GenderVal): Map[String, String] = Map(key -> value.dbName)
  }

  object Gender extends Enumeration {
    sealed case class GenderVal private[Gender](dbName: String, actualName: String) extends Val(dbName)

    val Female = GenderVal("f", "Female")
    val Male = GenderVal("m", "Male")
    val Other = GenderVal("", "")

    implicit def valueToCountryVal(x: Value): GenderVal = x.asInstanceOf[GenderVal]

    def findByString(value: String) = Option(
      value match {
        case "f" => Female
        case "m" => Male
        case _ => Other
      }
    )

    implicit def queryStringBinder(implicit stringBinder: QueryStringBindable[String]) =
      new QueryStringBindable[GenderVal] {
        override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, GenderVal]] = {
          stringBinder.bind(key, params)
            .map {
              case Right(s) =>
                Try(Gender.findByString(s)) match {
                  case Success(gender) =>
                    Right(gender.get)
                  case Failure(_) =>
                    Left(s"Failed to parse gender from '$s'")
                }
              case Left(baseBinderFailure) =>
                Left(baseBinderFailure)
            }
        }
        override def unbind(key: String, gender: GenderVal): String = {
          stringBinder.unbind(key, gender.dbName)
        }
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
    def unbind(key: String, value: LanguageVal): Map[String, String] = Map(key -> value.dbName)
  }

  object Language extends Enumeration {
    sealed case class LanguageVal private[Language](dbName: String, actualName: String) extends Val(dbName)

    val english = LanguageVal("en", "English")
    val swedish = LanguageVal("sw", "Swedish")
    val russian = LanguageVal("ru", "Russian")
    val lithuanian = LanguageVal("lt", "Lithuanian")
    val NoLanguage = LanguageVal("", "")

    implicit def valueToCountryVal(x: Value): LanguageVal = x.asInstanceOf[LanguageVal]

    def findByString(value: String): Option[LanguageVal] = {
      Option(
        value match {
          case "en" => english
          case "sw" => swedish
          case "ru" => russian
          case "lt" => lithuanian
          case _ => NoLanguage
        }
      )
    }

    implicit def queryStringBinder(implicit stringBinder: QueryStringBindable[String]) =
      new QueryStringBindable[LanguageVal] {
        override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, LanguageVal]] = {
          stringBinder.bind(key, params)
            .map {
              case Right(s) =>
                Try(Language.findByString(s)) match {
                  case Success(language) =>
                    Right(language.get)
                  case Failure(_) =>
                    Left(s"Failed to parse language from '$s'")
                }
              case Left(baseBinderFailure) =>
                Left(baseBinderFailure)
            }
        }
        override def unbind(key: String, language: LanguageVal): String = {
          stringBinder.unbind(key, language.dbName)
        }
      }
  }

  case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
    lazy val prev = Option(page - 1).filter(_ > 0)
    lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
  }

  def parseDate(date: String): Option[Date] = {
    if (date == "") None
    else {
      val format = new SimpleDateFormat("yyyy-MM-dd")
      val parsed = format.parse(date)
      Some(new Date(parsed.getTime))
    }
  }

  object SortOrder extends Enumeration {
    type Order = Value
    val asc = Value("asc")
    val desc = Value("desc")

    implicit def queryStringBinder(implicit stringBinder: QueryStringBindable[String]) =
      new QueryStringBindable[Order] {

        override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Order]] = {
          stringBinder.bind(key, params)
            .map {
              case Right(s) =>
                Try(SortOrder.withName(s)) match {
                  case Success(sortOrder) =>
                    Right(sortOrder)
                  case Failure(_) =>
                    Left(s"Failed to parse sort order from '$s'")
                }
              case Left(baseBinderFailure) =>
                Left(baseBinderFailure)
            }
        }

        override def unbind(key: String, sortOrder: Order): String = {
          stringBinder.unbind(key, sortOrder.toString)
        }
      }
  }
}
