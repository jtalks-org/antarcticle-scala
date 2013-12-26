package views.helpers

import java.io.{StringReader, StringWriter}
import org.tautua.markdownpapers.Markdown

object Markdown {
  def toHtml(markdown: String): String = {
    val reader = new StringReader(markdown)
    val md = new Markdown()
    val writer = new StringWriter()
    md.transform(reader, writer)
    writer.toString.trim
  }
}
