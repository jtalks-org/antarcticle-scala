import org.specs2.mutable.Specification
import views.helpers.Markdown

/**
 */
class MarkdownSpec extends Specification{

  "markdown" should {
    val textWithFirstLevelHeader = "# First level header"
    
    "transform first level header" in {
      Markdown.toHtml(textWithFirstLevelHeader) mustEqual("<h1> First level header</h1>");
    }
  }

  "markdown" should {
    val textWithSecondLevelHeader = "## Second level header"

    "transform second level header" in {
      Markdown.toHtml(textWithSecondLevelHeader) mustEqual("<h2> Second level header</h2>");
    }
  }

  "markdown" should {
    val textWithThirdLevelHeader = "### Third level header"

    "transform third level header" in {
      Markdown.toHtml(textWithThirdLevelHeader) mustEqual("<h3> Third level header</h3>")
    }
  }


  "markdown" should {
    val textWithBlockQuotes = "> The overriding design goal for Markdown's"

    "transform blockquotes" in {
      Markdown.toHtml(textWithBlockQuotes) mustEqual("<blockquote>\n<p>The overriding design goal for Markdown's</p>\n</blockquote>")
    }
  }


  "markdown" should {
    val textWithStrikeThroughText = "~~Mistaken text.~~"

    "transform strike through text" in {
      Markdown.toHtml(textWithStrikeThroughText) mustEqual("<del>Mistaken text.</del>")
    }
  }

  "markdown" should  {
    val textWithListsMarkdown = "+ Red\n+ Green\n+ Blue"
    "transform lists" in {
      Markdown.toHtml(textWithListsMarkdown) mustEqual("<ul>\n<li>Red</li>\n<li>Green</li>\n<li>Blue</li>\n</ul>");
    }
  }

  "markdown" should {
    ""
  }
}
