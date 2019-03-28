package modules.genre

import modules.utility.database.ExtendedPostgresProfile
import play.api.db.slick.HasDatabaseConfig

trait GenreTableComponent { self: HasDatabaseConfig[ExtendedPostgresProfile] =>
  import profile.api._

  class GenreTable(tag: Tag) extends Table[Genre](tag, "genre") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")

    def * = (id, title) <> ((Genre.apply _).tupled, Genre.unapply)
  }

  val genres = TableQuery[GenreTable]
}
