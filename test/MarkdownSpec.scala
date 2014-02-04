import org.specs2.mutable.Specification
import views.helpers.Markdown

class MarkdownSpec extends Specification{

  "markdown to html" should {
    "produce extended syntax highlighting capable html" in {
      val markdownSource = """
```scala
class Example(name: String) {
    val field: Option[Int] = None
}
```
"""
      val expectedCodeTag = "<code class=\"lang-scala\">"

      Markdown.toHtml(markdownSource) must contain(expectedCodeTag)
    }

    "produce html for fenced code blocks without explicit language" in {
      val markdownSource = """
```
class Example(name: String) {
    val field: Option[Int] = None
}
```
"""
      val expectedCodeTag = "<code>"

      Markdown.toHtml(markdownSource) must contain(expectedCodeTag)
    }
  }
}
