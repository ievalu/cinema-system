package modules.actor

import java.sql.Date

import javax.inject.{Inject, Singleton}
import modules.util.{Country, Gender, Page, SortOrder}
import modules.utility.database.ExtendedPostgresProfile
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

@Singleton
class ActorRepository @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
) (
    implicit ec: ExecutionContext
) extends ActorTableComponent with HasDatabaseConfigProvider[ExtendedPostgresProfile] {
  import profile.api._

  private val logger = Logger(this.getClass)

  def filterLogic(
      name: Option[String],
      birthDate: Option[Date],
      nationality: Country.Value,
      heightMin: Int,
      heightMax: Int,
      gender: Gender.Value
  ) = {

    val firstQuery =
      name.map {
        nameVal =>
          actors.filter(
            actor =>
            nameVal.split(" ")
              .map { word =>
                actor.firstName.ilike("%" + word + "%") || actor.lastName.ilike("%" + word + "%")
              }
            .reduceLeftOption(_ || _)
            .getOrElse(true: Rep[Boolean])
          )
      }.getOrElse(actors)
      .filter(actor => actor.height >= heightMin && actor.height <= heightMax)
    val dateFilteredQuery = birthDate match {
      case Some(date) => firstQuery.filter(actor => actor.birthDate === date)
      case None => firstQuery
    }
    val nationalityFilteredQuery = nationality match {
      case Country.NoCountry => dateFilteredQuery
      case _ => dateFilteredQuery.filter(actor => actor.nationality === nationality)
    }
    val genderFilteredQuery = gender match {
      case Gender.Other => nationalityFilteredQuery
      case _ => nationalityFilteredQuery.filter(actor => actor.gender === gender)
    }
    genderFilteredQuery
  }

  def sortLogic(
      actorTable: ActorTable, //Query[ActorRepository.this.ActorTable, ActorRepository.this.ActorTable#TableElementType, Seq],
      orderBy: SortableField.Value,
      order: SortOrder.Value
  ) = {
    val nameSort = actorTable.firstName.toLowerCase
    val dateSort = actorTable.birthDate
    val nationalitySort = {
      Country.values.drop(1)
        .foldLeft {
          Case
            .If(actorTable.nationality === Country.values.head)
            .Then(Some(Country.values.head.nationality): Rep[Option[String]])
        } {
          case(acc, enum) =>
            acc.If(actorTable.nationality === enum).Then(Some(enum.nationality): Rep[Option[String]])
        }
        .Else(Option.empty[String]: Rep[Option[String]])
    }
    val heightSort = actorTable.height
    (orderBy, order) match {
      case (SortableField.name, SortOrder.asc) => nameSort.asc
      case (SortableField.name, SortOrder.desc) => nameSort.desc
      case (SortableField.birthDate, SortOrder.asc) => dateSort.asc
      case (SortableField.birthDate, SortOrder.desc) => dateSort.desc
      case (SortableField.nationality, SortOrder.asc) => nationalitySort.asc
      case (SortableField.nationality, SortOrder.desc) => nationalitySort.desc
      case (SortableField.height, SortOrder.asc) => heightSort.asc
      case (SortableField.height, SortOrder.desc) => heightSort.desc
      case _ => actorTable.id.asc
    }
  }

  def count(
      name: Option[String],
      birthDate: Option[Date],
      nationality: Country.Value,
      heightMin: Int,
      heightMax: Int,
      gender: Gender.Value
  ): Future[Int] = db.run(filterLogic(name, birthDate, nationality, heightMin, heightMax, gender).length.result)

  def list(
      page: Int = 1,
      pageSize: Int = 8,
      name: Option[String],
      birthDate: Option[Date],
      nationality: Country.Value,
      heightMin: Int,
      heightMax: Int,
      gender: Gender.Value,
      orderBy: SortableField.Value,
      order: SortOrder.Value
  ): Future[Page[Actor]] = {
    val offset = (page - 1) * pageSize
    val filteredQuery = filterLogic(name, birthDate, nationality, heightMin, heightMax, gender)
    val sortedQuery = filteredQuery.sortBy{ a => sortLogic(a, orderBy, order) }
    for {
      totalRows <- count(name, birthDate, nationality, heightMin, heightMax, gender)
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
    )) returning actors
      into ((_, actor) =>
        actor)) +=
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
