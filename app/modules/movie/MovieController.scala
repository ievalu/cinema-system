package modules.movie

import javax.inject._
import modules.actor.ActorRepository
import modules.director.DirectorRepository
import modules.genre.GenreRepository
import modules.movieRelations.{MovieActorRepository, MovieDirectorRepository, MovieGenreRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import views.html
import modules.util._

import scala.concurrent.{ExecutionContext, Future}

class MovieController @Inject()(
    repo: MovieRepository,
    directorRepo: DirectorRepository,
    actorRepo: ActorRepository,
    genreRepo: GenreRepository,
    movieDirectorRepo: MovieDirectorRepository,
    movieActorRepo: MovieActorRepository,
    movieGenreRepo: MovieGenreRepository,
    cc: MessagesControllerComponents
)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val createMovieForm: Form[CreateMovieForm] = Form {
    mapping(
      "title" -> nonEmptyText,
      "description" -> nonEmptyText,
      "releaseDate" -> sqlDate,
      "country" -> of(CountryFormatter),
      "language" -> of(LanguageFormatter),
      "directors" -> seq(nonEmptyText),
      "actors" -> seq(nonEmptyText),
      "genres" -> seq(nonEmptyText)
    )(CreateMovieForm.apply)(CreateMovieForm.unapply)
  }

  val filterMovieForm: Form[FilterMovieForm] = Form {
    mapping(
      "title" -> nonEmptyText,
      "description" -> nonEmptyText,
      "releaseDate" -> optional(sqlDate),
      "country" -> of(CountryFormatter),
      "language" -> of(LanguageFormatter)
    )(FilterMovieForm.apply)(FilterMovieForm.unapply)
  }

  def index: Action[AnyContent] = Action { implicit request =>
    Ok(html.index())
  }

  def list(
      page: Int,
      pageSize: Int,
      title: String,
      description: String,
      releaseDate: String,
      country: Country.Value,
      language: Language.Value,
      orderBy: SortableField.Value,
      order: SortOrder.Value
  ): Action[AnyContent] = Action.async { implicit request =>
    val titleToRepo = if (title.trim().nonEmpty) Some(title) else None
    val descriptionToRepo = if (description.trim().nonEmpty) Some(description) else None
    repo.list(
      page,
      pageSize,
      titleToRepo,
      descriptionToRepo,
      parseDate(releaseDate),
      country,
      language,
      orderBy,
      order
    )
      .map(movies =>
        Ok(html.movie.list(
          movies,
          filterMovieForm.fill(
            FilterMovieForm(title, description, parseDate(releaseDate), country, language)
          ),
          SortItems(orderBy, order)
        ))
      )
  }

  def createMovie: Action[AnyContent] = Action.async {
    implicit request =>
      for {
        directors <- directorRepo.listAll
        actors <- actorRepo.listAll
        genres <- genreRepo.listAll
      } yield
        Ok(html.movie.create(
          createMovieForm,
          directors,
          actors,
          genres
        ))
  }

  def saveNewMovie: Action[AnyContent] = Action.async {
    implicit request =>
      createMovieForm.bindFromRequest.fold(
        formWithErrors => {
          for {
            directors <- directorRepo.listAll
            actors <- actorRepo.listAll
            genres <- genreRepo.listAll
          } yield
          Ok(html.movie.create(
            formWithErrors,
            directors,
            actors,
            genres,
          ))
        },
        newMovie => {
          repo.create(newMovie).map {
            _ => Redirect(routes.MovieController.list()).flashing("success" -> "Movie created")
          }
        }
      )
  }

  def delete(id: Long): Action[AnyContent] = Action.async {
    implicit request =>
      repo
        .findById(id)
        .map{
          case Some(_) =>
            val _ = repo.delete(id)
            Redirect(routes.MovieController.list()).flashing("success" -> "Movie deleted")
          case None => Redirect(routes.MovieController.list()).flashing("error" -> s"No movie with such id = $id")
      }
  }

  def edit(id: Long): Action[AnyContent] = Action.async {
    implicit request =>
      val relations = for {
        movieDirectors <- movieDirectorRepo.findDirectorsByMovieId(id)
        movieActors <- movieActorRepo.findActorsByMovieId(id)
        movieGenres <- movieGenreRepo.findGenresByMovieId(id)
      } yield (
        movieDirectors,
        movieActors,
        movieGenres
      )
      val movieTuple = for {
        movieOp <- repo.findById(id)
        movieRelations <- relations
      } yield (movieOp, movieRelations._1, movieRelations._2, movieRelations._3)
      movieTuple.flatMap {
        case (Some(movie), movieDirectors, movieActors, movieGenres) =>
          for {
            directors <- directorRepo.listAll
            actors <- actorRepo.listAll
            genres <- genreRepo.listAll
          } yield
            Ok(html.movie.create(
              createMovieForm
                .fill(
                  CreateMovieForm(
                    movie.title,
                    movie.description,
                    movie.releaseDate,
                    movie.country,
                    movie.language,
                    movieDirectors.map(directorId => directorId.toString),
                    movieActors.map(actorId => actorId.toString),
                    movieGenres.map(genreId => genreId.toString)
                  )
                ),
              directors,
              actors,
              genres,
              Some(id)
          ))
        case (None, _, _, _) =>
          for {
            directors <- directorRepo.listAll
            actors <- actorRepo.listAll
            genres <- genreRepo.listAll
          } yield
            Ok(html.movie.create(
              createMovieForm,
              directors,
              actors,
              genres
          ))
      }
  }

  def update(id: Long): Action[AnyContent] = Action.async {
    implicit request =>
      createMovieForm.bindFromRequest.fold(
        formWithErrors => {
          for {
            directors <- directorRepo.listAll
            actors <- actorRepo.listAll
            genres <- genreRepo.listAll
          } yield
          Ok(html.movie.create(
            formWithErrors,
            directors,
            actors,
            genres
          ))
        },
        newMovie => {
          repo.update(id, newMovie).map {
            _ => Redirect(routes.MovieController.list()).flashing("success" -> "Movie updated")
          }
        }
      )
  }
}
