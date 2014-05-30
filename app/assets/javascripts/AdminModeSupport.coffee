jQuery(=>
  $(document).ready(=>
    if(typeof(Storage) != "undefined")
      if ("true" == sessionStorage.adminMode)
        enterAdminMode()
      else
        exitAdminMode()
    else
      #fallback to cookies?
  )
)

enterAdminMode = () ->
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
  # turn app name editor into plain caption
  appNameContainer = $('#application-name-container')
  appNameContainer.attr('contenteditable', "false")
  appNameContainer.prop("onclick", null);
  # alter menu item
  menuItem = $('#toggle-admin-mode')
  menuItem.text("Enter admin mode")
  sessionStorage.adminMode = false
  menuItem.click((e) ->
    enterAdminMode()
    e.preventDefault()
  )