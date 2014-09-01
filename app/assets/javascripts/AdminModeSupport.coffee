jQuery(=>
  $(document).ready(=>
    # old browsers may have no support local storage
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
  # change icon into pencil
  appNameContainer = $('#application-name-container')
  oldApplicationName = appNameContainer.text()
  $('#application-icon')
  .removeClass("glyphicon-book")
  .addClass("glyphicon-pencil")
  .click((e) ->
    e.preventDefault()
    appNameContainer.focus()
  )
  # make app name editable
  appNameContainer
  .attr('contenteditable', "true")
  .click((e) ->
    e.preventDefault()
  )
  .keypress((e) ->
    if e.keyCode == 13  # Enter
      e.preventDefault()
      submitData(this.textContent)
  )
  .keyup ((e) ->
    if e.keyCode == 27  # Escape
      appNameContainer.text(oldApplicationName).blur()
  )
  # alter menu item
  sessionStorage.adminMode = true
  $('#toggle-admin-mode')
  .text("Exit admin mode")
  .closest('li').click((e) ->
    exitAdminMode()
    e.preventDefault()
  )
  enableBannerEditor()

submitData = (newName) ->
  if !operationInProgress
    if (newName.length > 256)
      showFailureNotification("Application name cannot exceed 256 chars")
    else
      operationInProgress = true
      $.ajax({
        type: "POST",
        url: $('#toggle-admin-mode').closest('a').attr('data-href'),
        contentType: 'application/json',
        data: JSON.stringify({instanceName: newName}),
        statusCode: {
          401: () ->
            showFailureNotification("Your session has ended. Please refresh the page.")
            $('#application-name-container').focus()
        }
        success: () ->
          showSuccessNotification("Application name has been updated")
          $('#application-name-container').blur()
        error: (data) ->
          showFailureNotification(data)
          $('#application-name-container').focus()
        complete: () ->
          operationInProgress = false
      })

enableBannerEditor = () ->
  $('.banner-control').show()
  $('.page-banner').addClass("banner-border")
  $('button[data-banner-submit-href]').each(() ->
    $(this).click(() =>
      $.ajax({
        type: "POST",
        url: $(this).attr('data-banner-submit-href'),
        contentType: 'application/json',
        data: JSON.stringify({'codepenId': $(this).prevAll('input').val()}),
        success: () ->
          showSuccessNotification("Banner id has been updated, please refresh the page")
        error: (data) ->
          showFailureNotification(data)
      })
    )
  )


exitAdminMode = () ->
  sessionStorage.adminMode = false
  $('#application-icon')
  .removeClass("glyphicon-pencil")
  .addClass("glyphicon-book")
  .unbind('click')
  # turn app name editor into plain caption
  $('#application-name-container')
  .attr('contenteditable', "false")
  .unbind('click')
  .removeAttr('keypress')
  .removeAttr('keyup')
  # alter menu item
  $('#toggle-admin-mode')
  .text("Enter admin mode")
  .closest('li').click((e) ->
    enterAdminMode()
    e.preventDefault()
  )
  disableBannerEditor()

disableBannerEditor = () ->
  $('.banner-control').hide()
  $('.page-banner').removeClass("banner-border")