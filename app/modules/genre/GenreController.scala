package modules.genre

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesAbstractController, MessagesControllerComponents}
import views.html

import scala.concurrent.ExecutionContext

class GenreController @Inject() (
    repo: GenreRepository,
    cc: MessagesControllerComponents
)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def list: Action[AnyContent] = Action.async { implicit request =>
    repo.list().map(genres => Ok(html.genre.list(genres)))
  }
}
