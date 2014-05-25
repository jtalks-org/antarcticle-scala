unless String::trim then String::trim = ->
  @replace /^\s+|\s+$/g, ""
jQuery(=>
  $(document).ready(=>
    $('.trimmed-input').bind('blur', () ->
      input = $(this)
      input.val(input.val().trim()))
  )
)

showSuccessNotification = (text) ->
  showNotification(text, $('.alert-success'))

showFailureNotification = (text) ->
  showNotification(text, $('.alert-danger'))

showNotification = (text, element) ->
  element.find("span").html(text);
  element.fadeIn().delay(3000).fadeOut();

