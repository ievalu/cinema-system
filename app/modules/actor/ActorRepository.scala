package modules.actor

import javax.inject.{Inject, Singleton}
import modules.utility.database.ExtendedPostgresProfile
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ActorRepository @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
) (
    implicit ec: ExecutionContext
) extends ActorTableComponent with HasDatabaseConfigProvider[ExtendedPostgresProfile] {
  import profile.api._

  private val logger = Logger(this.getClass)

  def list(): Future[Seq[Actor]] = db.run {
    actors.result
  }

  def findById(id: Long): Future[Option[Actor]] = db.run {
    actors.filter(a => a.id === id).result.headOption
  }

  def create(newActor: CreateActorForm): Future[Actor] = db.run {
    (actors.map(a => (
      a.firstName,
      a.lastName,
      a.birthDate,
      a.nationality,
      a.height,
      a.gender
    )) returning actors.map(_.id)
      into ((actorForm, id) =>
      Actor(
        id,
        actorForm._1,
        actorForm._2,
        actorForm._3,
        actorForm._4,
        actorForm._5,
        actorForm._6
      ))) +=
      (
        newActor.firstName,
        newActor.lastName,
        newActor.birthDate,
        newActor.nationality,
        newActor.height,
        newActor.gender)
  }

  def delete(id: Long): Future[Int] = db.run {
    actors.filter(a => a.id === id).delete
  }

  def update(id: Long, newActor: CreateActorForm): Future[Actor] = {
    db.run {
      (actors returning actors)
        .insertOrUpdate(
          Actor(
            id,
            newActor.firstName,
            newActor.lastName,
            newActor.birthDate,
            newActor.nationality,
            newActor.height,
            newActor.gender)
        )
    }.map {
      case Some(actor) =>
        logger.debug(s"Created new actor $actor")
        actor
      case None =>
        logger.debug(s"Updated existing actor $newActor")
        Actor(
          id,
          newActor.firstName,
          newActor.lastName,
          newActor.birthDate,
          newActor.nationality,
          newActor.height,
          newActor.gender)
    }
  }
}
