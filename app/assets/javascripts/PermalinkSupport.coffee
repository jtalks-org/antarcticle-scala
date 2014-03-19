# Displays popups with permanent links

class PermalinkSupport

  constructor: (@selector) ->
    # highlight the selected comment, if any
    urlParts = window.location.toString().split('#')
    if (urlParts.length > 1)
      $("#" + urlParts[1]).parent().siblings().addClass("comment-highlighted");
    # install permalink handlers
    $(@selector).click( ->
      bootbox.alert(window.location.toString().split('#')[0] + $(this).attr('href'))
      return false
    )

new PermalinkSupport('.permalink')


