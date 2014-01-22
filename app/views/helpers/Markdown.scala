package views.helpers

import java.io.{StringReader, StringWriter}
import org.pegdown.PegDownProcessor

object Markdown {
  def toHtml(markdown: String): String = {
    val processor = new PegDownProcessor()
    processor.markdownToHtml(markdown)
  }
}
