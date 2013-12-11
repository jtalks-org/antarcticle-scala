package org.jtalks.antarcticle.servlet

import org.jtalks.antarcticle.persistence.repositories.ArticlesRepositoryComponent

class ArticlesServlet(val dal: ArticlesRepositoryComponent) extends BaseServlet {
  import dal._

  get("/") {
    val repo = articlesRepository
    val articles = repo.getList(1,1) //TODO
    jade("/articles.jade", "articles" -> articles)
  }

  get("/articles") {
    val repo = articlesRepository
    val articles = repo.getList(1,1) // TODO
    jade("/articles.jade", "articles" -> articles)
  }

  get("/articles/:id") {
    val article = articlesRepository.get(params("id").toInt)

    article match {
      case Some(article) => jade("/article.jade", "article" -> article)
      case None => halt(404)
    }
  }
}

