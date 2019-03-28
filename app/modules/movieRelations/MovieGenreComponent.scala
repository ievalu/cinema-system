package modules.movieRelations

import modules.utility.database.ExtendedPostgresProfile
import play.api.db.slick.HasDatabaseConfig

trait MovieGenreComponent { self: HasDatabaseConfig[ExtendedPostgresProfile] =>
  import profile.api._

  class MovieGenreTable(tag: Tag) extends Table[MovieGenre](tag, "movie-genre") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def movieId = column[Long]("movie-id")
    def genreId = column[Long]("genre-id")

    def * = (id, movieId, genreId) <> ((MovieGenre.apply _).tupled, MovieGenre.unapply)
  }

  val movieGenres = TableQuery[MovieGenreTable]
}
