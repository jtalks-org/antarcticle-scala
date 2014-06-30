jQuery(=>
  $(document).ready(=>
    $('.language-item').on 'click', (e) ->
      current = $(this).text()
      $('#language-button .lang-label').text(current)
      $('#language-button .lang-input').val(current)
      e.preventDefault()

  )
)