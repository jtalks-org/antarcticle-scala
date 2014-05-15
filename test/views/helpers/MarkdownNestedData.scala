package views.helpers

object MarkdownNestedData {
  val inlineCodeInsideBlockquotes = ">`code1` and `code2`"
  val inlineCodeInsideBlockquotesResult = "<blockquote><p><code>code1</code> and <code>code2</code></p>\n</blockquote>"

  val horizLinesInsideBlockquotes = """>---
                                      |>***
                                      |>___""".stripMargin
  val horizLinesInsideBlockquotesResult = """<blockquote>
                                            |  <hr/>
                                            |  <hr/>
                                            |  <hr/>
                                            |</blockquote>""".stripMargin

  val boldTextInsideBlockquotes = """> __bold text__
                                    |>  **inside blockquotes**""".stripMargin
  val boldTextInsideBlockquotesResult =
    """<blockquote><p><strong>bold text</strong><br/> <strong>inside blockquotes</strong></p>
      |</blockquote>""".stripMargin

  val strikethroughAndItalicsInsideBlockquotes = """>*asterisks* _underscores_
                                                   |>~~Scratch this.~~""".stripMargin
  val strikethroughAndItalicsInsideBlockquotesResult =
    """<blockquote><p><em>asterisks</em> <em>underscores</em><br/><del>Scratch this.</del></p>
      |</blockquote>""".stripMargin

  val listsInsideBlockquotes = """> 1. First ordered list item
                                 |> 2. Another item
                                 |>  * Unordered sub-list.
                                 |> 1. Actual numbers don't matter, just that it's a number
                                 |> 4. And another item.
                                 |   Some text that should be aligned with the above item.""".stripMargin

  val listsInsideBlockquotesResult = """<blockquote>
                                       |  <ol>
                                       |    <li>First ordered list item</li>
                                       |    <li>Another item</li>
                                       |  </ol>
                                       |  <ul>
                                       |    <li>Unordered sub-list.</li>
                                       |  </ul>
                                       |  <ol>
                                       |    <li>Actual numbers don&rsquo;t matter, just that it&rsquo;s a number</li>
                                       |    <li>And another item.<br/> Some text that should be aligned with the above item.</li>
                                       |  </ol>
                                       |</blockquote>""".stripMargin

  val headersInsideBlockquotes = """># H1
                                   |>## H2
                                   |>### H3
                                   |>#### H4
                                   |>##### H5
                                   |>###### H6""".stripMargin
  val headersInsideBlockquotesResult = """<blockquote><h1>H1</h1><h2>H2</h2><h3>H3</h3><h4>H4</h4><h5>H5</h5><h6>H6</h6>
                                         |</blockquote>""".stripMargin

  val linksInsideBlockquotes = """>[inline-style link](https://www.google.com)
                                 |>https://www.mozilla.org
                               """.stripMargin
  val linksInsideBlockquotesResult = "<blockquote><p><a href=\"https://www.google.com\">inline-style link</a><br/>" +
    "<a href=\"https://www.mozilla.org\">https://www.mozilla.org</a></p>\n</blockquote>"


  val boldItalicCrossedInsideCode = "~~___**`code1`**___~~"
  val boldItalicCrossedInsideCodeResult = "<p><del><strong><em><strong><code>code1</code></strong></em></strong></del></p>"

  val inlineCodeInsideLists = """1. `code1`
                                |2. `code2`
                                |* `Unordered list can use asterisks`
                                |- `Or minuses`
                                |+ `Or pluses`""".stripMargin
  val inlineCodeInsideListsResult = """<ol>
                                      |  <li><code>code1</code></li>
                                      |  <li><code>code2</code></li>
                                      |</ol>
                                      |<ul>
                                      |  <li><p><code>Unordered list can use asterisks</code></p></li>
                                      |  <li><p><code>Or minuses</code></p></li>
                                      |  <li><p><code>Or pluses</code></p></li>
                                      |</ul>""".stripMargin

