package modules.actor

import java.sql.Date

import modules.util.{Country, Gender}
import modules.utility.database.ExtendedPostgresProfile
import play.api.db.slick.HasDatabaseConfig

trait ActorTableComponent { self: HasDatabaseConfig[ExtendedPostgresProfile] =>
  import profile.api._

  implicit val nationalityColumnType: BaseColumnType[Country.Value] = MappedColumnType.base[Country.Value, String](
    { enum => enum.dbName },
    { string => Country.findByString(string).getOrElse(Country.NoCountry) }
  )

  implicit val genderColumnType: BaseColumnType[Gender.Value] = MappedColumnType.base[Gender.Value, String](
    { enum => enum.dbName },
    { string => Gender.findByString(string).getOrElse(Gender.Other) }
  )

  class ActorTable(tag: Tag) extends Table[Actor](tag, "actor") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def firstName = column[String]("first-name")
    def lastName = column[String]("last-name")
    def birthDate = column[Date]("birth-date")
    def nationality = column[Country.Value]("nationality")
    def height = column[Int]("height")
    def gender = column[Gender.Value]("gender")

    def * = (
      id, firstName, lastName, birthDate, nationality, height, gender
    ) <> ((Actor.apply _).tupled, Actor.unapply)
  }

  val actors = TableQuery[ActorTable]
}
