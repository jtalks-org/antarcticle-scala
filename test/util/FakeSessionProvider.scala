package util

import scala.slick.jdbc.JdbcBackend.Session
import services.SessionProvider

object FakeSessionProvider {
  val FakeSessionValue: Session = null
}

trait FakeSessionProvider extends SessionProvider {
  import FakeSessionProvider._
  def withSession[T](f: Session => T): T = f(FakeSessionValue)
  def withTransaction[T](f: Session => T): T = f(FakeSessionValue)
}

import org.specs2.mock.Mockito

trait MockSession {
  this: Mockito =>

  val session = mock[Session]

  trait MockSessionProvider extends SessionProvider {
    def withSession[T](f: Session => T): T = f(session)
    def withTransaction[T](f: Session => T): T = f(session)
  }
}
