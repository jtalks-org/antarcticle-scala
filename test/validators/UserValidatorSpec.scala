package validators

import models.UserModels.User
import org.specs2.mutable.Specification
import org.specs2.scalaz.ValidationMatchers
import org.specs2.specification.core.Fragment

class UserValidatorSpec extends Specification with ValidationMatchers {

  val validator = new UserValidator

  val validUser = User("fakeUser", "email@email.net", "P@$$w0Rd")

  "user validator should accept user with username" in {
    val usernames = List("dmitry", "usernameWith_25characters", "a")
    Fragment.foreach(usernames.map(input => validUser.copy(username=input))) { user =>
      s"${user.username}" ! {
        validator.validate(user) must beSuccessful
      }
    }
  }

  "user validator should not accept user with not valid username" in {
    val usernames = List("", "username_With_26characters")
    Fragment.foreach(usernames.map(input => validUser.copy(username=input))) { user =>
      s"${user.username}" ! {
        validator.validate(user) must beFailing
      }
    }
  }

  "user validator should accept user with password" in {
    val passwords = List("1", "password", "password_with_50_characters_should_be_accepted_by_")
    Fragment.foreach(passwords.map(input => validUser.copy(password=input))) { user =>
      s"${user.password}" ! {
        validator.validate(user) must beSuccessful
      }
    }
  }

  "user validator should not accept user with not valid password" in {
    val passwords = List("", "password_with_51_characters_should_not_be_accepted_")

    Fragment.foreach(passwords.map(input => validUser.copy(password=input))) { user =>
      s"${user.password}" ! {
        validator.validate(user) must beFailing
      }
    }
  }

  "user validator should accept user with email" in {
    val emails = List("valid@email.yes", "also.valid@email.yes", "and@this.also.valid.com")
    Fragment.foreach(emails.map(input => validUser.copy(email=input))) { user =>
      s"${user.email}" ! {
        validator.validate(user) must beSuccessful
      }
    }
  }

  "user validator should not accept user with not valid email" in {
    val emails = List("", "wrew@fsf", "@dfds.ru")
    Fragment.foreach(emails.map(input => validUser.copy(email=input))) { user =>
      s"${user.email}" ! {
        validator.validate(user) must beFailing
      }
    }
  }
}
