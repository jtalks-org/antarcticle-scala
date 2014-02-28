import org.specs2.mutable.Specification
import views.helpers.Markdown

class MarkdownSpec extends Specification{

  "markdown to html" should {
    /*    "produce extended syntax highlighting capable html" in {
          val markdownSource = """
    ```scala
    class Example(name: String) {
        val field: Option[Int] = None
    }
    ```
    """
          val expectedCodeTag = "<code class=\"lang-scala\">"

          Markdown.toHtml(markdownSource) must contain(expectedCodeTag)
        }*/

    "produce html for fenced code blocks without explicit language" in {
      val markdownSource = """
```
class Example(name: String) {
    val field: Option[Int] = None
}
```
"""
      val expectedCodeTag = "<code"

      Markdown.toHtml(markdownSource) must contain(expectedCodeTag)
    }

    "properly escape symbols to prevent xss and broken formatting" in {
      val markdownSource = "<script></script><div><a href=\"url&param\">"

      val result = Markdown.toHtml(markdownSource)

      result must not contain "<script>"
      result must not contain "<div>"
      result must not contain "<a"
    }
  }
}
