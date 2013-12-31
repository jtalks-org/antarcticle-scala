package migrations

import models.database.{Schema, Profile}
import scala.slick.session.Session
import scala.slick.jdbc.meta.MTable
import scala.slick.jdbc.{StaticQuery => Q}
import play.api.Logger //tmp

trait Migration {
  val version: Int
  def run(implicit session: Session)
}

trait MigrationsContainer {
  def getMigrations: Iterable[Migration] = {
    import scala.reflect.runtime.{universe => ru}
    import ru._

    val runtimeMirror = ru.runtimeMirror(this.getClass.getClassLoader)
    val instanceMirror = runtimeMirror.reflect(this)
    val members = instanceMirror.symbol.typeSignature.members
    val migrationVals = members.collect {
      case field if field.typeSignature <:< typeOf[Migration] => field.asTerm
    }

    migrationVals.map(instanceMirror.reflectField(_).get.asInstanceOf[Migration])
  }
}

trait MigrationTool {
  this: Schema with Profile =>

  import profile.simple._

  val migrationsContainer: MigrationsContainer

  private val SCHEMA_VERSION_TABLE = "schema_version"
  private val VERSION_COLUMN = "current_version"

  def migrate(implicit session: Session) = {
    MTable.getTables(SCHEMA_VERSION_TABLE).list match {
      case Nil =>
        Logger.info("Migrating new database")
        createSchemaVersionTable
        val ddls = schema.map(_.ddl)
        ddls.tail.fold(ddls.head)(_ ++ _).create
        setInitialVersion
        Logger.info(s"Migration completed")
      case xs =>
        val migrations = migrationsContainer.getMigrations.toList.sortBy(_.version)
        val currentVersion = getCurrentVersion
        val notPerformedMigrations = migrations.drop(currentVersion)
        val newVersion = notPerformedMigrations.last.version
        Logger.info(s"Migrating database from version $currentVersion to $newVersion")
        notPerformedMigrations.foreach(_.run)
        updateVersion(newVersion)
        Logger.info(s"Migration completed")
    }
  }

  def getCurrentVersion(implicit s: Session) = {
     Q.queryNA[Int](s"select $VERSION_COLUMN from $SCHEMA_VERSION_TABLE").first
  }

  private def setInitialVersion(implicit s: Session) = {
    Q.updateNA(s"insert into $SCHEMA_VERSION_TABLE ($VERSION_COLUMN) values (0)").execute
  }

  private def updateVersion(version: Int)(implicit s: Session) = {
    Q.updateNA(s"update $SCHEMA_VERSION_TABLE set $VERSION_COLUMN=$version").execute
  }

  private def createSchemaVersionTable(implicit s: Session) = {
    Q.updateNA(s"create table $SCHEMA_VERSION_TABLE(" +
      s"$VERSION_COLUMN int not null)").execute
  }
}
