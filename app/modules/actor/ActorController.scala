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

  def list(page: Int, pageSize: Int): Action[AnyContent] = Action.async { implicit request =>
    repo.list(page, pageSize).map(actors => Ok(html.actor.list(actors)))
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

  // FIX flashing doesn't work
  def delete(id: Long): Action[AnyContent] = Action.async {
    implicit request =>
      repo
        .findById(id)
        .map{
          case Some(_) => {
            val _ = repo.delete(id)
            Redirect(routes.ActorController.list()).flashing("success" -> "Actor deleted")
          }
          case None => Redirect(routes.ActorController.list()).flashing("error" -> s"No actor with such id = $id")
        }
  }

  def edit(id: Long): Action[AnyContent] = Action.async {
    implicit request =>
      repo
        .findById(id)
        .map {
          case Some(actor) =>
            Ok(html.actor.create(
              createActorForm
                .fill(
                  new CreateActorForm(
                    actor.firstName,
                    actor.lastName,
                    actor.birthDate,
                    actor.nationality,
                    actor.height,
                    actor.gender
                  )),
              id
            ))
          case None => Ok(html.actor.create(createActorForm))
        }
  }

  def update(id: Long): Action[AnyContent] = Action.async {
    implicit request =>
      createActorForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(Ok(html.actor.create(formWithErrors)))
        },
        newActor => {
          repo.update(id, newActor).map {
            _ => Redirect(routes.ActorController.list()).flashing("success" -> "Actor updated")
          }
        }
      )
  }
}
