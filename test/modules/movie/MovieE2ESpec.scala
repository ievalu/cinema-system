package modules.movie

import java.time.LocalDate

import com.dimafeng.testcontainers.PostgreSQLContainer
import modules.util.{Country, Language}
import org.scalatest.{OptionValues, TestData}
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.{HtmlUnitFactory, OneBrowserPerTest, PlaySpec}
import org.scalatestplus.play.guice.GuiceOneServerPerTest
import play.api.Application
import utility.application.TestApplications.basicDatabaseTestApplication
import utility.database.PlayPostgreSQLTest

class MovieE2ESpec extends PlaySpec
  with GuiceOneServerPerTest
  with OneBrowserPerTest
  with HtmlUnitFactory
  with PlayPostgreSQLTest
  with ScalaFutures
  with Eventually
  with IntegrationPatience
  with OptionValues {

  override val container: PostgreSQLContainer = PostgreSQLContainer("postgres:alpine")

  override def newAppForTest(testData: TestData): Application = {
    basicDatabaseTestApplication(container)
  }

  "Movie form" must {

    "create movie" in {
      {
        val url = s"http://localhost:$port${modules.movie.routes.MovieController.createMovie().url}"
        go to url
      }

      eventually {
        pageTitle mustBe "Create movie"
      }

      click on name("title")
      textField("title").value = "Movie title"

      click on name("description")
      textField("description").value = "Movie description"

      executeScript("document.getElementById('releaseDate').value = '1996-09-26'")

      click on name("country")
      singleSel("country").value = "SE"

      click on name("language")
      singleSel("language").value = "lt"

      click on id("create-submit")
      // submit()

      val repo = app.injector.instanceOf[MovieRepository]
      eventually {
        val movie = repo.findById(6).futureValue
        movie mustEqual Some(Movie(
          6,
          "Movie title",
          "Movie description",
          java.sql.Date.valueOf(LocalDate.of(1996, 9, 26)),
          Country.Sweden,
          Language.lithuanian
        )
        )
      }
    }
  }
}
