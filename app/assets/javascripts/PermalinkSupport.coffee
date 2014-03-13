# Displays popups with permanent links

class PermalinkSupport

  constructor: (@selector) ->
    $(@selector).click( ->
      bootbox.alert(window.location.toString().split('#')[0] + $(this).attr('href'))
      return false
    )

new PermalinkSupport('.permalink')