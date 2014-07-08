jQuery(=>
  $(document).ready(=>
    $('.language-item').on 'click', (e) ->
      $('#language-button .lang-label').text($(this).text())
      $('#language-button .lang-input').val($(this).attr('lang'))
      e.preventDefault()

  )
)