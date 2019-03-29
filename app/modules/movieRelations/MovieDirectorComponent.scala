package modules.movieRelations

import modules.utility.database.ExtendedPostgresProfile
import play.api.db.slick.HasDatabaseConfig

trait MovieDirectorComponent { self: HasDatabaseConfig[ExtendedPostgresProfile] =>
  import profile.api._

  class MovieDirectorTable(tag: Tag) extends Table[MovieDirector](tag, "movie-director") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def movieId = column[Long]("movie-id")
    def directorId = column[Long]("director-id")

    def * = (id, movieId, directorId) <> ((MovieDirector.apply _).tupled, MovieDirector.unapply)
  }

  val movieDirectors = TableQuery[MovieDirectorTable]

}
