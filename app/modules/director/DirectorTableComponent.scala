package modules.director

import java.sql.Date

import modules.util.Country.CountryVal
import modules.util.{Country, Gender}
import modules.util.Gender.GenderVal
import modules.utility.database.ExtendedPostgresProfile
import play.api.db.slick.HasDatabaseConfig

trait DirectorTableComponent { self: HasDatabaseConfig[ExtendedPostgresProfile] =>
  import profile.api._

  implicit val nationalityColumnType: BaseColumnType[CountryVal] = MappedColumnType.base[CountryVal, String](
    { enum => Country.getNationality(enum) },
    { string => Country.findByString(string).getOrElse(Country.NoCountry) }
  )

  implicit val genderColumnType: BaseColumnType[GenderVal] = MappedColumnType.base[GenderVal, String](
    { enum => Gender.getName(enum) },
    { string => Gender.findByString(string).getOrElse(Gender.Other) }
  )

  class DirectorTable(tag: Tag) extends Table[Director](tag, "director") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def firstName = column[String]("first-name")
    def lastName = column[String]("last-name")
    def birthDate = column[Date]("birth-date")
    def nationality = column[CountryVal]("nationality")
    def height = column[Int]("height")
    def gender = column[GenderVal]("gender")

    def * = (
      id, firstName, lastName, birthDate, nationality, height, gender
    ) <> ((Director.apply _).tupled, Director.unapply)
  }

  val directors = TableQuery[DirectorTable]
}
