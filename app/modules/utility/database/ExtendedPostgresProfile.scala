package modules.utility.database

import com.github.tminglei.slickpg._

trait ExtendedPostgresProfile extends ExPostgresProfile
  with PgArraySupport
  with PgDate2Support
  with PgRangeSupport
  with PgHStoreSupport
  with PgPlayJsonSupport
  with PgSearchSupport
  with PgNetSupport
  with PgLTreeSupport {

  def pgjson = "jsonb"

  override val api = new API with ArrayImplicits
    with DateTimeImplicits
    with PlayJsonImplicits
    with NetImplicits
    with LTreeImplicits
    with RangeImplicits
    with HStoreImplicits
    with SearchImplicits
    with SearchAssistants {
  }
}

object ExtendedPostgresProfile extends ExtendedPostgresProfile
