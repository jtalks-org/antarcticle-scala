# Supports preview for markdown editors

class PreviewSupport

  inPreview = false;

  constructor: (@previewLinkSelector, @previewPanelSelector, @formSelector) ->
    $(@previewLinkSelector).click (e) =>
      previewPanel = $(@previewPanelSelector)
      form = $(@formSelector)
      $.post($(@previewLinkSelector).attr('data-href'), form.serialize())
      .done((data) =>
        previewPanel.html(data)
      )
      .fail((data) =>
        $(@formSelector).prepend(data.responseText)
      )

new PreviewSupport("#preview-tab", "#preview", ".default-form")




