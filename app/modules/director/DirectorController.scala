package modules.director

import javax.inject.Inject
import modules.util.{GenderFormatter, NationalityFormatter}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc.{Action, AnyContent, MessagesAbstractController, MessagesControllerComponents}
import views.html

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

  def list: Action[AnyContent] = Action.async { implicit request =>
    repo.list().map(directors => Ok(html.director.list(directors)))
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
}
