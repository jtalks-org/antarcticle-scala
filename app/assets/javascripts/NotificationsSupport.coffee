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
    $.get($("#notifications-link").attr('href')).done((data) =>
      $("#notifications-dropdown").html(data)
      $(".notifications-badge").text($(".dropdown-header").attr("data-count"))
    )
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