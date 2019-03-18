package modules.director

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesAbstractController, MessagesControllerComponents}
import views.html

import scala.concurrent.ExecutionContext

class DirectorController @Inject() (
    repo: DirectorRepository,
    cc: MessagesControllerComponents
)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def list: Action[AnyContent] = Action.async { implicit request =>
    repo.list().map(directors => Ok(html.director.list(directors)))
  }
}
