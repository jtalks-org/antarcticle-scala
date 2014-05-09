package migrations

import models.database.{Schema, Profile}
import scala.slick.jdbc.meta.MTable
import scala.slick.jdbc.{StaticQuery => Q}
import play.api.Logger //tmp
import scala.slick.jdbc.JdbcBackend

trait Migration {
  val version: Int
  def run(implicit session: JdbcBackend#Session)
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

  def migrate(implicit session: JdbcBackend#Session) = {
    MTable.getTables(SCHEMA_VERSION_TABLE).list match {
      case Nil =>
        Logger.info("Migrating new database")
        createSchemaVersionTable
        val ddls = schema.map(_.ddl)
        ddls.tail.fold(ddls.head)(_ ++ _).create
        val version = migrations.lastOption.map(_.version) getOrElse 0
        setInitialVersion(version)
        Logger.info(s"Migration completed (version: $version)")
      case xs =>
        val currentVersion = getCurrentVersion
        val notPerformedMigrations = migrations.filter(_.version > currentVersion)
        if (!notPerformedMigrations.isEmpty) {
          val newVersion = notPerformedMigrations.last.version
          Logger.info(s"Migrating database from version $currentVersion to $newVersion")
          notPerformedMigrations.foreach(_.run)
          updateVersion(newVersion)
          Logger.info(s"Migration completed")
        }
    }
  }

  private def migrations = {
    migrationsContainer.getMigrations.toList.sortBy(_.version)
  }

  def getCurrentVersion(implicit s: JdbcBackend#Session) = {
     Q.queryNA[Int](s"select $VERSION_COLUMN from $SCHEMA_VERSION_TABLE").first
  }

  private def setInitialVersion(version: Int)(implicit s: JdbcBackend#Session) = {
    Q.updateNA(s"insert into $SCHEMA_VERSION_TABLE ($VERSION_COLUMN) values ($version)").execute
  }

  private def updateVersion(version: Int)(implicit s: JdbcBackend#Session) = {
    Q.updateNA(s"update $SCHEMA_VERSION_TABLE set $VERSION_COLUMN=$version").execute
  }

  private def createSchemaVersionTable(implicit s: JdbcBackend#Session) = {
    Q.updateNA(s"create table $SCHEMA_VERSION_TABLE(" +
      s"$VERSION_COLUMN int not null)").execute
  }
}
