const confirmation_token = localStorage.getItem('confirmation_token')
const idempotence_key = localStorage.getItem('idempotence_key')
const token = localStorage.getItem('token')

console.log(confirmation_token);
console.log(idempotence_key);

const checkout = new window.YooMoneyCheckoutWidget({

    confirmation_token: confirmation_token,

    // return_url: '/',

    customization: {
      colors: {
        //Цвет акцентных элементов: кнопка Заплатить, выбранные переключатели, опции и текстовые поля
        // control_primary: '#00BF96', //Значение цвета в HEX
        control_primary: '#808080',

        //Цвет платежной формы и ее элементов
        background: '#F2F3F5' //Значение цвета в HEX
      }
    },
    
    error_callback: function(error) {
        console.log(error)
    }
});

checkout.on('success', () => {

  console.log("success");

  postPayingBooking();

  checkout.destroy();
});

checkout.on('fail', () => {

  console.log("fail");

  checkout.destroy();
});

checkout.on('modal_close', () => {

  console.log("modal_close");
});

checkout.render('payment-form');

async function postPayingBooking() {

  let data = [{
    "idempotence_key": `${idempotence_key}`,
  }];
  
  let json = JSON.stringify(data);

  let response_code;
  
  await fetch('/paying', {

    method: "POST",
    headers: {
      "Authorization": `Bearer ${token}`
    },
    body: json
  })
  .then(response => {

    response_code = response.status;

    return response.json();
  })
  .then(data => {

    console.log(data);

    if(response_code === 200) {

      localStorage.removeItem('confirmation_token');
      localStorage.removeItem('idempotence_key');
      window.location.href = '/';
    } else {

    }

  })
  .catch(error => {
    console.error('Ошибка:', error);
  });
}