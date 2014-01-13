package migrations

import org.specs2.specification.BeforeExample
import org.mockito.Matchers
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import scala.slick.jdbc.meta.MTable
import scala.slick.jdbc.JdbcBackend.Session
import util.TestDatabaseConfiguration
import scala.slick.jdbc.{StaticQuery => Q}
import models.database.Schema
import scala.slick.lifted.AbstractTable

class MigrationToolSpec extends Specification
              with Mockito with BeforeExample {

  val mockContainer = mock[MigrationsContainer]
  val mockMigration1 = mock[Migration]
  val mockMigration2 = mock[Migration]
  val mockMigration3 = mock[Migration]

  def before = {
    org.mockito.Mockito.reset(mockContainer)
    org.mockito.Mockito.reset(mockMigration1)
    org.mockito.Mockito.reset(mockMigration2)
    org.mockito.Mockito.reset(mockMigration3)
  }

  "migrations container" should {
    "return migrations list" in {
      object migrationsContainer extends MigrationsContainer {
        val m1 = new Migration {
          val version = 1
          def run(implicit session: Session): Unit = {}
        }

        val m2 = new Migration {
          val version = 2
          def run(implicit session: Session): Unit = {}
        }
      }

      migrationsContainer.getMigrations must have size 2
    }

    "return empty list when no migrations found" in {
      object emptyMigrationsContainer extends MigrationsContainer

      emptyMigrationsContainer.getMigrations must be empty
    }
  }

  "migrations tool" should {
    object tool extends MigrationTool with Schema with TestDatabaseConfiguration {
      val migrationsContainer = mockContainer
    }

    import tool._
    import profile.simple._
    import scala.slick.jdbc.JdbcBackend.Session

    "new database migration" should {
      "create version table when not exists" in withSession { implicit s: Session =>
        migrationsContainer.getMigrations returns Seq()

        tool.migrate

        MTable.getTables("schema_version").list must not be empty
      }

      "create schema" in withSession { implicit s: Session =>
        migrationsContainer.getMigrations returns Seq()

        tool.migrate

        def tableExists(table: TableQuery[_]) = !MTable.getTables(table.baseTableRow.asInstanceOf[AbstractTable[_]].tableName).list.isEmpty
        val allCreated = schema.forall(tableExists)
        allCreated must beTrue
      }

      "set schema version to 0" in withSession { implicit s: Session =>
        migrationsContainer.getMigrations returns Seq()

        tool.migrate

        tool.getCurrentVersion must_== 0
      }
    }

    "existing database migration" should {
      def setupMocks = {
        mockMigration1.version returns 1
        mockMigration2.version returns 2
        mockMigration3.version returns 3
        migrationsContainer.getMigrations returns Seq(mockMigration1, mockMigration3, mockMigration2)
      }

      "run all migrations in correct order when schema version is 0" in withSession { implicit s: Session =>
        setupMocks

        tool.migrate // create schema
        // artificially set schema version to 0
        Q.updateNA(s"update schema_version set current_version=0").execute
        tool.migrate

        there was one(mockMigration1).run(any[Session]) andThen
                  one(mockMigration2).run(any[Session]) andThen
                  one(mockMigration3).run(any[Session])
      }

      "run migrations starting from version 3 when schema version is 2" in withSession { implicit s: Session =>
        setupMocks

        tool.migrate // create schema
        // artificially set schema version to 2
        Q.updateNA(s"update schema_version set current_version=2").execute
        tool.migrate

        there was one(mockMigration3).run(any[Session])
        there was no(mockMigration1).run(any[Session])
        there was no(mockMigration2).run(any[Session])
      }

      "update schema version to 1 when db is new, but migration exists" in withSession { implicit s: Session =>
        mockMigration1.version returns 1
        migrationsContainer.getMigrations returns Seq(mockMigration1)

        tool.migrate

        tool.getCurrentVersion must_== 1
        there was no(mockMigration1).run(any[Session])
      }

      "not do anything when no new migrations" in withSession { implicit s: Session =>
        setupMocks

        tool.migrate // create schema
        // artificially set schema version to 3
        Q.updateNA(s"update schema_version set current_version=3").execute
        tool.migrate

        there was no(mockMigration1).run(any[Session])
        there was no(mockMigration2).run(any[Session])
        there was no(mockMigration3).run(any[Session])
      }

      "update schema version" in withSession { implicit s: Session =>
        setupMocks

        tool.migrate // create schema
        tool.migrate

        tool.getCurrentVersion must_== 3
      }
    }
  }
}

