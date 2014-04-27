import org.specs2.mutable.Specification
import views.helpers.Markdown

class MarkdownSpec extends Specification {

  val fencedScalaBlock = """
```scala
class Example(name: String) {

  val field: Option[Int] = None
}
```
                         """

  val fencedCodeBlock =  """
```
class Example(name: String) {
    val field: Option[Int] = None
}
```
"""

  "markdown to html" should {
    "produce extended syntax highlighting for code blocks" in {
      val expectedCodeTag = "<code class='scala'>"

      Markdown.toHtml(fencedScalaBlock) must contain(expectedCodeTag)
    }

    "produce html for fenced code blocks without explicit language" in {
      val expectedCodeTag = "<code"

      Markdown.toHtml(fencedCodeBlock) must contain(expectedCodeTag)
    }

    "recognize code blocks with preceding text" in {
      val expectedCodeTag = "<code class='scala'>"
      val textWithCode = "\n" + fencedScalaBlock

      Markdown.toHtml(textWithCode) must contain(expectedCodeTag)
    }

    "properly escape symbols to prevent xss and broken formatting" in {
      val markdownSource = "<script></script><div><a href='url&param'>"

      val result = Markdown.toHtml(markdownSource)

      result must not contain "<script>"
      result must not contain "<div>"
      result must not contain "<a"
    }

    "produce ordered list" in {
      val markdownSource = "1. First ordered list item\n 2. Another item"

      val result = Markdown.toHtml(markdownSource)

      result mustEqual "<ol>\n  <li>First ordered list item</li>\n  <li>Another item</li>\n</ol>"
    }

    "produce correct html for tables" in {
      val markdownSource = "|Tables|Are|Cool|\n| ------------- |:-------------:| -----:|\n" +
        "|col 3 is|right-aligned|$1600|\n|col 2 is|centered|$12|\n|zebra stripes|are neat|$1|"

      val result = Markdown.toHtml(markdownSource)

      result must contain("<table>\n  <thead>\n    <tr>\n  " +
        "    <th>Tables</th>\n      <th align=\"center\">Are</th>\n   " +
        "   <th align=\"right\">Cool</th>")
    }

    "produce correct html for tables with inline markdown" in {
      val markdownSource = "Markdown | Less | Pretty\n--- | --- | ---\n*Still* | `renders` | **nicely**\n1 | 2 | 3"

      val result = Markdown.toHtml(markdownSource)

      result must contain("<table>\n  <thead>\n    <tr>\n      <th>Markdown </th>\n    " +
        "  <th>Less </th>\n      <th>Pretty</th>\n    </tr>\n  </thead>\n  <tbody>\n    <tr>\n   " +
        "   <td><em>Still</em> </td>\n      <td><code>renders</code> </td>\n   " +
        "   <td><strong>nicely</strong></td>\n    </tr>\n    <tr>\n   " +
        "   <td>1 </td>\n      <td>2 </td>\n      <td>3</td>\n    </tr>\n  </tbody>\n</table>")
    }

    "produce correct blockquoted text" in {
      val markdownSource = "> First line.\n" +
        "> This line is part of the same quote."

      val result = Markdown.toHtml(markdownSource)

      result must contain("<blockquote><p>First line.<br/>This line is part of the same quote.</p>\n</blockquote>")
    }

    "produce correct italic and bold text" in {
      val markdownSource = "*put* **Markdown** into a blockquote."

      val result = Markdown.toHtml(markdownSource)

      result must contain("<p><em>put</em> <strong>Markdown</strong> into a blockquote.</p>")
    }

    "produce correct html with horizontal lines" in {
      val markdownSource = "Three or more...\n---\nHyphens\n***\nAsterisks\n___\nUnderscores"

      val result = Markdown.toHtml(markdownSource)

      result must contain("<h2>Three or more&hellip;</h2><p>Hyphens<br/>***<br/>Asterisks<br/>___<br/>Underscores</p>")
    }

    "produce correct html for emphasis" in {
      val markdownSource = "*italics* or _italics_ **bold** __bold__ **bold _italic_** ~~strike~~"

      val result = Markdown.toHtml(markdownSource)

      result must contain("<p><em>italics</em> or <em>italics</em> <strong>bold</strong>" +
        " <strong>bold</strong> <strong>bold <em>italic</em></strong> <del>strike</del></p>")
    }

    "produce correct html for nested emphasis" in {
      val markdownSource = "__strong _nestedemph___"

      val result = Markdown.toHtml(markdownSource)

      result must contain("<p><strong>strong <em>nestedemph</em></strong></p>")
    }
  }
}
