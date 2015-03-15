package jobs.actors

import java.sql.Timestamp

import akka.actor.{Actor, ActorLogging}
import com.github.nscala_time.time.StaticDateTimeFormat
import conf.ConfigurationComponent
import models.database.Schema
import org.joda.time.DateTime
import repositories._
import services.SessionProvider

class RemoveInactiveUsersActor extends InactiveUserRemover with Actor with ActorLogging with ActorDependencies {
  override def receive: Receive = {
    case time: DateTime ⇒ execute(time)
  }
}

trait InactiveUserRemover {
  this: UsersRepositoryComponent with SessionProvider with ActorLogging =>

  def execute(time: DateTime) = {
    log.info("Deleting inactive users  created before " + time.toString(StaticDateTimeFormat.fullDateTime()))
    withSession { implicit session =>
      usersRepository.deleteInactiveUsers(new Timestamp(time.getMillis))
    }
  }
}

trait ActorDependencies extends ConfigurationComponent with Schema with UsersRepositoryComponentImpl {}
