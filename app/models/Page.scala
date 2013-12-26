package models

case class Page[T](currentPage: Int, totalPages: Int, list: Seq[T])
