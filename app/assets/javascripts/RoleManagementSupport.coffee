jQuery(=>
  $(document).ready(=>
    $('.user-role').on('change', () ->
      $.ajax({
        type: "POST",
        url: $(this).attr('data-href'),
        contentType: 'application/json',
        data: JSON.stringify({ role: $(this).find(":selected").val()}),
        success: () =>
          showNotification("User role has been changed")
        fail: (data) =>
          showNotification(data)
      })
    )
    # search with empty field should not leave any artifacts in url, like '?search='
    $('.search-input').closest('form').on 'submit', (e) ->
      inputData = $('.search-input').val()
      if (inputData.trim() == "")
        document.location = document.location.toString().split('?')[0]
        e.preventDefault()
        false
      else
        true
  )
)

showNotification = (text) ->
  elem = $('.bb-alert ')
  elem.find("span").html(text);
  elem.fadeIn().delay(3000).fadeOut();


