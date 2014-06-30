# Supports preview for markdown editors

class PreviewSupport

  inPreview = false;

  constructor: (@previewLinkSelector, @previewPanelSelector, @formSelector) ->
    $(@previewLinkSelector).click () =>
      $('.clear-on-resubmit').remove() # clear old validation messages, if any
      previewPanel = $(@previewPanelSelector)
      form = $(@formSelector)
      $.post($(@previewLinkSelector).attr('data-href'), form.serialize())
      .done((data) =>
        previewPanel.html(data)
        # highlight any possible code in preview
        hljs.initHighlighting.called = false;
        hljs.initHighlighting();
      )
      .fail((data) =>
        $(@formSelector).prepend(data.responseText)
      )

new PreviewSupport("#preview-tab", "#preview", ".default-form")




