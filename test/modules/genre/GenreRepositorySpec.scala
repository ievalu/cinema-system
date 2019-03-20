package modules.genre

import java.time.LocalDate

import com.dimafeng.testcontainers.PostgreSQLContainer
import org.scalatest.TestData
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import utility.application.TestApplications.basicDatabaseTestApplication
import utility.database.PlayPostgreSQLTest

import scala.concurrent.ExecutionContext

class GenreRepositorySpec extends PlaySpec
  with GuiceOneAppPerTest
  with PlayPostgreSQLTest
  with ScalaFutures
  with IntegrationPatience {

  override val container: PostgreSQLContainer = PostgreSQLContainer("postgres:alpine")

  override def newAppForTest(testData: TestData): Application = {
    basicDatabaseTestApplication(container, evolutionsEnabled = false)
  }

  "Genre repository" must {
    "create new genre" in {
      implicit lazy val executionContext = app.injector.instanceOf[ExecutionContext]
      withEvolutions { () =>
        val repo = app.injector.instanceOf[GenreRepository]
        val genre = repo.create(
          new CreateGenreForm("newGenre")
        ).futureValue

        genre mustEqual Genre(6, "newGenre")
      }
    }

    "find genre by id" in {
      implicit lazy val executionContext = app.injector.instanceOf[ExecutionContext]
      withEvolutions { () =>
        val repo = app.injector.instanceOf[GenreRepository]
        val optionGenre = repo
          .findById(1)
          .futureValue
        val noneGenre = repo
          .findById(100)
          .futureValue

        optionGenre mustEqual Some(Genre(
          1,
          "comedy",
        ))

        noneGenre mustEqual None
      }
    }

    "update genre" in {
      implicit lazy val executionContext = app.injector.instanceOf[ExecutionContext]
      withEvolutions { () =>
        val repo = app.injector.instanceOf[GenreRepository]
        val updatedGenre = repo
          .update(
            1,
            CreateGenreForm(
              "Romance comedy"))
          .futureValue

        updatedGenre mustEqual Genre(
          1,
          "Romance comedy"
        )
      }
    }
  }
}
