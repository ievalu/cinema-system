package modules.actor

import javax.inject.Inject
import modules.util.{GenderFormatter, NationalityFormatter}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints.{max, min}
import play.api.mvc.{Action, AnyContent, MessagesAbstractController, MessagesControllerComponents}
import views.html

import scala.concurrent.{ExecutionContext, Future}

class ActorController @Inject() (
    repo: ActorRepository,
    cc: MessagesControllerComponents
) (implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val createActorForm: Form[CreateActorForm] = Form {
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "birthDate" -> sqlDate,
      "nationality" -> of(NationalityFormatter),
      "height" -> number.verifying(min(0), max(300)),
      "gender" -> of(GenderFormatter)
    )(CreateActorForm.apply)(CreateActorForm.unapply)
  }

  def list: Action[AnyContent] = Action.async { implicit request =>
    repo.list().map(actors => Ok(html.actor.list(actors)))
  }

  def createActor: Action[AnyContent] = Action {
    implicit request =>
      Ok(html.actor.create(createActorForm))
  }

  def saveNewActor: Action[AnyContent] = Action.async {
    implicit request =>
      createActorForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(Ok(html.actor.create(formWithErrors)))
        },
        newActor => {
          repo.create(newActor).map {
            _ => Redirect(routes.ActorController.list()).flashing("success" -> "Actor added")
          }
        }
      )
  }
}
