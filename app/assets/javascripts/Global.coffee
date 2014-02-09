jQuery(=>
  $(document).ready(=>
    $('pre > code').each (i, el) =>
      # language specific class
      lang = $(el).attr('class')
      # add classes for highlighting and line numbers
      $(el).parent().addClass('prettyprint').addClass('linenums').addClass("lang-#{lang}")
    # turn on highlighting
    prettyPrint()
    # custom tags input field
    $('#tags_filter').tags_input()
  )
)

