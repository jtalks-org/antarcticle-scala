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
      val expectedCodeTag = "<code>"

      Markdown.toHtml(fencedScalaBlock) must contain(expectedCodeTag)
    }

    "produce html for fenced code blocks without explicit language" in {
      val expectedCodeTag = "<code"

      Markdown.toHtml(fencedCodeBlock) must contain(expectedCodeTag)
    }

    "recognize code blocks with preceding text" in {
      val expectedCodeTag = "<code>"
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
  }
}
