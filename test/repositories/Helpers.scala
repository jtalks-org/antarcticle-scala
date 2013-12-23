//package test.repositories
//
//import play.api.test._
//import play.api.test.Helpers._
//import play.api.db.slick.DB
//import slick.session.Session
//import play.api.Play.current
//import models.database.{Users, Articles}
//
//object Helpers {
//  def inMemorySession[T](t: (Session) => T) = {
//    running(new FakeApplication(additionalConfiguration = inMemoryDatabase())) {
//      val db = DB
//      import db.driver.profile.simple._
//
//      db.withSession { implicit session: Session =>
//        (Articles.ddl ++ Users.ddl).create
//        t(session)
//      }
//    }
//  }
//
//  def fixtures() = {
//
//  }
//}
