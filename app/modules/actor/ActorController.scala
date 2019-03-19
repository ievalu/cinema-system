package modules.actor

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesAbstractController, MessagesControllerComponents}
import views.html

import scala.concurrent.ExecutionContext

class ActorController @Inject() (
    repo: ActorRepository,
    cc: MessagesControllerComponents
) (implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def list: Action[AnyContent] = Action.async { implicit request =>
    repo.list().map(actors => Ok(html.actor.list(actors)))
  }
}
