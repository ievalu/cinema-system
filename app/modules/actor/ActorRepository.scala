package modules.actor

import java.sql.Date

import javax.inject.{Inject, Singleton}
import modules.util.Page
import modules.utility.database.ExtendedPostgresProfile
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.lifted.ColumnOrdered

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ActorRepository @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
) (
    implicit ec: ExecutionContext
) extends ActorTableComponent with HasDatabaseConfigProvider[ExtendedPostgresProfile] {
  import profile.api._

  private val logger = Logger(this.getClass)

  def filterLogic(
      name: String = "%",
      birthDate: Option[Date],
      heightMin: Int,
      heightMax: Int
  ) = {
    val nameArr = name.toLowerCase.split(" ")
    val firstQuery = actors
      .filter(
        actor => nameArr.length match {
          case 1 => {
            (actor.firstName.toLowerCase like nameArr(0)) ||
              (actor.lastName.toLowerCase like nameArr(0))
          }
          case 2 => {
            (actor.firstName.toLowerCase like nameArr(0)) ||
              (actor.lastName.toLowerCase like nameArr(0)) ||
              (actor.firstName.toLowerCase like nameArr(1)) ||
              (actor.lastName.toLowerCase like nameArr(1))
          }
        }
      )
      .filter(actor => actor.height >= heightMin && actor.height <= heightMax)
    val dateFilteredQuery = birthDate match {
      case Some(date) => firstQuery.filter(actor => actor.birthDate === date)
      case None => firstQuery
    }
    dateFilteredQuery
  }

  def sortLogic(
      actorTable: ActorTable, //Query[ActorRepository.this.ActorTable, ActorRepository.this.ActorTable#TableElementType, Seq],
      orderBy: SortableField.Value,
      order: SortOrder.Value
  ) = {
    (orderBy, order) match {
      case (SortableField.name, SortOrder.asc) => actorTable.firstName.toLowerCase.asc
      case (SortableField.name, SortOrder.desc) => actorTable.firstName.toLowerCase.desc
      case (SortableField.birthDate, SortOrder.asc) => actorTable.birthDate.asc
      case (SortableField.birthDate, SortOrder.desc) => actorTable.birthDate.desc
      case (SortableField.height, SortOrder.asc) => actorTable.height.asc
      case (SortableField.height, SortOrder.desc) => actorTable.height.desc
      case _ => actorTable.id.asc
    }
  }

  def count(
      name: String = "%",
      birthDate: Option[Date],
      heightMin: Int,
      heightMax: Int
  ): Future[Int] = db.run(filterLogic(name, birthDate, heightMin, heightMax).length.result)

  def list(
      page: Int = 1,
      pageSize: Int = 8,
      name: String = "%",
      birthDate: Option[Date],
      heightMin: Int,
      heightMax: Int,
      orderBy: SortableField.Value,
      order: SortOrder.Value
  ): Future[Page[Actor]] = {
    val offset = (page - 1) * pageSize
    val filteredQuery = filterLogic(name, birthDate, heightMin, heightMax)
    val sortedQuery = filteredQuery.sortBy{ a => sortLogic(a, orderBy, order) }
    for {
      totalRows <- count(name, birthDate, heightMin, heightMax)
      actorList <- db.run(
        sortedQuery
          .drop(offset)
          .take(pageSize)
          .result
      )
    } yield Page(actorList, page, offset, totalRows)
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
