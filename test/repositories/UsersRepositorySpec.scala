package repositories

import org.specs2.mutable.Specification
import models.database.{Schema, UserRecord}
import util.TestDatabaseConfigurationWithFixtures
import scala.slick.jdbc.JdbcBackend

class UsersRepositorySpec extends Specification {

  object repository extends TestDatabaseConfigurationWithFixtures with Schema
  with UsersRepositoryComponentImpl {

    import profile.simple._

    override def fixtures(implicit session: JdbcBackend#Session): Unit = {
      users ++= Seq(
        UserRecord(None, "user1", "password1", false, Some("fn"), Some("ln"), None),
        UserRecord(None, "user2", "password2", false, None, None, None)
      )
    }
  }

  import repository._
  import profile.simple._

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

      val user = usersRepository.getByRememberToken(token)

      user must beSome
    }

    "return None when user not found" in withTestDb { implicit session =>
      val user = usersRepository.getByRememberToken("user124234")

      user must beNone
    }
  }

  "user insertion" should {
    val toInsert = UserRecord(None, "user_to_insert", "password_to_insert")

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

  "user search" should {

    "search by login" in withTestDb { implicit session =>
      val records = usersRepository.findUserPaged("user1", 0, 10)

      records.size mustEqual 1
      records.head.username mustEqual "user1"
    }

    "search by last name" in withTestDb { implicit session =>
      val records = usersRepository.findUserPaged("ln", 0, 10)

      records.size mustEqual 1
      records.head.username mustEqual "user1"
    }

    "return all records for empty request" in withTestDb { implicit session =>
      val records = usersRepository.findUserPaged("", 0, 10)

      records.size mustEqual 2
    }

    "return paged results" in withTestDb { implicit session =>
      val records = usersRepository.findUserPaged("", 1, 1)

      records.size mustEqual 1
      records.head.username mustEqual "user2"
    }

    "return sorted results" in withTestDb { implicit session =>
      val records = usersRepository.findUserPaged("", 0, 10)

      records.size mustEqual 2
      records.head.username mustEqual "user1"
      records.tail.head.username mustEqual "user2"
    }
  }

  "user search count" should {

    "search by login" in withTestDb { implicit session =>
      usersRepository.countFindUser("user1") mustEqual 1
    }

    "search by last name" in withTestDb { implicit session =>
      usersRepository.countFindUser("ln") mustEqual 1
    }

    "count all records for empty request" in withTestDb { implicit session =>
      usersRepository.countFindUser("") mustEqual 2
    }
  }
}
