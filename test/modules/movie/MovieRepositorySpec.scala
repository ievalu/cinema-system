package modules.movie

import java.sql.Date

import com.dimafeng.testcontainers.PostgreSQLContainer
import org.scalatest.TestData
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import utility.application.TestApplications._
import utility.database.PlayPostgreSQLTest

import scala.concurrent.ExecutionContext

class MovieRepositorySpec extends PlaySpec
  with GuiceOneAppPerTest
  with PlayPostgreSQLTest
  with ScalaFutures
  with IntegrationPatience {

  override val container: PostgreSQLContainer = PostgreSQLContainer("postgres:alpine")

  override def newAppForTest(testData: TestData): Application = {
    basicDatabaseTestApplication(container, evolutionsEnabled = false)
  }

  "MovieRepository" must {
    "create new movie" in {
      implicit lazy val executionContext = app.injector.instanceOf[ExecutionContext]
      withEvolutions { () =>
        val repo = app.injector.instanceOf[MovieRepository]
        val movie = repo
          .create(new CreateMovieForm(
            "New title",
            "New description",
            new Date(10000),
            Country.USA,
            Language.english
          )
          ).futureValue

        movie mustEqual Movie(
          6,
          "New title",
          "New description",
          new Date(10000),
          Country.USA,
          Language.english
        )
      }
    }
  }
}
