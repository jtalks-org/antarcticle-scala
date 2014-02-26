package views.helpers

import org.pegdown._
import org.pegdown.Extensions._
import org.parboiled.common.StringUtils
import org.pegdown.ast._
import org.apache.commons.lang3.StringEscapeUtils

/**
 * Adds class with specified in fenced code block language for
 * extended syntax highlighting using google prettify.
 *
 * Example:
 * Should produce: <pre><code class="lang-ruby">..</code></pre>
 * for markdown like:
 * ```ruby
 * ruby code here
 * ```
 */
object GooglePrettifyVerbatimSerializer extends VerbatimSerializer {

  val map  = new java.util.HashMap[String, VerbatimSerializer]
  map.put(VerbatimSerializer.DEFAULT, GooglePrettifyVerbatimSerializer)
  
  override def serialize(node: VerbatimNode, printer: Printer): Unit = {
    printer.println().print("<pre><code")
    if (!StringUtils.isEmpty(node.getType)) {
      printAttribute(printer, "class", s"lang-${node.getType}")
    }
    printer.print(">")
    var text = node.getText
    // print HTML breaks for all initial newlines
    while (text.charAt(0) == '\n') {
      printer.print("<br/>")
      text = text.substring(1)
    }
    printer.printEncoded(text)
    printer.print("</code></pre>")

  }

  private def printAttribute(printer: Printer, name: String, value: String): Unit = {
    printer.print(' ').print(name).print('=').print('"').print(value).print('"')
  }
}

/**
 * Prevents XSS attack by escaping any tags in article/comment contents,
 * which are not parsed as explicit code by markdown parser
 */
class EscapingToHtmlSerializer extends ToHtmlSerializer(new LinkRenderer, GooglePrettifyVerbatimSerializer.map){

  override def visit(node : HtmlBlockNode) {
    val text = node.getText
    if (text.length() > 0) printer.println()
    printer.print(escape(text))
  }

  override def visit(node : InlineHtmlNode ) = printer.print(escape(node.getText))

  private def escape(text:String) = StringEscapeUtils.escapeHtml4(text)
}

object Markdown {

  def toHtml(markdown: String): String = {
    // use all extensions
    val processor = new PegDownProcessor(ALL)
    val astRoot = processor.parseMarkdown(processor.prepareSource(markdown.toCharArray))
    new EscapingToHtmlSerializer().toHtml(astRoot)
  }
}
