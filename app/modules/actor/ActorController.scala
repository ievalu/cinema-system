package modules.actor

import javax.inject.Inject
import modules.util.{GenderFormatter, NationalityFormatter}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints.{max, min}
import play.api.mvc.{Action, AnyContent, MessagesAbstractController, MessagesControllerComponents}
import views.html
import modules.util._

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

  val filterActorForm: Form[FilterActorForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "birthDate" -> optional(sqlDate),
      "nationality" -> of(NationalityFormatter),
      "heightMin" -> number.verifying(min(0), max(300)),
      "heightMax" -> number.verifying(min(0), max(300)),
      "gender" -> of(GenderFormatter)
    )(FilterActorForm.apply)(FilterActorForm.unapply)
  }

  def list(
      page: Int,
      pageSize: Int,
      name: String,
      birthDate: String,
      nationality: Country.Value,
      heightMin: Int,
      heightMax: Int,
      gender: Gender.Value,
      orderBy: SortableField.Value,
      order: SortOrder.Value
  ): Action[AnyContent] = Action.async { implicit request =>
    repo
      .list(page, pageSize, "%" + name + "%", parseDate(birthDate), nationality, heightMin, heightMax, gender, orderBy, order)
      .map(actors => Ok(html.actor.list(actors, filterActorForm.fill(FilterActorForm(name, parseDate(birthDate), nationality, heightMin, heightMax, gender)), SortItems(orderBy, order))))
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
