package views.helpers

object MarkdownData {

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

  val codeBlockIndentedWithSpaces = """
    class Example(name: String) {
        val field: Option[Int] = None
    }
    """.stripMargin

  val codeBlockIndentedWithSpacesResult = """
    class Example(name: String) {
        val field: Option[Int] = None
    }
    """.stripMargin

  val listWithParagraphs = """
                         Multiple paragraphs:
                         |
                         |1.	Item 1, graf one.
                         |
                         |	Item 2. graf two. The quick brown fox jumped over the lazy dog's
                         |	back.
                         |
                         |2.	Item 2.
                         |
                         |3.	Item 3.
                         """.stripMargin
  val listWithParagraphsResult = """<pre><code>Multiple paragraphs:</code></pre>
              |<ol>
              |  <li><p>Item 1, graf one.</p><p>Item 2. graf two. The quick brown fox jumped over the lazy dog&rsquo;s<br/>back.</p></li>
              |  <li><p>Item 2.</p></li>
              |  <li><p>Item 3.</p></li>
              |</ol>""".stripMargin

  val listWithIndents = """
                         |* this
                         |
                         | * sub
                         |
                         | that
                       """.stripMargin
  val listWithIndentsResult = """<ul>
                                |  <li>this</li>
                                |</ul>
                                |<ul>
                                |  <li>sub</li>
                                |</ul><p>that</p>""".stripMargin

  val orderedList =
    """1. First ordered list item
      | 2. Another item""".stripMargin
  val orderedListResult = """<ol>
                            |  <li>First ordered list item</li>
                            |  <li>Another item</li>
                            |</ol>""".stripMargin

  val nestedList =  """
                      |*	One
                      |	*	Two
                      |
                      |1. First
                      |2. Second:
                      |	* One
                      |3. Third
                      |
                      |1. First
                      |
                      |2. Second:
                      |	* One
                      |
                      |3. Third
                    """.stripMargin
  val nestedListResult = """<ul>
                       |  <li><p>One</p>
                       |  <ul>
                       |    <li>Two</li>
                       |  </ul></li>
                       |</ul>
                       |<ol>
                       |  <li>First</li>
                       |  <li><p>Second:</p>
                       |  <ul>
                       |    <li>One</li>
                       |  </ul></li>
                       |  <li>Third</li>
                       |  <li><p>First</p></li>
                       |  <li><p>Second:</p>
                       |  <ul>
                       |    <li>One</li>
                       |  </ul></li>
                       |  <li><p>Third</p></li>
                       |</ol>""".stripMargin

  val table = "|Tables|Are|Cool|\n| ------------- |:-------------:| -----:|\n" +
    "|col 3 is|right-aligned|$1600|\n|col 2 is|centered|$12|\n|zebra stripes|are neat|$1|"
  val tableResult = """<table>
                      |  <thead>
                      |    <tr>
                      |      <th>Tables</th>
                      |      <th style='text-align:center'>Are</th>
                      |      <th style='text-align:right'>Cool</th>
                      |    </tr>
                      |  </thead>
                      |  <tbody>
                      |    <tr>
                      |      <td>col 3 is</td>
                      |      <td style='text-align:center'>right-aligned</td>
                      |      <td style='text-align:right'>$1600</td>
                      |    </tr>
                      |    <tr>
                      |      <td>col 2 is</td>
                      |      <td style='text-align:center'>centered</td>
                      |      <td style='text-align:right'>$12</td>
                      |    </tr>
                      |    <tr>
                      |      <td>zebra stripes</td>
                      |      <td style='text-align:center'>are neat</td>
                      |      <td style='text-align:right'>$1</td>
                      |    </tr>
                      |  </tbody>
                      |</table>""".stripMargin

  val tableWithInlineMarkdown = "|Markdown | Less | Pretty|\n" +
                                  "|--- | --- | ---|\n" +
                                  "|*Still* | `renders` | **nicely**|\n" +
                                  "|1 | 2 | 3|"

  val tableWithInlineMarkdownResult = """<table>
                                        |  <thead>
                                        |    <tr>
                                        |      <th>Markdown </th>
                                        |      <th>Less </th>
                                        |      <th>Pretty</th>
                                        |    </tr>
                                        |  </thead>
                                        |  <tbody>
                                        |    <tr>
                                        |      <td><em>Still</em> </td>
                                        |      <td><code>renders</code> </td>
                                        |      <td><strong>nicely</strong></td>
                                        |    </tr>
                                        |    <tr>
                                        |      <td>1 </td>
                                        |      <td>2 </td>
                                        |      <td>3</td>
                                        |    </tr>
                                        |  </tbody>
                                        |</table>""".stripMargin

  val horizontalLines = """Three or more...
                          |---
                          |Hyphens
                          |***
                          |Asterisks
                          |___
                          |Underscores""".stripMargin
  val horizontalLinesResult = "<h2>Three or more&hellip;</h2><p>Hyphens<br/>***<br/>Asterisks<br/>___<br/>Underscores</p>"

  val italicAndBoldText = "*put* **Markdown** into a blockquote."
  val italicAndBoldTextResult = "<p><em>put</em> <strong>Markdown</strong> into a blockquote.</p>"

  val emphasis = "*italics* or _italics_ **bold** __bold__ **bold _italic_** ~~strike~~"
  val emphasisResult = "<p><em>italics</em> or <em>italics</em> <strong>bold</strong>" +
    " <strong>bold</strong> <strong>bold <em>italic</em></strong> <del>strike</del></p>"

  val blockquotedText = "> First line.\n> This line is part of the same quote."
  val blockquotedTextResult = """<blockquote><p>First line.<br/>This line is part of the same quote.</p>
                          |</blockquote>""".stripMargin

  val headers = """# H1
                  |## H2
                  |### H3
                  |#### H4
                  |##### H5
                  |###### H6""".stripMargin
  val headersResult = "<h1>H1</h1><h2>H2</h2><h3>H3</h3><h4>H4</h4><h5>H5</h5><h6>H6</h6>"

  val links = """[inline-style link](https://www.google.com)
                |https://www.mozilla.org
                """.stripMargin
  val linksResult = "<p><a href=\"https://www.google.com\">inline-style link</a>" +
                    "<br/><a href=\"https://www.mozilla.org\">https://www.mozilla.org</a></p>"

  val inlineCode = "Inline `code` has `back-ticks around` it."
  val inlineCodeResult = "<p>Inline <code>code</code> has <code>back-ticks around</code> it.</p>"

}
