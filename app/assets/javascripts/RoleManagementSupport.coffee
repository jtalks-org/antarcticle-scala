jQuery(=>
  $(document).ready(=>
    $('.user-role').on('change', () =>
      elem = $('.bb-alert ')
      elem.find("span").html("User role has been changed");
      elem.fadeIn().delay(4000).fadeOut();
    )


  )
)

