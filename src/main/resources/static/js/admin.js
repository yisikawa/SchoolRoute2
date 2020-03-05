$.extend( $.fn.dataTable.defaults, { 
        language: {
            url: "https://cdn.datatables.net/plug-ins/9dcbecd42ad/i18n/Japanese.json"
        } 
    });

$( "#tabs" ).tabs();

$("#sensorfile").on("change", function() {
  var file = $(this).prop('files')[0];
  if (file.size　> 100 * 1024 * 1024) {
      alert("file size error:" + (Math.round(file.size * 100 / 1024 / 1024) /100) + "MB[max:100MB]");
      $(this).replaceWith($(this).clone(true));
  }
});

$("#sensorfile").on("keyup", function() {
  var pressedKey = event.keyCode;
  if (pressedKey == 8 || pressedKey == 46) {
      $(this).replaceWith($(this).clone(true));
  }
});

$("#download input[type='radio']").checkboxradio({icon: false});

$("#download .dl-type").controlgroup();
