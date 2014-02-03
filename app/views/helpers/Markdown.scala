package views.helpers

import java.io.{StringReader, StringWriter}
import org.pegdown.PegDownProcessor
import org.pegdown.Extensions._

object Markdown {
  def toHtml(markdown: String): String = {
    val processor = new PegDownProcessor(ALL)
    processor.markdownToHtml(markdown)
  }
}
