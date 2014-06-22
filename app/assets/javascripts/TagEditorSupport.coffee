jQuery(=>
  $(document).ready(=>
    # set up tags and tag suggestion list for input fields
    input = $('#tag_input')
    tagApi = input.tagsManager({
      tagClass: "tm-tag tm-tag-info"
      hiddenTagListName: "tags",
      tagsContainer: '.tag-container'
    });
    input.typeahead({
      limit: 10,
      prefetch: input.attr('data-url'),
    # filters autocomplete suggestions to exclude already selected tags
      suggestionFilter: (list) ->
        list.filter((elem) ->
          $.inArray(elem.value, tagApi.tagsManager("tags")) == -1
        )
    }).on('typeahead:selected', (e, d) =>
      tagApi.tagsManager("pushTag", d.value)
    )
    # if we have something in the search field on page load - make tags from it
    tagApi.tagsManager("pushTag", tag) for tag in $('#tag_input').attr('value').split(',')
    $('.tt-hint').addClass('form-control');
  )
)