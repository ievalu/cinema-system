package modules.movieRelations

import modules.utility.database.ExtendedPostgresProfile
import play.api.db.slick.HasDatabaseConfig

trait MovieActorComponent { self: HasDatabaseConfig[ExtendedPostgresProfile] =>
  import profile.api._

  class MovieActorTable(tag: Tag) extends Table[MovieActor](tag, "movie-actor") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def movieId = column[Long]("movie-id")
    def actorId = column[Long]("actor-id")

    def * = (id, movieId, actorId) <> ((MovieActor.apply _).tupled, MovieActor.unapply)
  }

  val movieGenres = TableQuery[MovieActorTable]

}
