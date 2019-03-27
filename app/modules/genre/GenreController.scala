package modules.genre

import javax.inject.Inject
import modules.util.SortOrder
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

  def list(
      page: Int,
      pageSize: Int,
      title: String,
      orderBy: SortableField.Value,
      order: SortOrder.Value
  ): Action[AnyContent] = Action.async { implicit request =>
    repo
      .list(page, pageSize, "%" + title + "%", orderBy, order)
      .map(genres => Ok(html.genre.list(genres, createGenreForm.fill(CreateGenreForm(title)), SortItems(orderBy, order))))
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

  def delete(id: Long): Action[AnyContent] = Action.async {
    implicit request =>
      repo
        .findById(id)
        .map{
          case Some(_) => {
            val _ = repo.delete(id)
            Redirect(routes.GenreController.list()).flashing("success" -> "Genre deleted")
          }
          case None => Redirect(routes.GenreController.list()).flashing("error" -> s"No genre with such id = $id")
        }
  }

  def edit(id: Long): Action[AnyContent] = Action.async {
    implicit request =>
      repo
        .findById(id)
        .map {
          case Some(genre) =>
            Ok(html.genre.create(
              createGenreForm
                .fill(new CreateGenreForm(genre.title)),
              Some(id)
            ))
          case None => Ok(html.genre.create(createGenreForm))
        }
  }

  def update(id: Long): Action[AnyContent] = Action.async {
    implicit request =>
      createGenreForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(Ok(html.genre.create(formWithErrors)))
        },
        newGenre => {
          repo.update(id, newGenre).map {
            _ => Redirect(routes.GenreController.list()).flashing("success" -> "Genre updated")
          }
        }
      )
  }
}
