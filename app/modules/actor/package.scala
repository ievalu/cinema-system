package modules

import java.sql.Date

import modules.util.Country.CountryVal
import modules.util.Gender.GenderVal

package object actor {

  case class Actor (
      id: Long,
      firstName: String,
      lastName: String,
      birthDate: Date,
      nationality: CountryVal,
      height: Int,
      gender: GenderVal
  )

  case class CreateActorForm (
      firstName: String,
      lastName: String,
      birthDate: Date,
      nationality: CountryVal,
      height: Int,
      gender: GenderVal
  )

  case class FilterActorForm (
      name: String,
      birthDate: Option[Date],
      heightMin: Int,
      heightMax: Int
  )
}
