# Displays popups with permanent links

class PermalinkSupport

  constructor: (@selector) ->
    # highlight the selected comment, if any
    urlParts = window.location.toString().split('#')
    if (urlParts.length > 1)
      $("#comment" + urlParts[1]).addClass("comment-highlighted");
    # install permalink handlers
    $(@selector).click((e) ->
      e.preventDefault()
      html = '<div id="post-permalink" class="comment-permalink" contenteditable="true">'
      html += window.location.toString().split('#')[0] + $(this).attr('href')
      html += '</div>'
      bootbox.alert({animate: false, message: html, title: 'Copy link to clipboard'})
      element = document.getElementById('post-permalink')
      setTimeout(() ->
        selectText(element)
      , 500)
      element.focus()
      return false
    )

  selectText = (element) ->
    if (document.selection)
      document.selection.empty();
      range = document.body.createTextRange();
      range.moveToElementText(element)
      range.select();
    else if (window.getSelection)
      if (window.getSelection().empty)
        window.getSelection().empty()
      else if (window.getSelection().removeAllRanges)
        window.getSelection().removeAllRanges()
      range = document.createRange()
      range.selectNode(element.firstChild)
      window.getSelection().addRange(range)


new PermalinkSupport('.permalink')


