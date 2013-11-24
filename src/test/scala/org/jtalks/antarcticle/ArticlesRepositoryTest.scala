package org.jtalks.antarcticle

import org.jtalks.antarcticle.persistence.repositories.SlickArticlesRepositoryComponent
import org.jtalks.antarcticle.persistence._
import java.sql.Timestamp
import org.jtalks.antarcticle.persistence.schema.{User, Article}
import org.jtalks.antarcticle.models.{ArticleListModel, UserModel}
import org.scalatest.OptionValues


class ArticlesRepositoryTest extends RepositorySpec with OptionValues {

  val repository = new SlickArticlesRepositoryComponent
        with TestDbProvider
        with Schema

  override def schema = repository

  import repository._
  import profile.simple._

  describe("findAll") {
    it("returns all articles") { implicit session: Session =>
      val time = new Timestamp(new java.util.Date().getTime())

      Users.insertAll(
        User(None, "user1"),
        User(None, "user2")
      )

      Articles.insertAll(
        Article(None, "New title", "<b>content</b>", time, 1),
        Article(None, "New title 2", "<i>html text</i>", time, 2)
      )

      val articles = articlesRepository.findAll
      articles should contain (ArticleListModel(1, "New title", "<b>content</b>", time, UserModel(1,"user1")))
    }

    it("returns empty list when there are no articles") { implicit session: Session =>
      val articles = articlesRepository.findAll
      articles should be (empty)
    }
  }

  describe("get") {
    it("returns article by id when article exists") { implicit session: Session =>
      val time = new Timestamp(new java.util.Date().getTime())

      Users.insertAll(
        User(None, "user1"),
        User(None, "user2")
      )

      Articles.insertAll(
        Article(None, "New title", "<b>content</b>", time, 1),
        Article(None, "New title 2", "<i>html text</i>", time, 2)
      )

      val article = articlesRepository.get(1)
      article.value should be (ArticleListModel(1, "New title", "<b>content</b>", time, UserModel(1,"user1")))
    }

    it("returns None when article not exists") { implicit session: Session =>
      val article = articlesRepository.get(1)
      article should be (None)
    }
  }

}
