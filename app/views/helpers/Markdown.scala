package views.helpers

import org.pegdown._
import org.pegdown.Extensions._
import org.pegdown.ast._
import org.apache.commons.lang3.StringEscapeUtils

/**
 * <p>Extends PegDown serializer to archive the following:
 *
 * <p>1. Prevents XSS attack by escaping any tags in article/comment contents,
 * which are not parsed as explicit code by markdown parser
 * <p>2. Adds source code formatting CSS classes
 */
class EscapingToHtmlSerializer extends ToHtmlSerializer(new LinkRenderer){

  override def visit(node : HtmlBlockNode) {
    val text = node.getText
    if (text.length() > 0) printer.println()
    printer.printEncoded(text)
  }

  override def visit(node : InlineHtmlNode ) = printer.printEncoded(node.getText)

  override def visit(node: CodeNode) = {
    printer.println().print("<pre class='prettyprint linenums'><code>")
    printer.printEncoded(node.getText)
    printer.print("</code></pre>")
  }

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
