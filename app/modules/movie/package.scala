package modules

import java.sql.Date

import modules.util.Country.CountryVal
import modules.util.Language.LanguageVal

package object movie {

  case class Movie (
      id: Long,
      title: String,
      description: String,
      releaseDate: Date,
      country: CountryVal,
      language: LanguageVal
  )

  case class CreateMovieForm (
      title: String,
      description: String,
      releaseDate: Date,
      country: CountryVal,
      language: LanguageVal
  )

  case class FilterMovieForm (
      title: String,
      description: String,
      releaseDate: Option[Date]
  )
}
