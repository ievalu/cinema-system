package modules.director

import javax.inject.Inject
import modules.util.{GenderFormatter, NationalityFormatter}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc.{Action, AnyContent, MessagesAbstractController, MessagesControllerComponents}
import views.html
import modules.util._

import scala.concurrent.{ExecutionContext, Future}

class DirectorController @Inject() (
    repo: DirectorRepository,
    cc: MessagesControllerComponents
)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val createDirectorForm: Form[CreateDirectorForm] = Form {
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "birthDate" -> sqlDate,
      "nationality" -> of(NationalityFormatter),
      "height" -> number.verifying(min(0), max(300)),
      "gender" -> of(GenderFormatter)
    )(CreateDirectorForm.apply)(CreateDirectorForm.unapply)
  }

  val filterDirectorForm: Form[FilterDirectorForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "birthDate" -> optional(sqlDate),
      "nationality" -> of(NationalityFormatter),
      "heightMin" -> number.verifying(min(0), max(300)),
      "heightMax" -> number.verifying(min(0), max(300)),
      "gender" -> of(GenderFormatter)
    )(FilterDirectorForm.apply)(FilterDirectorForm.unapply)
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
    val nameToRepo = if (name.trim().nonEmpty) Some(name) else None
    repo
      .list(page, pageSize, nameToRepo, parseDate(birthDate), nationality, heightMin, heightMax, gender, orderBy, order)
      .map(
        directors =>
          Ok(
            html.director.list(
              directors,
              filterDirectorForm.fill(FilterDirectorForm(name, parseDate(birthDate), nationality, heightMin, heightMax, gender)),
              SortItems(orderBy, order)
            )
          )
      )
  }

  def createDirector: Action[AnyContent] = Action {
    implicit request =>
      Ok(html.director.create(createDirectorForm))
  }

  def saveNewDirector: Action[AnyContent] = Action.async {
    implicit request =>
      createDirectorForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(Ok(html.director.create(formWithErrors)))
        },
        newDirector => {
          repo.create(newDirector).map {
            _ => Redirect(routes.DirectorController.list()).flashing("success" -> "Director added")
          }
        }
      )
  }

  def delete(id: Long): Action[AnyContent] = Action.async {
    implicit request =>
      repo
        .findById(id)
        .map{
          case Some(_) => {
            val _ = repo.delete(id)
            Redirect(routes.DirectorController.list()).flashing("success" -> "Director deleted")
          }
          case None => Redirect(routes.DirectorController.list()).flashing("error" -> s"No director with such id = $id")
        }
  }

  def edit(id: Long): Action[AnyContent] = Action.async {
    implicit request =>
      repo
        .findById(id)
        .map {
          case Some(director) =>
            Ok(html.director.create(
              createDirectorForm
                .fill(
                  new CreateDirectorForm(
                    director.firstName,
                    director.lastName,
                    director.birthDate,
                    director.nationality,
                    director.height,
                    director.gender
                  )),
              Some(id)
            ))
          case None => Ok(html.director.create(createDirectorForm))
        }
  }

  def update(id: Long): Action[AnyContent] = Action.async {
    implicit request =>
      createDirectorForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(Ok(html.director.create(formWithErrors)))
        },
        newDirector => {
          repo.update(id, newDirector).map {
            _ => Redirect(routes.DirectorController.list()).flashing("success" -> "Director updated")
          }
        }
      )
  }
}
