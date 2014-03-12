# Supports preview for markdown editors

class PreviewSupport

  inPreview = false;

  constructor: (@editPanelSelector, @previewPanelSelector, @formSelector, @previewButtonSelector) ->
    $(@previewButtonSelector).click (e) =>
      editPanel = $(@editPanelSelector)
      previewPanel = $(@previewPanelSelector)
      previewButton = $(@previewButtonSelector)
      form = $(@formSelector)
      $.post(previewButton.attr('href'), form.serialize())
      .done((data) =>
          if (inPreview)
            previewPanel.html("")
            previewButton.text("Show preview")
          else
            previewPanel.html(data)
            previewButton.text("Back to edit")
          inPreview = !inPreview
          editPanel.toggle()
        )
      .fail((data) =>
          $(@formSelector).prepend(data.responseText)
        )
      e.preventDefault()

new PreviewSupport(".hide-on-preview", ".preview-panel", ".default-form", ".preview-button")




