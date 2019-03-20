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

  // FIX flashing doesn't work
  def delete(id: Long): Action[AnyContent] = Action.async {
    implicit request =>
      repo
        .findById(id)
        .map{
          case Some(_) => {
            val _ = repo.delete(id)
            Redirect(routes.MovieController.list()).flashing("success" -> "Movie deleted")
          }
          case None => Redirect(routes.MovieController.list()).flashing("error" -> s"No movie with such id = $id")
      }
  }

  def edit(id: Long): Action[AnyContent] = Action.async {
    implicit request =>
      repo
        .findById(id)
        .map {
          case Some(movie) =>
            Ok(html.movie.create(
              createMovieForm
                .fill(new CreateMovieForm(movie.title, movie.description, movie.releaseDate, movie.country, movie.language)),
              id
            ))
          case None => Ok(html.movie.create(createMovieForm))
        }
  }

  def update(id: Long): Action[AnyContent] = Action.async {
    implicit request =>
      createMovieForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(Ok(html.movie.create(formWithErrors)))
        },
        newMovie => {
          repo.update(id, newMovie).map {
            _ => Redirect(routes.MovieController.list()).flashing("success" -> "Movie updated")
          }
        }
      )
  }
}
