package util

import scala.slick.jdbc.JdbcBackend
import services.SessionProvider

object FakeSessionProvider {
  val FakeSessionValue: JdbcBackend#Session = null
}

/**
 * Provider with predefined constant value for session.
 * Useful when you don't need to assert something on Session object.
 **/
trait FakeSessionProvider extends SessionProvider {
  import FakeSessionProvider._
  def withSession[T](f: JdbcBackend#Session => T): T = f(FakeSessionValue)
  def withTransaction[T](f: JdbcBackend#Session => T): T = f(FakeSessionValue)
}

import org.specs2.mock.Mockito

/**
 * Provides MockSessionProvider trait to extend, for cases when you need
 * expectations on Session object.
 **/
trait MockSession {
  this: Mockito =>

  val session = mock[JdbcBackend#Session]

  trait MockSessionProvider extends SessionProvider {
    def withSession[T](f: JdbcBackend#Session => T): T = f(session)
    def withTransaction[T](f: JdbcBackend#Session => T): T = f(session)
  }
}
