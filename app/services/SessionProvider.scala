package services

import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.jdbc.JdbcBackend.Session

trait SessionProvider {
  def withSession[T](f: Session => T): T
  def withTransaction[T](f: Session => T): T
}

trait SlickSessionProvider extends SessionProvider {
  val db: Database

  override def withSession[T](f: Session => T): T = db.withSession(session => f(session))
  override def withTransaction[T](f: Session => T): T = db.withTransaction(session => f(session))
}