  val boldLinks = """**[inline-style bold link](https://www.google.com)**
                    |__[inline-style bold link](https://www.google.com)__""".stripMargin
  val boldLinksResult =
    """<p><strong><a href="https://www.google.com">inline-style bold link</a></strong><br/><strong><a href="https://www.google.com">inline-style bold link</a></strong></p>"""

  val italicLinks = """*[inline-style italic link](https://www.google.com)*
                      |_[inline-style italic link](https://www.google.com)_""".stripMargin
  val italicLinksResult =
    """<p><em><a href="https://www.google.com">inline-style italic link</a></em><br/><em><a href="https://www.google.com">inline-style italic link</a></em></p>"""

  val crossedLinks = """~~[inline-style crossed link](https://www.google.com)~~
                       |~~https://www.mozilla.org~~""".stripMargin
  val crossedLinksResult =
    """<p><del><a href="https://www.google.com">inline-style crossed link</a></del><br/><del><a href="https://www.mozilla.org~~">https://www.mozilla.org~~</a></del></p>"""

  val boldItalicCrossedTextInList = """+ **bold** *italic*
                                      |+ __bold__ _italic_ ~~crossed~~
                                      |- **bold** *italic*
                                      |- __bold__ _italic_ ~~crossed~~
                                      |* **bold** *italic*
                                      |* __bold__ _italic_ ~~crossed~~""".stripMargin
  val boldItalicCrossedTextInListResult = """<ul>
                                            |  <li><p><strong>bold</strong> <em>italic</em></p></li>
                                            |  <li><p><strong>bold</strong> <em>italic</em> <del>crossed</del></p></li>
                                            |  <li><p><strong>bold</strong> <em>italic</em></p></li>
                                            |  <li><p><strong>bold</strong> <em>italic</em> <del>crossed</del></p></li>
                                            |  <li><p><strong>bold</strong> <em>italic</em></p></li>
                                            |  <li><p><strong>bold</strong> <em>italic</em> <del>crossed</del></p></li>
                                            |</ul>""".stripMargin

  val boldItalicCrossedTextInOrderedList = """1. **bold**
                                             |1. *italic*
                                             |1. __bold__
                                             |1. _italic_
                                             |1. ~~crossed~~""".stripMargin
  val boldItalicCrossedTextInOrderedListResult = """<ol>
                                                   |  <li><strong>bold</strong></li>
                                                   |  <li><em>italic</em></li>
                                                   |  <li><strong>bold</strong></li>
                                                   |  <li><em>italic</em></li>
                                                   |  <li><del>crossed</del></li>
                                                   |</ol>""".stripMargin

  val boldItalicCrossedTextInHeaders = """# **bold** *italic* __bold__ _italic_ ~~crossed~~
                                         |## **bold** *italic* __bold__ _italic_ ~~crossed~~
                                         |### **bold** *italic* __bold__ _italic_ ~~crossed~~
                                         |#### **bold** *italic* __bold__ _italic_ ~~crossed~~
                                         |##### **bold** *italic* __bold__ _italic_ ~~crossed~~
                                         |##### **bold** *italic* __bold__ _italic_ ~~crossed~~""".stripMargin
  val boldItalicCrossedTextInHeadersResult = "<h1><strong>bold</strong> <em>italic</em> <strong>bold</strong> " +
    "<em>italic</em> <del>crossed</del></h1><h2><strong>bold</strong> <em>italic</em> <strong>bold</strong> " +
    "<em>italic</em> <del>crossed</del></h2><h3><strong>bold</strong> <em>italic</em> <strong>bold</strong> " +
    "<em>italic</em> <del>crossed</del></h3><h4><strong>bold</strong> <em>italic</em> <strong>bold</strong> " +
    "<em>italic</em> <del>crossed</del></h4><h5><strong>bold</strong> <em>italic</em> <strong>bold</strong> " +
    "<em>italic</em> <del>crossed</del></h5><h5><strong>bold</strong> <em>italic</em> <strong>bold</strong> " +
    "<em>italic</em> <del>crossed</del></h5>"

}
