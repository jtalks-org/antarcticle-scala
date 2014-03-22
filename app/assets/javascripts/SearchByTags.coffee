#handles searh by tags actions

class SearchByTagsEventHandler

  constructor:  ->
    $("#search_by_tags").click(->
      tagsInput = $(".tags-inner-input").val()
      addTag tag for tag in tagsInput.split(' ')
    )

  addTag = (tag) ->
    tagElement = '<li>@tag<a href="#" class="remove-tag">' + tag + '</a></li>'
    $(".tags-input").append(tagElement);
    $("#tags_filter").val($("#tags_filter").val() + " " + tag)

new SearchByTagsEventHandler()