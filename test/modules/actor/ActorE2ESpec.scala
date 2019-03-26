package modules.actor

import java.time.LocalDate

import com.dimafeng.testcontainers.PostgreSQLContainer
import modules.util.{Country, Gender}
import org.scalatest.{OptionValues, TestData}
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.{HtmlUnitFactory, OneBrowserPerTest, PlaySpec}
import org.scalatestplus.play.guice.GuiceOneServerPerTest
import play.api.Application
import utility.application.TestApplications.basicDatabaseTestApplication
import utility.database.PlayPostgreSQLTest

class ActorE2ESpec extends PlaySpec
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

  "Actor form" must {
    "create actor" in {
      {
        val url = s"http://localhost:$port${modules.actor.routes.ActorController.createActor().url}"
        go to url
      }

      eventually {
        pageTitle mustBe "Add actor"
      }

      click on name("firstName")
      textField("firstName").value = "First name"

      click on name("lastName")
      textField("lastName").value = "Last name"

      executeScript("document.getElementById('birthDate').value = '1996-09-26'")

      click on name("nationality")
      singleSel("nationality").value = "IT"

      click on name("height")
      numberField("height").value = "150"

      click on name("gender")
      singleSel("gender").value = "f"

      click on id("create-submit")

      val repo = app.injector.instanceOf[ActorRepository]
      eventually {
        val actor = repo.findById(4).futureValue
        actor mustEqual Some(Actor(
          4,
          "First name",
          "Last name",
          java.sql.Date.valueOf(LocalDate.of(1996, 9, 26)),
          Country.Italy,
          150,
          Gender.Female
        ))
      }
    }
  }

}
