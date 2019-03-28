package modules

import modules.util.SortOrder
import play.api.mvc.QueryStringBindable

import scala.util.{Failure, Success, Try}

package object genre {

  case class Genre (
      id: Long,
      title: String
  )

  case class CreateGenreForm (title: String)

  object SortableField extends Enumeration {
    type Field = Value
    val id = Value("id")
    val title = Value("title")

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

  case class SortItems(
      field: SortableField.Value,
      order: SortOrder.Value
  )
}
