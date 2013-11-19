package org.jtalks.antarcticle.servlet

import org.scalatra._
import scala.slick.driver.H2Driver.simple._
import Database.threadLocalSession
import org.jtalks.antarcticle.persistence.repositories.ArticlesRepositoryComponent

class ArticlesServlet(val dal: ArticlesRepositoryComponent) extends BaseServlet {
  import dal._

  get("/articles") {
    val repo = articlesRepository
    val articles = repo.findAll
    jade("/articles.jade", "articles" -> articles)
  }

  get("/articles/:id") {
    val article = articlesRepository.get(params("id").toInt)

    article match {
      case Some(article) =>
        jade("/article.jade", "article" -> article)
      case None => NotFound
    }
  }
}

