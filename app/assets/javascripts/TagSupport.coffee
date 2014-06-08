jQuery(=>
  $(document).ready(=>
    # set up tags and tag autocompletion for imput fields
    if ($("#tag_input").size() > 0)
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
      # control tag search form submit behavior
      $('.search-input').closest('form').on 'submit', (e) ->
        inputData = $('.search-input').val()
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