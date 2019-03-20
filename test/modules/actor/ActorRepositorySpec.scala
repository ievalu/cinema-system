package modules.actor

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

class ActorRepositorySpec extends PlaySpec
  with GuiceOneAppPerTest
  with PlayPostgreSQLTest
  with ScalaFutures
  with IntegrationPatience {

  override val container: PostgreSQLContainer = PostgreSQLContainer("postgres:alpine")

  override def newAppForTest(testData: TestData): Application = {
    basicDatabaseTestApplication(container, evolutionsEnabled = false)
  }

  "Actor repository" must {
    "create new actor" in {
      implicit lazy val executionContext = app.injector.instanceOf[ExecutionContext]
      withEvolutions { () =>
        val repo = app.injector.instanceOf[ActorRepository]
        val actor = repo
          .create(new CreateActorForm(
            "name",
            "lastName",
            new Date(10000),
            Country.USA,
            187,
            Gender.Male
          )
          ).futureValue

        actor mustEqual Actor(
          4,
          "name",
          "lastName",
          new Date(10000),
          Country.USA,
          187,
          Gender.Male
        )
      }
    }

    "find actor by id" in {
      implicit lazy val executionContext = app.injector.instanceOf[ExecutionContext]
      withEvolutions { () =>
        val repo = app.injector.instanceOf[ActorRepository]
        val actorOption = repo
          .findById(2)
          .futureValue

        val noneActor = repo
          .findById(100)
          .futureValue

        actorOption mustEqual Some(Actor(
          2,
          "Cameron",
          "Frisbee",
          java.sql.Date.valueOf(LocalDate.of(1981, 10, 27)),
          Country.Australia,
          174,
          Gender.Male
        ))

        noneActor mustEqual None
      }
    }

    "update actor" in {
      implicit lazy val executionContext = app.injector.instanceOf[ExecutionContext]
      withEvolutions { () =>
        val repo = app.injector.instanceOf[ActorRepository]
        val updatedActor = repo
          .update(
            2,
            CreateActorForm(
              "Cameron",
              "John",
              java.sql.Date.valueOf(LocalDate.of(1981, 10, 27)),
              Country.Australia,
              174,
              Gender.Male
            )
          )
          .futureValue

        updatedActor mustEqual Actor(
          2,
          "Cameron",
          "John",
          java.sql.Date.valueOf(LocalDate.of(1981, 10, 27)),
          Country.Australia,
          174,
          Gender.Male
        )
      }
    }
  }
}
