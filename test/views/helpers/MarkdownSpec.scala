package views.helpers

import org.specs2.mutable.Specification
import views.helpers.MarkdownData._
import views.helpers.MarkdownNestedData._

class MarkdownSpec extends Specification {

  def removeNewLines(str:String) = str.replaceAll("(\r\n)|\r|\n", "")

  def beEqualIgnoreNewLinesTo(str: String) = {
    beEqualTo(removeNewLines(str)) ^^ {(t: String) => removeNewLines(t)}
  }

  def containIgnoreNewLines(str:String) = {
    contain(removeNewLines(str)) ^^ {(t: String) => removeNewLines(t)}
  }

  "markdown to html" should {
    "produce extended syntax highlighting for code blocks" in {
      val expectedCodeTag = "<code class='scala'>"

      Markdown.toHtml(fencedScalaBlock) must containIgnoreNewLines(expectedCodeTag)
    }

    "produce html for fenced code blocks without explicit language" in {
      val expectedCodeTag = "<code"

      Markdown.toHtml(fencedCodeBlock) must containIgnoreNewLines(expectedCodeTag)
    }

    "produce html for code block indented with four spaces" in {
      val expectedCodeTag = "<code"

      Markdown.toHtml(codeBlockIndentedWithSpaces) must containIgnoreNewLines(expectedCodeTag)
    }

    "recognize code blocks with preceding text" in {
      val expectedCodeTag = "<code class='scala'>"
      val textWithCode = "\n" + fencedScalaBlock

      Markdown.toHtml(textWithCode) must containIgnoreNewLines(expectedCodeTag)
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

      result must beEqualIgnoreNewLinesTo(orderedListResult)
    }

    "produce correct html for tables" in {
      val result = Markdown.toHtml(table)

      result must containIgnoreNewLines(tableResult)
    }

    "produce correct html for tables with inline markdown" in {
      val result = Markdown.toHtml(tableWithInlineMarkdown)

      result must containIgnoreNewLines(tableWithInlineMarkdownResult)
    }

    "produce correct blockquoted text" in {
      val result = Markdown.toHtml(blockquotedText)

      result must containIgnoreNewLines(blockquotedTextResult)
    }

    "produce correct italic and bold text" in {
      val result = Markdown.toHtml(italicAndBoldText)

      result must containIgnoreNewLines(italicAndBoldTextResult)
    }

    "produce correct html with horizontal lines" in {
      val result = Markdown.toHtml(horizontalLines)

      result must containIgnoreNewLines(horizontalLinesResult)
    }

    "produce correct html for emphasis" in {
      val result = Markdown.toHtml(emphasis)

      result must containIgnoreNewLines(emphasisResult)
    }

    "produce correct html for nested emphasis" in {
      val markdownSource = "__strong _nestedemph___"

      val result = Markdown.toHtml(markdownSource)

      result must containIgnoreNewLines("<p><strong>strong <em>nestedemph</em></strong></p>")
    }

    "correctly handle underscores in text" in {
      val markdownSource = "__strong _nestedemph__"

      val result = Markdown.toHtml(markdownSource)

      result must containIgnoreNewLines("<p><strong>strong _nestedemph</strong></p>")
    }

    "produce correct html for email" in {
      val markdownSource = "mail@to.me"

      val result = Markdown.toHtml(markdownSource)

      result must containIgnoreNewLines("mailto:")
    }

    "produce correct html for nested lists" in {
      val result = Markdown.toHtml(nestedList)

      result must containIgnoreNewLines(nestedListResult)
    }

    "produce correct html for ordered list with paragraphs" in {
      val result = Markdown.toHtml(listWithParagraphs)

      result must containIgnoreNewLines(listWithParagraphsResult)
    }

    "produce correct html for list with different indents" in {
      val result = Markdown.toHtml(listWithIndents)

      result must containIgnoreNewLines(listWithIndentsResult)
    }

    "produce correct html for headers" in {
      val result = Markdown.toHtml(headers)

      result must containIgnoreNewLines(headersResult)
    }

    "produce correct html for inline code" in {
      val result = Markdown.toHtml(inlineCode)

      result must containIgnoreNewLines(inlineCodeResult)
    }

    "produce correct html for links" in {
      val result = Markdown.toHtml(links)

      result must containIgnoreNewLines(linksResult)
    }
  }

  "markdown to html for nested data" should {
    "correctly handle bold text in blockquotes" in {
      val result = Markdown.toHtml(boldTextInsideBlockquotes)

      result must containIgnoreNewLines(boldTextInsideBlockquotesResult)
    }

    "correctly handle italic and strikethrough text in blockquotes" in {
      val result = Markdown.toHtml(strikethroughAndItalicsInsideBlockquotes)

      result must containIgnoreNewLines(strikethroughAndItalicsInsideBlockquotesResult)
    }

    "correctly handle ordered list in blockquotes" in {
      val result = Markdown.toHtml(listsInsideBlockquotes)

      result must containIgnoreNewLines(listsInsideBlockquotesResult)
    }

    "correctly handle headers in blockquotes" in {
      val result = Markdown.toHtml(headersInsideBlockquotes)

      result must containIgnoreNewLines(headersInsideBlockquotesResult)
    }

    "correctly handle links in blockquotes" in {
      val result = Markdown.toHtml(linksInsideBlockquotes)

      result must containIgnoreNewLines(linksInsideBlockquotesResult)
    }

    "correctly handle inline code in blockquotes" in {
      val result = Markdown.toHtml(inlineCodeInsideBlockquotes)

      result must containIgnoreNewLines(inlineCodeInsideBlockquotesResult)
    }

    "correctly handle horizonal lines in blockquotes" in {
      val result = Markdown.toHtml(horizLinesInsideBlockquotes)

      result must containIgnoreNewLines(horizLinesInsideBlockquotesResult)
    }

    "correctly handle bold italic and crossed text inside inline code" in {
      val result = Markdown.toHtml(boldItalicCrossedInsideCode)

      result must containIgnoreNewLines(boldItalicCrossedInsideCodeResult)
    }

    "correctly handle inline code in lists" in {
      val result = Markdown.toHtml(inlineCodeInsideLists)

      result must containIgnoreNewLines(inlineCodeInsideListsResult)
    }

    "correct html for inline bold links" in {
      val result = Markdown.toHtml(boldLinks)

      result must containIgnoreNewLines(boldLinksResult)
    }

    "correct html for inline italic links" in {
      val result = Markdown.toHtml(italicLinks)

      result must containIgnoreNewLines(italicLinksResult)
    }

    "correct html for inline crossed links" in {
      val result = Markdown.toHtml(crossedLinks)

      result must containIgnoreNewLines(crossedLinksResult)
    }

    "correctly handle bold, italic and crossed text in lists" in {
      val result = Markdown.toHtml(boldItalicCrossedTextInList)

      result must containIgnoreNewLines(boldItalicCrossedTextInListResult)
    }

    "correctly handle bold, italic and crossed text in ordered lists" in {
      val result = Markdown.toHtml(boldItalicCrossedTextInOrderedList)

      result must containIgnoreNewLines(boldItalicCrossedTextInOrderedListResult)
    }

    "correctly handle bold, italic and crossed text in headers" in {
      val result = Markdown.toHtml(boldItalicCrossedTextInHeaders)

      result must containIgnoreNewLines(boldItalicCrossedTextInHeadersResult)
    }
  }
}
