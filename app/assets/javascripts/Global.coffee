unless String::trim then String::trim = ->
  @replace /^\s+|\s+$/g, ""
jQuery(=>
  $(document).ready(=>
    $('.trimmed-input').bind('blur', () ->
      input = $(this)
      input.val(input.val().trim()))
    hljs.initHighlightingOnLoad()
    $("body").css("cursor", "default")

    tagApi = $('#tag_input').tagsManager({
      tagClass: "tm-tag tm-tag-info"
      hiddenTagListName: "tags",
      tagsContainer: '.tag-container'
    });
    $("#tag_input").typeahead({
      name: 'countries',
      limit: 10,
      prefetch: 'http://localhost:9000/tags'
    }).on('typeahead:selected', (e, d) =>
      tagApi.tagsManager("pushTag", d.value);
    )
    tagApi.tagsManager("pushTag", tag) for tag in $('#tag_input').attr('value').split(',')
    $('.tt-hint').addClass('form-control');
  )
)

