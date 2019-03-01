package models

import javax.inject.{Inject, Singleton}
import modules.utility.database.ExtendedPostgresProfile
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PersonRepository @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider
)(
    implicit ec: ExecutionContext
) extends PersonTableComponent with HasDatabaseConfigProvider[ExtendedPostgresProfile] {
  import profile.api._

  def create(name: String, age: Int): Future[Person] = db.run {
    (people.map(p => (p.name, p.age))
      returning people.map(_.id)
      into ((nameAge, id) => Person(id, nameAge._1, nameAge._2))
    ) += (name, age)
  }

  def list(): Future[Seq[Person]] = db.run {
    people.result
  }
}
