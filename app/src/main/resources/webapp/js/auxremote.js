$(document).ready(function () {

  connect();
  pollMount();

  $(".btn").mouseup(function(){
      console.log('up')
      $(this).blur();
      $('#north').blur();
  });


    $("#indexSeek").click(function(e){
        e.preventDefault();
        $.ajax({type: "POST",
                url: "/api/mount",
                contentType:"application/json; charset=utf-8",
                dataType:"json",
                data: JSON.stringify({ 'pecMode': 'INDEXING'}),
                success:function(result){
          console.log('started pec indexing');
        }});
      });

    $("#pecRecord").click(function(e){
        e.preventDefault();
        $.ajax({type: "POST",
                url: "/api/mount",
                contentType:"application/json; charset=utf-8",
                dataType:"json",
                data: JSON.stringify({ 'pecMode': 'RECORDING'}),
                success:function(result){
          console.log('started recording');
        }});
      });

    $("#pecStopRecord").click(function(e){
        e.preventDefault();
        $.ajax({type: "POST",
                url: "/api/mount",
                contentType:"application/json; charset=utf-8",
                dataType:"json",
                data: JSON.stringify({ 'pecMode': 'IDLE'}),
                success:function(result){
          console.log('stopped recording');
        }});
      });

    $("#pecPlay").click(function(e){
        e.preventDefault();
        $.ajax({type: "POST",
                url: "/api/mount",
                contentType:"application/json; charset=utf-8",
                dataType:"json",
                data: JSON.stringify({ 'pecMode': 'PLAYING'}),
                success:function(result){
          console.log('started pec play');
        }});
      });

    $("#pecStopPlay").click(function(e){
        e.preventDefault();
        $.ajax({type: "POST",
                url: "/api/mount",
                contentType:"application/json; charset=utf-8",
                dataType:"json",
                data: JSON.stringify({ 'pecMode': 'IDLE'}),
                success:function(result){
          console.log('stoped pec playback');
        }});
      });

    $("#trackingOn").click(function(e){
          e.preventDefault();
        $.ajax({type: "POST",
                url: "/api/mount",
                contentType:"application/json; charset=utf-8",
                dataType:"json",
                data: JSON.stringify({ 'guideRate': 'SIDEREAL'}),
                success:function(result){
          console.log('started tracking');
        }});
      });

    $("#trackingOff").click(function(e){
          e.preventDefault();
        $.ajax({type: "POST",
                url: "/api/mount",
                contentType:"application/json; charset=utf-8",
                dataType:"json",
                data: JSON.stringify({ 'guideRate': 'OFF'}),
                success:function(result){
          console.log('stopped tracking');
        }});
      });

      function pollMount(){
          $.get('/api/mount', function(data) {
              console.log(data);
              setTimeout(pollMount,5000);
              $('#handsetScreen').val(data.raHours)
              $('#pecMode').val(data.pecMode)
              if(data.pecIndexFound) {
                $('#pecPlay').prop('disabled', false);
                $('#pecRecord').prop('disabled', false);
              } else {
                $('#pecPlay').prop('disabled', true);
                $('#pecRecord').prop('disabled', true);
              }
              if(data.pecMode == 'PLAYING') {
                $('#pecStopPlay').prop('disabled', false);
              } else {
                $('#pecStopPlay').prop('disabled', true);
              }
              if(data.pecMode == 'RECORDING') {
                 $('#pecStopRecord').prop('disabled', false);
              } else {
                 $('#pecStopRecord').prop('disabled', true);
              }
          });
      }

      function connect() {
          console.log('connecting');
            $.post('/api/mount/connect', function(data) {
                console.log(data);
            });
      }

});