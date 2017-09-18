
  $(document).ready(function(){

        console.log(1);
        if(loginError) {
            $('.log-status').addClass('wrong-entry');
            $('.alert').fadeIn(500);
            setTimeout( "$('.alert').fadeOut(1500);",3000 );
        }

        $('.form-control').keypress(function(){
            $('.log-status').removeClass('wrong-entry');
        });

    });