import org.specs2.mutable.Specification
import views.helpers.Markdown

/**
 */
class MarkdownSpec extends Specification{

  "markdown" should {
    val textWithFirstLevelHeader = "# First level header"
    
    "transform first level header" in {
      Markdown.toHtml(textWithFirstLevelHeader) mustEqual("<h1>First level header</h1>");
    }
  }

  "markdown" should {
    val textWithSecondLevelHeader = "## Second level header"

    "transform second level header" in {
      Markdown.toHtml(textWithSecondLevelHeader) mustEqual("<h2>Second level header</h2>");
    }
  }

  "markdown" should {
    val textWithThirdLevelHeader = "### Third level header"

    "transform third level header" in {
      Markdown.toHtml(textWithThirdLevelHeader) mustEqual("<h3>Third level header</h3>")
    }
  }


  "markdown" should {
    val textWithBlockQuotes = "> The overriding design goal for Markdown's"

    "transform blockquotes" in {
      Markdown.toHtml(textWithBlockQuotes) mustEqual("<blockquote><p>The overriding design goal for Markdown's</p>\n</blockquote>")
    }
  }

  "markdown" should {
    val textWithLinks = "This is [an example](http://example.com/ \"Title\") inline link.[This link](http://example.net/) has no title attribute."

    "transform links" in {
      Markdown.toHtml(textWithLinks) mustEqual("<p>This is <a href=\"http://example.com/\" title=\"Title\">an example</a> inline link.<a href=\"http://example.net/\">This link</a> has no title attribute.</p>")
    }
  }


  "markdown" should  {
    val textWithListsMarkdown = "+ Red\n+ Green\n+ Blue"
    "transform lists" in {
      Markdown.toHtml(textWithListsMarkdown) mustEqual("<ul>\n  <li>Red</li>\n  <li>Green</li>\n  <li>Blue</li>\n</ul>");
    }
  }


  "markdown" should {
    val textWithSyntaxHighlight = "```ruby\nrequire 'redcarpet'\nmarkdown = Redcarpet.new(\"Hello World!\")\nputs markdown.to_html\n```"

    "transform syntax highlighting" in {
      Markdown.toHtml(textWithSyntaxHighlight) mustEqual("<p><code>ruby\nrequire &#39;redcarpet&#39;\nmarkdown = Redcarpet.new(&quot;Hello World!&quot;)\nputs markdown.to_html\n</code></p>")
    }
  }
}
