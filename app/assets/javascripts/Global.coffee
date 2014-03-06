unless String::trim then String::trim = -> @replace /^\s+|\s+$/g, ""
jQuery(=>
  $(document).ready(=> $('.trimmed-input').bind('blur', () ->
    input = $(this)
    input.val(input.val().trim()))
  )
)

