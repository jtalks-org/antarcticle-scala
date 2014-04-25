jQuery(=>
  $(document).ready(=>
    $.get($("#notifications-link").attr('href'))
    .done((data) =>
      $("#notifications-dropdown").html(data)
    )
  )
)