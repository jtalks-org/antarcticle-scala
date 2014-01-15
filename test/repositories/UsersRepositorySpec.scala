package repositories

import org.specs2.mutable.Specification
import models.database.{Schema, UserRecord}
import util.TestDatabaseConfiguration
import migrations.{MigrationTool, MigrationsContainer}
import org.specs2.specification.BeforeExample
import scala.slick.jdbc.JdbcBackend

class UsersRepositorySpec extends Specification {
  object repository extends TestDatabaseConfiguration with Schema
            with MigrationTool with UsersRepositoryComponentImpl {
    val migrationsContainer = new MigrationsContainer {}
  }

  import repository._
  import profile.simple._

  def populateDb(implicit session: JdbcBackend#Session) = {
    migrate

    users ++= Seq(
      UserRecord(None, "user1", false, Some("fn"), Some("ln"), None),
      UserRecord(None, "user2", false, None, None, None)
    )
  }

  def withTestDb[T](f: Session => T) = withSession { implicit s =>
    populateDb
    f(s)
  }

  "update remember me token" should {
    "be updated" in withTestDb { implicit s =>
      val userId = 1
      val token = "4534"

      usersRepository.updateRememberToken(userId, token)

      val actualToken = users.filter(_.id === userId).map(_.rememberToken).first
      actualToken must_== token
    }
  }

  "get by username" should {
    "return user user1" in withTestDb { implicit session =>
      val user = usersRepository.getByUsername("user1")

      user must beSome
    }

    "return None when user not found" in withTestDb { implicit session =>
      val user = usersRepository.getByUsername("user124234")

      user must beNone
    }
  }

  "get by token" should {
    "return user with token 1234" in withTestDb { implicit session =>
      val token = "1234"
      val userId = 1
      usersRepository.updateRememberToken(userId, token)

      val user = usersRepository.getByRemeberToken(token)

      user must beSome
    }

    "return None when user not found" in withTestDb { implicit session =>
      val user = usersRepository.getByRemeberToken("user124234")

      user must beNone
    }
  }

  "user insertion" should {
    val toInsert = UserRecord(None, "user_to_insert")

    "create new user record" in withTestDb { implicit session =>
      val oldCount = users.length.run

      usersRepository.insert(toInsert)

      val newCount = users.length.run
      newCount must_== oldCount + 1
    }

    "assign id to new user" in withTestDb { implicit session =>
      val id: Int = usersRepository.insert(toInsert)
      true
    }
  }
}
