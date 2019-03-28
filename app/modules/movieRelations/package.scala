package modules

package object movieRelations {

  case class MovieGenre (
      id: Long,
      movieId: Long,
      genreId: Long
  )

  case class MovieActor (
      id: Long,
      movieId: Long,
      actorId: Long
  )

  case class MovieDirector (
      id: Long,
      movieId: Long,
      directorId: Long
  )
}
