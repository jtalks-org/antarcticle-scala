package jobs

import akka.actor.Props
import conf.Keys.DeleteInactiveUsers
import conf.PropertiesProviderComponent
import jobs.actors.RemoveInactiveUsersActor
import org.joda.time.DateTime
import play.api.Logger
import services.ActorSystemProvider

trait Scheduler {
  this: PropertiesProviderComponent with ActorSystemProvider =>

  def runJobs() = {
    import play.api.libs.concurrent.Execution.Implicits._

    import scala.concurrent.duration._

    propertiesProvider.get(DeleteInactiveUsers) match {
      case Some(true) =>
        Logger.info("Inactive user will be removed every 24 hours")
        val userCleaner = actorSystem.actorOf(Props[RemoveInactiveUsersActor])
        actorSystem.scheduler.schedule(1.second, 24.hour, userCleaner, DateTime.now().minusHours(24))
      case _ => Logger.info("Inactive users are never removed from database")
    }
  }
}
