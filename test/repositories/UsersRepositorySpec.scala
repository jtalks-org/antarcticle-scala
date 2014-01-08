package repositories

import org.specs2.mutable.Specification
import models.database.{UserToInsert, Schema}
import util.TestDatabaseConfiguration
import migrations.{MigrationTool, MigrationsContainer}

class UsersRepositorySpec extends Specification {
  object repository extends UsersRepositoryComponentImpl with MigrationTool
                                with Schema with TestDatabaseConfiguration {
    val migrationsContainer = new MigrationsContainer {}
  }

  import repository._
  import profile.simple._

  def populateDb(implicit session: Session) = {
    migrate

    val users = List(
      UserToInsert("user1", false, Some("fn"), Some("ln")),
      UserToInsert("user2")
    )

    val usersIds = Users.forInsert.insertAll(users : _*)
    (users, usersIds)
  }

  "update remember me token" should {
    "be updated" in withSession { implicit s: Session =>
      val (_, usersIds) = populateDb
      val userId = usersIds(0)
      val token = "4534"

      usersRepository.updateRememberToken(userId, token)

      val actualToken = Query(Users).filter(_.id === userId).map(_.rememberToken).first
      actualToken must_== token
    }
  }

  "get by username" should {
    "return user user1" in withSession { implicit session: Session =>
      val (users, usersIds) = populateDb

      val user = usersRepository.getByUsername("user1")

      user must beSome
    }

    "return None when user not found" in withSession { implicit session: Session =>
      populateDb

      val user = usersRepository.getByUsername("user124234")

      user must beNone
    }
  }

  "get by token" should {
    "return user with token 1234" in withSession { implicit session: Session =>
      val (_, usersIds) = populateDb
      val token = "1234"
      usersRepository.updateRememberToken(usersIds(0), token)

      val user = usersRepository.getByRemeberToken(token)

      user must beSome
    }

    "return None when user not found" in withSession { implicit session: Session =>
      populateDb

      val user = usersRepository.getByRemeberToken("user124234")

      user must beNone
    }
  }

  "user insertion" should {
    val toInsert = UserToInsert("user_to_insert")

    "create new user record" in withSession { implicit session: Session =>
      val (users, _) = populateDb
      val oldCount = users.size

      usersRepository.insert(toInsert)

      val newCount = Query(Users.length).first
      newCount must_== oldCount + 1
    }

    "assign id to new user" in withSession { implicit session: Session =>
      val (users, _) = populateDb

      val id: Int = usersRepository.insert(toInsert)
      true
    }
  }
}
