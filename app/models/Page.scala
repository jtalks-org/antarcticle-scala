package models

import scala.math._
import conf.Constants

/**
 * @param currentPage number or this page, starting from 1 up to totalPages value
 * @param totalItems total number of items in all the pages
 * @param list page contents
 * @tparam T type of the page elements
 */
case class Page[T](currentPage: Int, lastUsedTag: String, totalItems: Int, list: Seq[T]) {

  val totalPages = Page.getPageCount(totalItems)
  require(currentPage > 0, "Pages are numbered starting from 1")
}

object Page{
  def getPageCount(totalItems : Int) = if (totalItems == 0)  1 else ceil(totalItems / Constants.PAGE_SIZE.toDouble).toInt
}
