unless String::trim then String::trim = ->
  @replace /^\s+|\s+$/g, ""
jQuery(=>
  $(document).ready(=>
    $('.trimmed-input').bind('blur', () ->
      input = $(this)
      input.val(input.val().trim()))
    hljs.initHighlightingOnLoad()
    $('.tags-input').tags_input()
    $("body").css("cursor", "default")
    april = new AprilApril({
      replaceImages: true,
      playSound: true,
      shrink: true,
      scrambleLinks: true,
      scrambleText: true,
    });
    april.fool();
  )
)

