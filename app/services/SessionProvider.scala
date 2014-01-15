package services

import scala.slick.jdbc.JdbcBackend

trait SessionProvider {
  def withSession[T](f: JdbcBackend#Session => T): T
  def withTransaction[T](f: JdbcBackend#Session => T): T
}

trait SlickSessionProvider extends SessionProvider {
  val db: JdbcBackend#Database

  override def withSession[T](f: JdbcBackend#Session => T): T = db.withSession(session => f(session))
  override def withTransaction[T](f: JdbcBackend#Session => T): T = db.withTransaction(session => f(session))
}
