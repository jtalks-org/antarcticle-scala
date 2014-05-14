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

    if ($("#tag_input").size() > 0)
      input = $('#tag_input')
      tagApi = input.tagsManager({
        tagClass: "tm-tag tm-tag-info"
        hiddenTagListName: "tags",
        tagsContainer: '.tag-container'
      });
      input.typeahead({
        limit: 10,
        prefetch: input.attr('data-url')
      }).on('typeahead:selected', (e, d) =>
        tagApi.tagsManager("pushTag", d.value)
      )
      tagApi.tagsManager("pushTag", tag) for tag in $('#tag_input').attr('value').split(',')
      $('.tt-hint').addClass('form-control');
      $('.tag-search-input').closest('form').on 'submit', (e) ->
        inputData = $('.tag-search-input').val()
        if (inputData.trim() != "")
          tagApi.tagsManager("pushTag", inputData)
        if ($('.tag-container span').length == 0)
          document.location = document.location.toString().split('?')[0]
          e.preventDefault()
          false
        else
          true
  )
)

