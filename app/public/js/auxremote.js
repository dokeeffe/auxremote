$(document).ready(function () {
  $(".btn").mouseup(function(){
      console.log('up')
      $(this).blur();
      $('#north').blur();
  });
});