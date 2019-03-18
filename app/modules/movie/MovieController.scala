package modules.movie

import javax.inject._
import play.api.mvc._
import views.html

import scala.concurrent.{ExecutionContext}

class MovieController @Inject()(
    repo: MovieRepository,
    cc: MessagesControllerComponents
)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def index: Action[AnyContent] = Action { implicit request =>
    Ok(html.index())
  }

  def list: Action[AnyContent] = Action.async { implicit request =>
    repo.list().map(movies => Ok(html.movie.list(movies)))
  }
}
