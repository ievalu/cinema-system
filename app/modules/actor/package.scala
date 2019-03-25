package modules

import java.sql.Date

import modules.util.Country.CountryVal
import modules.util.Gender.GenderVal
import play.api.mvc.QueryStringBindable

import scala.util.{Failure, Success, Try}

package object actor {

  case class Actor (
      id: Long,
      firstName: String,
      lastName: String,
      birthDate: Date,
      nationality: CountryVal,
      height: Int,
      gender: GenderVal
  )

  case class CreateActorForm (
      firstName: String,
      lastName: String,
      birthDate: Date,
      nationality: CountryVal,
      height: Int,
      gender: GenderVal
  )

  case class FilterActorForm (
      name: String,
      birthDate: Option[Date],
      heightMin: Int,
      heightMax: Int
  )

  object SortableField extends Enumeration {
    type Field = Value
    val name = Value("name")
    val birthDate = Value("birthDate")
    val height = Value("height")

    implicit def queryStringBinder(implicit stringBinder: QueryStringBindable[String]) =
      new QueryStringBindable[Field] {

        override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Field]] = {
          stringBinder.bind(key, params)
            .map {
              case Right(s) =>
                Try(SortableField.withName(s)) match {
                  case Success(sortField) =>
                    Right(sortField)
                  case Failure(_) =>
                    Left(s"Failed to parse sort field from '$s'")
                }
              case Left(baseBinderFailure) =>
                Left(baseBinderFailure)
            }
        }

        override def unbind(key: String, sortField: Field): String = {
          stringBinder.unbind(key, sortField.toString)
        }
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

  case class SortItems(
      field: SortableField.Value,
      order: SortOrder.Value
  )
}
