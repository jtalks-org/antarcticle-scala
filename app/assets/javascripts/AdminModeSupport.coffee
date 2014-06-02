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

enterAdminMode = () ->
  $('#application-icon').removeClass("glyphicon-book").addClass("glyphicon-pencil")
  # make app name editable
  appNameContainer = $('#application-name-container')
  appNameContainer.attr('contenteditable', "true")
  appNameContainer.click((e) ->
    e.preventDefault()
  )
  appNameContainer.keypress((e) ->
    if (e.which == 13)
      appNameContainer.blur()
      #todo: submit data to server
      e.preventDefault()
  )
  # alter menu item
  menuItem = $('#toggle-admin-mode')
  menuItem.text("Exit admin mode")
  sessionStorage.adminMode = true
  menuItem.click((e) ->
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
  .unbind('click');
  # alter menu item
  $('#toggle-admin-mode')
  .text("Enter admin mode")
  .click((e) ->
    enterAdminMode()
    e.preventDefault()
  )