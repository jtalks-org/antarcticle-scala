package util

import scala.slick.session.Session
import services.SessionProvider

object FakeSessionProvider {
  val FakeSessionValue: Session = null
}

trait FakeSessionProvider extends SessionProvider {
  import FakeSessionProvider._
  def withSession[T](f: Session => T): T = f(FakeSessionValue)
  def withTransaction[T](f: Session => T): T = f(FakeSessionValue)
}