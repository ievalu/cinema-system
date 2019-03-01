package models

import modules.utility.database.ExtendedPostgresProfile
import play.api.db.slick.HasDatabaseConfig

trait PersonTableComponent { self: HasDatabaseConfig[ExtendedPostgresProfile] =>
  import profile.api._

  class PeopleTable(tag: Tag) extends Table[Person](tag, "people") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def age = column[Int]("age")

    def * = (id, name, age) <> ((Person.apply _).tupled, Person.unapply)
  }

  val people = TableQuery[PeopleTable]
}
