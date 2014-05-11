#todo: replace string html elements with dom builders
$.fn.extend
  tags_input: (options) ->
    settings =
      delimiter: ','                                  # tags delimiting character
      class: 'tags-input'                             # css class for styles
      validate: (tag) -> tag && !/^\s*$/.test(tag)     # custom validation function

    settings = $.extend settings, options
    input = $(this)

    # input will be replaced with our elements
    input.hide()

    # read tags from input value
    tags = []
    if (input.val())
      tags = input.val().split(',')

    # input for new tag
    tagInput = $("<input data-role=\"tagsinput\" data-provide=\"typeahead\" data-items=\"10\" autocomplete=\"off\" class=\"tags-inner-input\" />")
    $.get('tags').done((data) => tagInput.typeahead().data('typeahead').source = data)

    createTag = (tag) ->
      escapedTag = $('<div></div>').text(tag).html()
      "<li class=\"tag\"><span>#{escapedTag}</span><a href=\"#\" class=\"remove-tag\">x</a></li>"

    # construct tags list and input for new tag
    html = "<ul class=\"#{settings.class}\">"
    for tag in tags
      html += createTag(tag)
    html += "</ul>"
    tagsList = $(html).insertAfter(input)
    tagsList.append(tagInput)

    # show placeholder from source input only when tags is empty
    updatePlaceholder = ->
      if (tags.length == 0)
        tagInput.attr('placeholder', input.attr('placeholder'))
      else
        tagInput.attr('placeholder', '')

    updatePlaceholder()

    # update source input value
    updateValue = ->
      input.attr('value', tags.join(','))

    # check tag existence in array
    tagExists = (tag) ->
      tags.indexOf(tag) != -1

    # validates that tag is not empty, unique and passes custom validation
    tagValid = (tag) ->
      tag && !tagExists(tag) && settings.validate(tag)

    # add tag if validation passed
    addTag = ->
      newTagValue = tagInput.val().trim()
      tagInput.val('')
      if (!tagValid(newTagValue))
        return false
      tags.push(newTagValue)
      tagsList.append(createTag(newTagValue))
      tagsList.append(tagInput)
      updateValue()
      tagInput.focus()
      updatePlaceholder()
      # remove error highlighting, if any
      $('ul.' + settings.class).css('border','none')

    # focus on tag input when clicked anywhere inside <ul>
    tagsList.on 'click', (e) ->
      tagInput.focus()

    tagInput.on 'keypress', (e) ->
      # create tag when pressed delimiter character
      if (e.which == settings.delimiter.charCodeAt(0))
        e.preventDefault()
        addTag()
    # create tag (if valid) when pressed enter. event will be propagated further
    #else if (event.which == 13 || event.keyCode == 13)
    #addTag()
    #true

    # remove last tag when backspace pressed
    tagInput.on 'keydown', (e) ->
      ths = $(this)
      if (e.keyCode == 8 && ths.val() == '')
        e.preventDefault()
        ths.parent().find('li:last').remove()
        tags.pop()
        updateValue()
        updatePlaceholder()

    # remove tag when "X" link clicked
    tagsList.on 'click','.remove-tag', (e) ->
      ths = $(this)
      e.preventDefault()
      tag = ths.prev().text()
      ths.parent().remove()
      tags.splice($.inArray(tag, tags), 1)
      updateValue()
      tagInput.focus()
      updatePlaceholder()

    # create tag (if valid) when pressed enter. event will be propagated further
    tagInput.closest('form').on 'submit', (e) ->
      addTag() # TODO: don't propagate when validation fails
      if (tags.length == 0)
        document.location=document.location.toString().split('?')[0]
        e.preventDefault()
        false
      else
        true