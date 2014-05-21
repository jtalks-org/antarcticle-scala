package models

import scala.math._
import conf.Constants

/**
 * @param currentPage number or this page, starting from 1 up to totalPages value
 * @param totalItems total number of items in all the pages
 * @param list page contents
 * @tparam T type of the page elements
 */
abstract class Page[T](val currentPage: Int, val totalItems: Int, val list: Seq[T]) {

  val totalPages: Int

  require(currentPage > 0, "Pages are numbered starting from 1")
}

class ArticlePage[T](currentPage: Int, totalItems: Int, list: Seq[T]) extends Page[T](currentPage, totalItems, list) {
  override val totalPages = ArticlePage.getPageCount(totalItems)
}

class UserPage[T](currentPage: Int, totalItems: Int, list: Seq[T]) extends Page[T](currentPage, totalItems, list) {
  override val totalPages = UserPage.getPageCount(totalItems)
}

class BasePage(pageSize: Int) {
  def getPageCount(totalItems: Int) = if (totalItems == 0) 1 else ceil(totalItems / pageSize.toDouble).toInt
}

object ArticlePage extends BasePage(Constants.PAGE_SIZE_ARTICLES)

object UserPage extends BasePage(Constants.PAGE_SIZE_USERS)