# Helper class to serve removal links.
# It's a good practive to use HTTP DELETE for removal operations. However, it requires
# a form to be created on the page, while other form functionality is not required.
# This handler transforms click on the delete link into HTTP DELETE request to the libk's target.
#
# Default selector: class 'delete-link'
# Default OK action: redirect to URL from server response body
#
# Selector and action are customizable, just create your own DeleteLinkHandler instance with
# all necessary suff set as constructor parameters.

class DeleteLinkHandler

  defaultOnSuccess: (data) => window.location = data

  constructor: (@selector, @onSuccess = this.defaultOnSuccess) ->
    that = this
    $(@selector).click( ->
      bootbox.confirm("Are you sure? This operation cannot be undone", (result) =>
        if result
          $.ajax({
            url: $(this).attr('href'),
            type: 'DELETE'
          })
          .done((data) =>  that.onSuccess(data))
          .fail((data) => console.log("Operation " + $(this).attr('href') + " failed: " + data))
      )
      return false
    )

new DeleteLinkHandler('.delete-link')