jQuery(=>
  $(document).ready(=>
    # old browsers may not support local storage
    if(typeof(Storage) != "undefined")
      if ($('#toggle-admin-mode').length > 0)
        if ("true" == sessionStorage.adminMode)
          enterAdminMode()
        else
          exitAdminMode()
      else
        # seems like we have no admin menu, make sure admin mode if off
        sessionStorage.adminMode = false
    else
      #todo: fallback to cookies?
  )
)

operationInProgress = false

enterAdminMode = () ->
  $('#application-icon').removeClass("glyphicon-book").addClass("glyphicon-pencil")
  # make app name editable
  $('#application-name-container')
  .attr('contenteditable', "true")
  .addClass("editable-content")
  .click((e) ->
    e.preventDefault()
  )
  .keypress((e) ->
    if (e.which == 13 && !operationInProgress)
      operationInProgress == true
      e.preventDefault()
      $.ajax({
        type: "POST",
        url: $('#toggle-admin-mode').closest('a').attr('data-href'),
        contentType: 'application/json',
        data: JSON.stringify({ instanceName: this.innerText}),
        success: () ->
          showSuccessNotification("Application name has been updated")
          $('#application-name-container').blur()
        error: (data) ->
          showFailureNotification(data)
          $('#application-name-container').focus()
      })

  )
  # alter menu item
  sessionStorage.adminMode = true
  $('#toggle-admin-mode')
  .text("Exit admin mode")
  .closest('li').click((e) ->
    exitAdminMode()
    e.preventDefault()
  )

exitAdminMode = () ->
  sessionStorage.adminMode = false
  $('#application-icon')
  .removeClass("glyphicon-pencil")
  .addClass("glyphicon-book")
  # turn app name editor into plain caption
  $('#application-name-container')
  .attr('contenteditable', "false")
  .removeClass("editable-content")
  .unbind('click')
  .unbind('keypress')
  # alter menu item
  $('#toggle-admin-mode')
  .text("Enter admin mode")
  .closest('li').click((e) ->
    enterAdminMode()
    e.preventDefault()
  )