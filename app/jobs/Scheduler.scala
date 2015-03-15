package jobs

import akka.actor.{Props, ActorSystem}
import jobs.actors.RemoveInactiveUsersActor
import org.joda.time.DateTime
import play.libs.Akka

trait Scheduler {

  def runJobs() = {
    import play.api.libs.concurrent.Execution.Implicits._
    import scala.concurrent.duration._

    val system: ActorSystem = Akka.system
    val userCleaner = system.actorOf(Props[RemoveInactiveUsersActor])
    system.scheduler.schedule(1.second, 24.hour, userCleaner, DateTime.now().minusHours(24))
  }
}
