const timeStorage = [];
const dateStorage = [];

function convert(in_begin, in_end) {

  const begin = parseInt(in_begin);
  const end = parseInt(in_end);

  let size = end - begin;
  let count = size / 3600000;

  let date;

  for(let i = 0; i < count; i++) {

    let value = begin + (i * 3600000);

    date = new Date(value);
    date.setHours(0, 0, 0, 0);
    let day = date.getTime();
    
    if(dateStorage.indexOf(day) == -1)
      dateStorage.push(day);

    timeStorage.push(value);
  }
}

getListBooking();

function start_datepicker() {

  $(function() {

    var dateFormat = "dd-mm-yy";

    function highlightDate(date)
    {
      // const dateString = $.datepicker.formatDate("yy-mm-dd", date);

      if(dateStorage.includes(date.getTime())) {
        return [true, 'highlight-green'];
      } else {
        return [true];
      }
    }

    from = $( "#from" )
      .datepicker({
        dateFormat: dateFormat,
        changeMonth: true,
        numberOfMonths: 1,
        minDate: 0,
        beforeShowDay: highlightDate,
        showWeek: true,
        firstDay: 1,
      })
      .on( "change", function(e) {

        const time = $('#from').datepicker('getDate').getTime();

        showTimeFrom(timeStorage, time, time + 86400000);

        changeLabel();

        to.datepicker( "option", "minDate", getDate( this ) );
      });

    to = $( "#to" )
      .datepicker({
        dateFormat: dateFormat,
        changeMonth: true,
        numberOfMonths: 1,
        minDate: 0,
        beforeShowDay: highlightDate,
        showWeek: true,
        firstDay: 1,
      })
      .on( "change", function() {

        const time = $('#to').datepicker('getDate').getTime();

        showTimeTo(timeStorage, time, time + 86400000);

        changeLabel();

        from.datepicker( "option", "maxDate", getDate( this ) );
      });

    function getDate( element ) {
      var date;
      try {
        date = $.datepicker.parseDate( dateFormat, element.value );
      } catch( error ) {
        date = null;
      }

      return date;
    }
  });
}

async function getListBooking() {

  /*let data = [{
    "id": `${id_house}`
  }];*/

  await fetch(`/datebooking?instruction=HhRrK6d7f8gD1J2L&id=${id_house}`, {

    // method: "POST",
    /*headers: {
      "Authorization": `Bearer ${token}`
    },*/
    /*headers: {
      'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8'
    },*/
    // body: content
  })
  .then(response => {

    if(response.status === 200) {
      return response.json();

      // window.location.href = "/";
    } else {
      // return response.json();
    }
  })
  .then(data => {

    for(let i = 0; i < Object.keys(data).length; i++) {
      const item = data[i];
      convert(item.begin, item.end);
    }

    start_datepicker();
  })
  .catch(error => {
    console.error('Ошибка:', error);
  });
}

async function getInsertBooking(content) {

  let response_code;
  
  await fetch('/booking', {

    method: "POST",
    headers: {
      "Authorization": `Bearer ${token}`
    },
    body: content
  })
  .then(response => {

    response_code = response.status;

    return response.json();
  })
  .then(data => {

    console.log(data);

    if(response_code === 200) {

      if(Object.keys(data).length === 1) {
        const item = data[0];

        localStorage.setItem('confirmation_token', item.confirmation_token);
        localStorage.setItem('idempotence_key', item.idempotence_key);
        window.location.href = 'yapay.html';
      }

    } else {

    }

  })
  .catch(error => {
    console.error('Ошибка:', error);
  });
}

const btnBooking = document.getElementById("btnBooking");
btnBooking.addEventListener('click', (e) => {

  const time_from = $('#slider_from').slider("option", "value");
  const time_to = $('#slider_to').slider("option", "value");
  const data_from = $('#from').datepicker('getDate');
  const data_to = $('#to').datepicker('getDate');

  const mls_from = data_from.getTime() + time_from * 3600000;
  const mls_to = data_to.getTime() + time_to * 3600000;

  const price = parseInt(g_price) * ((mls_to - mls_from) / 3600000);

  let data = [{
    "from": `${mls_from}`,
    "to": `${mls_to}`,
    "id": `${id_house}`,
    "price": price,
    "description": g_description
  }];
  
  let json = JSON.stringify(data);

  getInsertBooking(json);
});

// ------------------------------------------------------------------------------

function changeLabel() {
  const lblCalendar = document.getElementById("ID_LBL_CALENDAR");
  const date_from = $( "#from" ).datepicker().val();
  const date_to = $( "#to" ).datepicker().val();

  const val_from = $('#slider_from').slider("option", "value");
  const val_to = $('#slider_to').slider("option", "value");

  var time_from;
  var time_to;

  if(val_from < 10)
    time_from = `0${val_from}:00`;
  else
    time_from = `${val_from}:00`;

  if(val_to < 10)
    time_to = `0${val_to}:00`;
  else
    time_to = `${val_to}:00`;

    lblCalendar.innerText = `${date_from} ${time_from} - ${date_to} ${time_to}`;
}

$(function() {
	$('#slider_from').slider({

    change: function(event, ui) {
      changeLabel();
    },

		min: 0,
		max: 23,
		value: 0
	});
});

$(function() {
	$('#slider_to').slider({

    change: function(event, ui) {
      changeLabel();
    },

		min: 0,
		max: 23,
		value: 0
	});
});

// -----------------------------

function showTimeFrom(array, data_begin, data_end) {
  for(var i = 0; i < 24; i++)
    document.getElementById(`from${i}`).style.backgroundColor = 'white';

  for(var i = 0; i < array.length; i++) {
    if(data_begin <= array[i] && array[i] < data_end) {

      const hour = (array[i] - data_begin) / 3600000;

      document.getElementById(`from${hour}`).style.backgroundColor = 'grey';
    }
  }
}

function showTimeTo(array, data_begin, data_end) {
  for(var i = 0; i < 24; i++)
    document.getElementById(`to${i}`).style.backgroundColor = 'white';

  for(var i = 0; i < array.length; i++) {
    if(data_begin <= array[i] && array[i] < data_end) {

      const hour = (array[i] - data_begin) / 3600000;

      document.getElementById(`to${hour}`).style.backgroundColor = 'grey';
    }
  }
}

$(document).ready(function() {
  let id = 0;
  var ticks = $('.slider-scale-tick');
  for (var i = 0; i < ticks.length - 1; i++) {
    var tick = ticks[i];
    if(i < 24)
      tick.insertAdjacentHTML('afterend', `<div id=from${id++} class="slider-scale-tick-line"></div>`);

    if(i == 24)
      id = 0;

    if(i > 24)
      tick.insertAdjacentHTML('afterend', `<div id=to${id++} class="slider-scale-tick-line"></div>`);

    tick.classList.add("fixed");
  }

  $('.slider-scale-tick-line').css({
    display: 'block',
    width: '10px',
    height: '5px',
    background: 'white',
    top: '50%',
    transform: 'translateY(-50%)'
  });
});