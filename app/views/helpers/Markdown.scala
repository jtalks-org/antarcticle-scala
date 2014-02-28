package views.helpers

import org.pegdown._
import org.pegdown.Extensions._
import org.pegdown.ast._
import org.apache.commons.lang3.{StringUtils, StringEscapeUtils}

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
    printer.println().print(s"<pre class='prettyprint linenums'><code>${node.getText}</code></pre>")
  }

  override def visit(node: VerbatimNode) = {
    printer.println().print("<pre class='prettyprint linenums'><code")
    if (!StringUtils.isEmpty(node.getType)) {
      printer.print(s" class='lang-${node.getType}'")
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
}

object Markdown {

  def toHtml(markdown: String): String = {
    // use all extensions, except html filtering
    val processor = new PegDownProcessor(ALL)
    val astRoot = processor.parseMarkdown(processor.prepareSource(markdown.toCharArray))
    new EscapingToHtmlSerializer().toHtml(astRoot)
  }
}
