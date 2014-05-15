package views.helpers

import org.specs2.mutable.Specification
import views.helpers.MarkdownData._
import views.helpers.MarkdownNestedData._

class MarkdownSpec extends Specification {

  "markdown to html" should {
    "produce extended syntax highlighting for code blocks" in {
      val expectedCodeTag = "<code class='scala'>"

      Markdown.toHtml(fencedScalaBlock) must contain(expectedCodeTag)
    }

    "produce html for fenced code blocks without explicit language" in {
      val expectedCodeTag = "<code"

      Markdown.toHtml(fencedCodeBlock) must contain(expectedCodeTag)
    }

    "produce html for code block indented with four spaces" in {
      val expectedCodeTag = "<code"

      Markdown.toHtml(codeBlockIndentedWithSpaces) must contain(expectedCodeTag)
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
      val result = Markdown.toHtml(orderedList)

      result mustEqual orderedListResult
    }

    "produce correct html for tables" in {
      val result = Markdown.toHtml(table)

      result must contain(tableResult)
    }

    "produce correct html for tables with inline markdown" in {
      val result = Markdown.toHtml(tableWithInlineMarkdown)

      result must contain(tableWithInlineMarkdownResult)
    }

    "produce correct blockquoted text" in {
      val result = Markdown.toHtml(blockquotedText)

      result must contain(blockquotedTextResult)
    }

    "produce correct italic and bold text" in {
      val result = Markdown.toHtml(italicAndBoldText)

      result must contain(italicAndBoldTextResult)
    }

    "produce correct html with horizontal lines" in {
      val result = Markdown.toHtml(horizontalLines)

      result must contain(horizontalLinesResult)
    }

    "produce correct html for emphasis" in {
      val result = Markdown.toHtml(emphasis)

      result must contain(emphasisResult)
    }

    "produce correct html for nested emphasis" in {
      val markdownSource = "__strong _nestedemph___"

      val result = Markdown.toHtml(markdownSource)

      result must contain("<p><strong>strong <em>nestedemph</em></strong></p>")
    }

    "correctly handle underscores in text" in {
      val markdownSource = "__strong _nestedemph__"

      val result = Markdown.toHtml(markdownSource)

      result must contain("<p><strong>strong _nestedemph</strong></p>")
    }

    "produce correct html for email" in {
      val markdownSource = "mail@to.me"

      val result = Markdown.toHtml(markdownSource)

      result must contain("mailto:")
    }

    "produce correct html for nested lists" in {
      val result = Markdown.toHtml(nestedList)

      result must contain(nestedListResult)
    }

    "produce correct html for ordered list with paragraphs" in {
      val result = Markdown.toHtml(listWithParagraphs)

      result must contain(listWithParagraphsResult)
    }

    "produce correct html for list with different indents" in {
      val result = Markdown.toHtml(listWithIndents)

      result must contain(listWithIndentsResult)
    }

    "produce correct html for headers" in {
      val result = Markdown.toHtml(headers)

      result must contain(headersResult)
    }

    "produce correct html for inline code" in {
      val result = Markdown.toHtml(inlineCode)

      result must contain(inlineCodeResult)
    }

    "produce correct html for links" in {
      val result = Markdown.toHtml(links)

      result must contain(linksResult)
    }
  }

  "markdown to html for nested data" should {
    "correctly handle bold text in blockquotes" in {
      val result = Markdown.toHtml(boldTextInsideBlockquotes)

      result must contain(boldTextInsideBlockquotesResult)
    }

    "correctly handle italic and strikethrough text in blockquotes" in {
      val result = Markdown.toHtml(strikethroughAndItalicsInsideBlockquotes)

      result must contain(strikethroughAndItalicsInsideBlockquotesResult)
    }

    "correctly handle ordered list in blockquotes" in {
      val result = Markdown.toHtml(listsInsideBlockquotes)

      result must contain(listsInsideBlockquotesResult)
    }

    "correctly handle headers in blockquotes" in {
      val result = Markdown.toHtml(headersInsideBlockquotes)

      result must contain(headersInsideBlockquotesResult)
    }

    "correctly handle links in blockquotes" in {
      val result = Markdown.toHtml(linksInsideBlockquotes)

      result must contain(linksInsideBlockquotesResult)
    }

    "correctly handle inline code in blockquotes" in {
      val result = Markdown.toHtml(inlineCodeInsideBlockquotes)

      result must contain(inlineCodeInsideBlockquotesResult)
    }

    "correctly handle horizonal lines in blockquotes" in {
      val result = Markdown.toHtml(horizLinesInsideBlockquotes)

      result must contain(horizLinesInsideBlockquotesResult)
    }

    "correctly handle bold italic and crossed text inside inline code" in {
      val result = Markdown.toHtml(boldItalicCrossedInsideCode)

      result must contain(boldItalicCrossedInsideCodeResult)
    }

    "correctly handle inline code in lists" in {
      val result = Markdown.toHtml(inlineCodeInsideLists)

      result must contain(inlineCodeInsideListsResult)
    }

    "correct html for inline bold links" in {
      val result = Markdown.toHtml(boldLinks)

      result must contain(boldLinksResult)
    }

    "correct html for inline italic links" in {
      val result = Markdown.toHtml(italicLinks)

      result must contain(italicLinksResult)
    }

    "correct html for inline crossed links" in {
      val result = Markdown.toHtml(crossedLinks)

      result must contain(crossedLinksResult)
    }

    "correctly handle bold, italic and crossed text in lists" in {
      val result = Markdown.toHtml(boldItalicCrossedTextInList)

      result must contain(boldItalicCrossedTextInListResult)
    }

    "correctly handle bold, italic and crossed text in ordered lists" in {
      val result = Markdown.toHtml(boldItalicCrossedTextInOrderedList)

      result must contain(boldItalicCrossedTextInOrderedListResult)
    }

    "correctly handle bold, italic and crossed text in headers" in {
      val result = Markdown.toHtml(boldItalicCrossedTextInHeaders)

      result must contain(boldItalicCrossedTextInHeadersResult)
    }
  }
}
