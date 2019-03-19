package modules

package object genre {

  case class Genre (
      id: Long,
      title: String
  )

  case class CreateGenreForm (title: String)
}
