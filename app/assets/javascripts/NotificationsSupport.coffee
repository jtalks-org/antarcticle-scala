jQuery(=>
  $(document).ready(=>
    if ($("#notifications-link").size() > 0)  # if we don't have any notification controls don't bother initializing
      loadNotifications()
  )
)

dismissNotification = (e) ->
  $.ajax({
    url: $(this).attr('data-href'),
    type: 'DELETE'
  }).done(=>
    loadNotifications()
  )
  e.preventDefault()
  return false

loadNotifications = () ->
  $.get($("#notifications-link").attr('href')).done((data) =>
    # load notifications on page load
    $("#notifications-dropdown").html(data)
    $(".notifications-badge").text($(".dropdown-header").attr("data-count"))
    # register user action listeners
    $(".dismiss-notification").click(dismissNotification)
    $("#drop-notifications-button").click(dismissAllNotifications)
  )

dismissAllNotifications = (e) ->
  $.ajax({
    url: $(this).attr('href'),
    type: 'DELETE'
  }).done((data) =>
    $("#notifications-dropdown").html(data)
    $(".notifications-badge").text("")
  )
  e.preventDefault()
  return false