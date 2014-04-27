jQuery(=>
  $(document).ready(=>
    $.get($("#notifications-link").attr('href')).done((data) =>
      # load notifications on page load
      $("#notifications-dropdown").html(data)
      $(".notifications-badge").text($(".dropdown-header").attr("data-count"))
      # register user action listeners
      $("#drop-notifications-button").click(dismissAllNotifications)
      $(".dismiss-notification").click(dismissNotification)
    )
  )
)

dismissNotification = (e) ->
  $.ajax({
    url: $(this).attr('data-href'),
    type: 'DELETE'
  }).done( =>
    $(this).parent().parent().parent().remove()
    notificationsLeft = $(".notifications-badge").text() - 1
    $(".notifications-badge").text(if notificationsLeft == 0 then "" else notificationsLeft)
  )
  e.preventDefault()
  return false

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