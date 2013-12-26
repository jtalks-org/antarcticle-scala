package views.helpers

import java.io.{StringReader, StringWriter}

object Markdown {
  def toHtml(markdown: String): String = {
    val reader = new StringReader(markdown)
    val md = new org.tautua.markdownpapers.Markdown()
    val writer = new StringWriter()
    md.transform(reader, writer)
    writer.toString.trim
  }
}
