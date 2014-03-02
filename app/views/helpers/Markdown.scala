package views.helpers

import org.parboiled.Parboiled

/**
 * Helper object for markdown-to-html rendering
 */
object Markdown {

  def toHtml(markdown: String): String = {
    val parser = Parboiled.createParser[MarkdownParser, Object](classOf[MarkdownParser])
    val astRoot = parser.parse(markdown)
    new EscapingToHtmlSerializer().toHtml(astRoot)
  }
}


