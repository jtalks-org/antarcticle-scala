package org.jtalks.antarcticle.servlet

import org.scalatra.test.scalatest.ScalatraSuite
import org.scalatest.FunSuite
import org.jtalks.antarcticle.persistence.repositories.ArticlesRepositoryComponent
import org.jtalks.antarcticle.persistence.{UserModel, ArticleListModel}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._

class ArticlesServletTest extends ScalatraSuite with FunSuite with MockitoSugar {

  val testDal = new ArticlesRepositoryComponent  {
    val repo = mock[ArticlesRepository]
    val articlesRepository = repo
  }

  addServlet(new ArticlesServlet(testDal), "/*")

  test("get all articles") {
    when(testDal.articlesRepository.findAll).thenReturn(List(ArticleListModel(1, "first article content", "content", new java.util.Date(), UserModel(1, "user1"))))
    get("/articles") {
      status should equal (200)
      body should include ("First template")
      body should include ("first article content")
    }
  }

}
