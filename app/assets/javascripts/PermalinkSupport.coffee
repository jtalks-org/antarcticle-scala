# Displays popups with permanent links

class PermalinkSupport

  constructor: (@selector) ->
    # highlight the selected comment, if any
    urlParts = window.location.toString().split('#')
    if (urlParts.length > 1)
      $("#comment" + urlParts[1]).addClass("comment-highlighted");
    # install permalink handlers
    $(@selector).click(->
      html = 'Press Ctrl+C to copy link to clipboard:<br/><br/>'
      html += '<span id="post-permalink">'
      html += window.location.toString().split('#')[0] + $(this).attr('href')
      html += '</span>'
      bootbox.alert(html)
      setTimeout(() ->
        selectText('post-permalink')
      , 500)
      return false
    )

  selectText = (containerId) ->
    if (document.selection)
      document.selection.empty();
      range = document.body.createTextRange();
      range.moveToElementText(document.getElementById(containerId))
      range.select();
    else if (window.getSelection)
      if (window.getSelection().empty)
        window.getSelection().empty()
      else if (window.getSelection().removeAllRanges)
        window.getSelection().removeAllRanges()
      range = document.createRange()
      range.selectNode(document.getElementById(containerId))
      window.getSelection().addRange(range)


new PermalinkSupport('.permalink')


