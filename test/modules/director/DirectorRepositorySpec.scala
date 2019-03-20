package modules.director

import java.sql.Date
import java.time.LocalDate

import com.dimafeng.testcontainers.PostgreSQLContainer
import modules.util.{Country, Gender}
import org.scalatest.TestData
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import utility.application.TestApplications.basicDatabaseTestApplication
import utility.database.PlayPostgreSQLTest

import scala.concurrent.ExecutionContext

class DirectorRepositorySpec extends PlaySpec
  with GuiceOneAppPerTest
  with PlayPostgreSQLTest
  with ScalaFutures
  with IntegrationPatience {

  override val container: PostgreSQLContainer = PostgreSQLContainer("postgres:alpine")

  override def newAppForTest(testData: TestData): Application = {
    basicDatabaseTestApplication(container, evolutionsEnabled = false)
  }

  "Director repository" must {
    "create new director" in {
      implicit lazy val executionContext = app.injector.instanceOf[ExecutionContext]
      withEvolutions { () =>
        val repo = app.injector.instanceOf[DirectorRepository]
        val director = repo
          .create(new CreateDirectorForm(
            "name",
            "lastName",
            new Date(10000),
            Country.USA,
            187,
            Gender.Male
            )
          ).futureValue

        director mustEqual Director(
          3,
          "name",
          "lastName",
          new Date(10000),
          Country.USA,
          187,
          Gender.Male
        )
      }
    }

    "find director by id" in {
      implicit lazy val executionContext = app.injector.instanceOf[ExecutionContext]
      withEvolutions { () =>
        val repo = app.injector.instanceOf[DirectorRepository]
        val directorOption = repo
          .findById(2)
          .futureValue

        val noneDirector = repo
            .findById(100)
            .futureValue

        directorOption mustEqual Some(Director(
          2,
          "Clementine",
          "Johannes",
          java.sql.Date.valueOf(LocalDate.of(1989, 4, 18)),
          Country.NewZealand,
          178,
          Gender.Female
        ))

        noneDirector mustEqual None
      }
    }

    "update director" in {
      implicit lazy val executionContext = app.injector.instanceOf[ExecutionContext]
      withEvolutions { () =>
        val repo = app.injector.instanceOf[DirectorRepository]
        val updatedDirector = repo
          .update(
            1,
            CreateDirectorForm(
              "Clementine",
              "Duff",
              java.sql.Date.valueOf(LocalDate.of(1989, 4, 18)),
              Country.NewZealand,
              175,
              Gender.Female
            )
          )
          .futureValue

        updatedDirector mustEqual Director(
          1,
          "Clementine",
          "Duff",
          java.sql.Date.valueOf(LocalDate.of(1989, 4, 18)),
          Country.NewZealand,
          175,
          Gender.Female
        )
      }
    }
  }
}
