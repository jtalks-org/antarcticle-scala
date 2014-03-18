# Helper class to ease AJAX validation on forms.
# Every form with, suitable for selector given is processed. Regular submit is replaced
# with AJAX call to form's action URL.
#
# Default selector: class 'default-form'
# Default OK action: redirect to URL from server response body
# Default validation fail action: prepend server response content to the form, i.e. response should contain
# error description and markup. See also formErrors.scala.html template
#
# Selector and actions are customizable, just create your own FormHandler instance with
# all necessary suff set as constructor parameters.

class BaseFormHandler

  defaultOnSuccess: (data) =>
    $("body").css("cursor", "default")
    window.location.href = data
    if (window.location.href.split('#')[0] == data.split('#')[0])
      window.location.reload(true)

  defaultOnFail: (data) =>
    $('.clear-on-resubmit').remove() # clear old validation messages, if any
    this.form.prepend(data.responseText)
    $("body").css("cursor", "default")

class FormHandler extends BaseFormHandler

  constructor: (@selector, @onSuccess = this.defaultOnSuccess, @onFail = this.defaultOnFail) ->
    this.form = $(@selector)
    this.form.submit(=>
      $("body").css("cursor", "progress")
      $.post(this.form.attr('action'), this.form.serialize())
      .done((data) =>
          @onSuccess(data))
      .fail((data) =>
          @onFail(data))
      return false
    )

class GetFormHandler extends BaseFormHandler

  constructor: (@selector, @onSuccess = this.defaultOnSuccess, @onFail = this.defaultOnFail) ->
    this.form = $(@selector)
    this.form.submit(=>
      $("body").css("cursor", "progress")
      $.get(this.form.attr('action'), this.form.serialize())
      .done((data) =>
          @onSuccess(data))
      .fail((data) =>
          @onFail(data))
      return false
    )

new FormHandler('.default-form')
tagSearchHandler = new GetFormHandler('.tags-search-form')
tagSearchHandler.onSuccess = ((data) =>
  $("body").css("cursor", "default")
  document.body.innerHTML = data
)
tagSearchHandler.onFail = ((data) =>
  bootbox.alert(data.responseText))
