jQuery(=>
  $(document).ready(=>
    $('pre > code').each (i, el) =>
      # add classes for highlighting and line numbers
      $(el).parent().addClass('prettyprint').addClass('linenums')
    # turn on highlighting
    prettyPrint()
    # custom tags input field
    $('#tags_filter').tags_input()
  )
)

