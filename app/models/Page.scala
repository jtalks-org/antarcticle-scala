package models

case class Page[T](currentPage: Int, totalPages: Int, totalItems: Int, list: Seq[T])
