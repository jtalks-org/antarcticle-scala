package org.jtalks.antarcticle

import org.scalatest.{Matchers, FunSuite, BeforeAndAfterAll}
import org.scalatra.test.ScalatraTests

trait ControllerSpec extends FunSuite with ScalatraTests with BeforeAndAfterAll with Matchers {
  override protected def beforeAll(): Unit = start()
  override protected def afterAll(): Unit = stop()
}
