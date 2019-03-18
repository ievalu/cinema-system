package modules.movie

import javax.inject._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import views.html
import modules.util._

import scala.concurrent.{ExecutionContext, Future}

class MovieController @Inject()(
    repo: MovieRepository,
    cc: MessagesControllerComponents
)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val createMovieForm: Form[CreateMovieForm] = Form {
    mapping(
      "title" -> nonEmptyText,
      "description" -> nonEmptyText,
      "releaseDate" -> sqlDate,
      "country" -> of(CountryFormatter),
      "language" -> of(LanguageFormatter)
    )(CreateMovieForm.apply)(CreateMovieForm.unapply)
  }

  def index: Action[AnyContent] = Action { implicit request =>
    Ok(html.index())
  }

  def list: Action[AnyContent] = Action.async { implicit request =>
    repo.list().map(movies => Ok(html.movie.list(movies)))
  }

  def createMovie: Action[AnyContent] = Action {
    implicit request =>
      Ok(html.movie.create(createMovieForm))
  }

  def saveNewMovie: Action[AnyContent] = Action.async {
    implicit request =>
      createMovieForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(Ok(html.movie.create(formWithErrors)))
        },
        newMovie => {
          repo.create(newMovie).map {
            _ => Redirect(routes.MovieController.list()).flashing("success" -> "Movie created")
          }
        }
      )
  }
}
