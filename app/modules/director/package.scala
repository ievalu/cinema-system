package modules

import java.sql.Date

import modules.director.Gender.GenderVal
import modules.director.Nationality.NationalityVal
import play.api.data.FormError
import play.api.data.format.Formatter

package object director {

  def NationalityFormatter: Formatter[NationalityVal] = new Formatter[NationalityVal] {
    def bind(key: String, data: Map[String, String]): Either[List[FormError], NationalityVal] = {
      val value = data.getOrElse(key, "")
      Nationality
        .findByString(value)
        .map(Right(_))
        .getOrElse(Left(List(FormError(key, s"Nationality not found by value:'$value'"))))
    }
    def unbind(key: String, value: NationalityVal): Map[String, String] = Map(key -> Nationality.getName(value))
  }

  object Nationality extends Enumeration {
    sealed case class NationalityVal private[Nationality](dbName: String, actualName: String) extends Val(dbName)

    val american = NationalityVal("40", "American")
    val australian = NationalityVal("2", "Australian")
    val kiwi = NationalityVal("23", "Kiwi")
    val spanish = NationalityVal("32", "Spanish")
    val swedish = NationalityVal("34", "Swedish")
    val lithuanian = NationalityVal("97", "Lithuanian")
    val noNationality = NationalityVal("", "No such nationality")

    def findByString(value: String): Option[NationalityVal] = {
      Option(
        value match {
          case "40" => american
          case "2" => australian
          case "23" => kiwi
          case "32" => spanish
          case "34" => swedish
          case "97" => lithuanian
        }
      )
    }

    def getName(nationalityVal: NationalityVal): String = nationalityVal match {
      case this.american => american.dbName
      case this.australian => australian.dbName
      case this.kiwi => kiwi.dbName
      case this.spanish => spanish.dbName
      case this.swedish => swedish.dbName
      case this.lithuanian => lithuanian.dbName
      case _ => "No such nationality"
    }

    def getCountryValues = Seq(american, australian, kiwi, spanish, swedish, lithuanian)
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
      case Female => Female.actualName
      case Male => Male.actualName
      case _ => "Other"
    }

    def getCountryValues = Seq(Female, Male)
  }

  case class Director (
      id: Long,
      firstName: String,
      lastName: String,
      birthDate: Date,
      nationality: NationalityVal,
      height: Int,
      gender: GenderVal
  )
}
