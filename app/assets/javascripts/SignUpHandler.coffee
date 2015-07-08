new FormHandler('.signup-form', (data) =>
  $("body").css("cursor", "default")
  $('.signup-form').empty()
  $('.signup-form').prepend("<div class='alert alert-success'>Hey, see your activation link in your mailbox</div>")
)