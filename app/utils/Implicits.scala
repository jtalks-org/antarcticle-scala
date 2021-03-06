package utils

import java.sql.Timestamp
import org.joda.time.DateTime
import scalaz._
import scala.language.implicitConversions
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object Implicits {
  // convert java.sql.Timestamp to joda
  implicit def timestampToDateTime(ts: Timestamp): DateTime = new DateTime(ts.getTime)
  // convert joda to java.sql.Timestamp
  implicit def dateTimeToTimestamp(dt: DateTime): Timestamp = new Timestamp(dt.getMillis)

  /**
   * Scalaz monad instance for Future
   */
  implicit val futureMonad = new Monad[Future] {
    override def point[A](a: => A): Future[A] =
      Future(a)

    override def bind[A, B](fa: Future[A])(f: A => Future[B]): Future[B] =
      fa.flatMap(f)
  }
}
