package modules.genre

import javax.inject.Inject
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, AnyContent, MessagesAbstractController, MessagesControllerComponents}
import views.html

import scala.concurrent.{ExecutionContext, Future}

class GenreController @Inject() (
    repo: GenreRepository,
    cc: MessagesControllerComponents
)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val createGenreForm = Form (
    mapping (
      "title" -> nonEmptyText
    )(CreateGenreForm.apply)(CreateGenreForm.unapply)
  )

  def list: Action[AnyContent] = Action.async { implicit request =>
    repo.list().map(genres => Ok(html.genre.list(genres)))
  }

  def createGenre: Action[AnyContent] = Action {
    implicit request =>
      Ok(html.genre.create(createGenreForm))
  }

  def saveNewGenre: Action[AnyContent] = Action.async {
    implicit request =>
      createGenreForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(Ok(html.genre.create(formWithErrors)))
        },
        newGenre => {
          repo.create(newGenre).map {
            _ => Redirect(routes.GenreController.list()).flashing("success" -> "Genre created")
          }
        }
      )
  }
}
