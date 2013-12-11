package org.jtalks.antarcticle.services

import org.jtalks.antarcticle.persistence.repositories.ArticlesRepositoryComponent
import org.jtalks.antarcticle.persistence.schema.{ArticleToUpdate, UserRecord, ArticleRecord}
import java.util.Date
import java.sql.Timestamp
import org.jtalks.antarcticle.models.ArticleModels.{ArticleListModel, ArticleDetailsModel}
import org.jtalks.antarcticle.models.UserModel
import org.jtalks.antarcticle.util.DateImplicits._
import org.joda.time.DateTime

// article form
case class Article(id: Option[Int] = None, title: String, content: String, tags: Seq[String]) {
  //TODO: strip tags
  lazy val description = content.take(300)
}

trait ArticlesServiceComponent {
  val articlesService: ArticlesService

  trait ArticlesService {
    def getPage(page: Int): List[ArticleListModel]
    def createArticle(article: Article): ArticleDetailsModel
    def get(id: Int): Option[ArticleDetailsModel]
    def updateArticle(article: Article)
    def removeArticle(id: Int)
  }
}


trait ArticlesServiceComponentImpl extends ArticlesServiceComponent {
  this: ArticlesRepositoryComponent =>

  val articlesService = new ArticlesServiceImpl

  class ArticlesServiceImpl extends ArticlesService {
    def createArticle(article: Article) = {
      val currentTime = DateTime.now
      val currentUserId = 1 //TODO
      val newRecord = ArticleRecord(None, article.title, article.content, currentTime, currentTime, article.description, currentUserId)
      val inserted = articlesRepository.insert(newRecord)
      toDetailsModel(inserted, UserRecord(Some(1), ""))// TODO: real user
    }

    def updateArticle(article: Article) = {
      val modificationTime = DateTime.now
      //TODO: handle id
      articlesRepository.update(article.id.get, toRecord(article, modificationTime))
    }

    def removeArticle(id: Int) = {
      articlesRepository.remove(id)
    }

    def get(id: Int) = {
      articlesRepository.get(id).map((toDetailsModel _).tupled(_))
    }

    def getPage(page: Int) = {
      val pageSize = 3 //TODO: move to some settings file
      val offset = pageSize * page
      articlesRepository.getList(offset, pageSize).map((toListModel _).tupled(_))
    }

    def toRecord(article: Article, updatedAt: Timestamp) = {
      ArticleToUpdate(article.title, article.content, updatedAt, article.description)
    }

    def toDetailsModel(articleRecord: ArticleRecord, authorRecord: UserRecord) = {
      ArticleDetailsModel(articleRecord.id.get, articleRecord.title,
        articleRecord.content, articleRecord.createdAt,
          UserModel(authorRecord.id.get, authorRecord.username))
    }

    def toListModel(articleRecord: ArticleRecord, authorRecord: UserRecord) = {
      ArticleListModel(articleRecord.id.get, articleRecord.title,
        articleRecord.description, articleRecord.createdAt,
          UserModel(authorRecord.id.get, authorRecord.username))
    }
  }
}
