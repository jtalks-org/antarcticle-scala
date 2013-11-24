package org.jtalks.antarcticle.servlet

import org.jtalks.antarcticle.persistence.repositories.ArticlesRepositoryComponent
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.jtalks.antarcticle.models.{ArticleListModel, UserModel}
import org.jtalks.antarcticle.ControllerSpec

class ArticlesServletTest extends ControllerSpec with MockitoSugar {

  val testDal = new ArticlesRepositoryComponent  {
    val repo = mock[ArticlesRepository]
    val articlesRepository = repo
  }

  val article = ArticleListModel(1, "first article content", "content", new java.util.Date(), UserModel(1, "user1"))

  addServlet(new ArticlesServlet(testDal), "/*")

  test("get all articles") {
    when(testDal.articlesRepository.findAll).thenReturn(List(article))
    get("/articles") {
      status should equal (200)
      body should include ("First template")
      body should include (article.content)
    }
  }

  test("get article") {
    val id = 1
    when(testDal.articlesRepository.get(id)).thenReturn(Some(article))
    get(s"/articles/$id") {
      status should equal (200)
      body should include (article.title)
    }
  }


  test("get not existing article") {
    val id = 1
    when(testDal.articlesRepository.get(id)).thenReturn(None)
    get(s"/articles/$id") {
      status should equal (404)
    }
  }

}
