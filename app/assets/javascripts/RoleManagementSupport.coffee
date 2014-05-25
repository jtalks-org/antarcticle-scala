jQuery(=>
  $(document).ready(=>
    $('.user-role').on('change', () ->
      $.ajax({
        type: "POST",
        url: $(this).attr('data-href'),
        contentType: 'application/json',
        data: JSON.stringify({ role: $(this).find(":selected").val()}),
        success: () =>
          showSuccessNotification("User role has been changed")
        error: () =>
          showFailureNotification("Operation cannot be performed due to insufficient permissions")
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




