jQuery(=>
  $(document).ready(=>
    $.get($("#notifications-link").attr('href')).done((data) =>
      $("#notifications-dropdown").html(data)
      $("#drop-notifications-button").click((e) ->
        $.ajax({
          url: $(this).attr('href'),
          type: 'DELETE'
        }).done((data) =>
          $("#notifications-dropdown").html(data)
        )
        e.preventDefault()
        return false
      )
    )
  )
)