unless String::trim then String::trim = ->
  @replace /^\s+|\s+$/g, ""
jQuery(=>
  $(document).ready(=>
    $('.trimmed-input').bind('blur', () ->
      input = $(this)
      input.val(input.val().trim()))
    hljs.initHighlightingOnLoad()
    $("body").css("cursor", "default")
    # tag handling
    tagApi = $('#tag_input').tagsManager({
      tagClass: "tm-tag tm-tag-info"
      hiddenTagListName: "tags",
      tagsContainer: '.tag-container'
    });
    if ($("#tag_input").size() > 0)
      $("#tag_input").typeahead({
        limit: 10,
        prefetch: $("#tag_input").attr('data-url')
      }).on('typeahead:selected', (e, d) =>
        tagApi.tagsManager("pushTag", d.value)
      )
      tagApi.tagsManager("pushTag", tag) for tag in $('#tag_input').attr('value').split(',')
      $('.tt-hint').addClass('form-control');
      $('.tag-search-input').closest('form').on 'submit', (e) ->
        input = $('.tag-search-input').val()
        if (input.trim() != "")
          tagApi.tagsManager("pushTag", input)
        if ($('.tag-container span').length == 0)
          document.location = document.location.toString().split('?')[0]
          e.preventDefault()
          false
        else
          true
  )
)

