function post(e) {
  const xhr = new XMLHttpRequest();
  const url = 'http://example.com/data';
  const data = {
    name: 'John',
    age: 30
  };
  
  xhr.open('POST', url);
  xhr.setRequestHeader('Content-Type', 'application/json');
  xhr.onload = function() {
    if (xhr.status === 200) {
      console.log(xhr.response);
    }
  
    xhr.send(JSON.stringify(data));
  }

  console.log('Кнопка была нажата! 5556');
};


function parseJson(e)
{
  e.preventDefault();

  const jsonString = '[{"name": "John", "age": 30}, {"name": "Jane", "age": 25}]';

  const data = JSON.parse(jsonString);
  data.forEach(function(item) {

    console.log(item.name + ' - ' + item.age);
  });
}



function sendFormSS(e) {
  e.preventDefault();

  try {

    const xhr = new XMLHttpRequest();
    xhr.open('GET', 'https://127.0.0.1/json?table=houses&item=*');
    // xhr.open('GET', '/json?table=houses&item=*');
    xhr.timeout = 5000;
    xhr.send();
    xhr.onload = function() {
      if (xhr.status != 200) { // анализируем HTTP-статус ответа, если статус не 200, то произошла ошибка
        alert(`Ошибка ${xhr.status}: ${xhr.statusText}`); // Например, 404: Not Found
      } else { // если всё прошло гладко, выводим результат
        alert(`Готово, получили ${xhr.response.length} байт`); // response -- это ответ сервера
      }
    };

    /*xhr.onreadystatechange = function(event) {
      //alert(`Готово, получили ${xhr.response.length} байт`); // response -- это ответ сервера

      if (xhr.readyState === 4) {
          if (xhr.status === 200) {
            const data = xhr.responseText;
            console.log(data);
          } else {
            // console.error('Ошибка ' + xhr.status);
          }
      }
    }*/

    /*xhr.onreadystatechange = function() {
      if (xhr.readyState === 4 && xhr.status === 200) {
        if (xhr.status === 200) {
          const data = JSON.parse(xhr.responseText);
          console.log(data);
      }
    };*/

    xhr.onreadystatechange = () => {
      // In local files, status is 0 upon success in Mozilla Firefox
      if (xhr.readyState === XMLHttpRequest.DONE) {
        const status = xhr.status;
        if (status === 0 || (status >= 200 && status < 400)) {
          // The request has been completed successfully
          console.log(`${xhr.responseText}`);
        } else {
          // Oh no! There has been an error with the request!
        }
      }
    };

    xhr.onprogress = function(event) {
      if (event.lengthComputable) {
        alert(`Получено ${event.loaded} из ${event.total} байт`);
      } else {
        alert(`Получено ${event.loaded} байт`); // если в ответе нет заголовка Content-Length
      }
    };

    xhr.onerror = function() {
      alert("Запрос не удался");
    };

  } catch(error) {
    console.error('Error fetching data:', error);
  }
}

document.addEventListener('DOMContentLoaded', function()
{

});


async function getJson(e) {
  e.preventDefault();

  // const response = await fetch('https://127.0.0.1/json?table=houses&item=*');
  // const response = fetch('https://127.0.0.1/json?table=houses&item=street');
  
  /*await fetch('https://127.0.0.1/json?table=houses&item=street')
  .then(response => response.json())
  .then(data => {
    console.log(data.street); // "John"
  });*/

  console.log("");

    try {

      await fetch('https://127.0.0.1/json?table=houses&item=preview')
      .then(response => response.json())
      .then(data => {

        console.log(data);

        // Создаем таблицу HTML
        const table = document.createElement('table');

        // Создаем заголовок таблицы
        const thead = document.createElement('thead');
        const tr = document.createElement('tr');
        for (const key in data[0]) {
          const th = document.createElement('th');
          th.textContent = key;
          tr.appendChild(th);
        }
        thead.appendChild(tr);
        table.appendChild(thead);

        // Создаем тело таблицы
        const tbody = document.createElement('tbody');
        data.forEach(item => {
          const tr = document.createElement('tr');
          for (const key in item) {
            const img = document.createElement('img');
            //td.textContent = item[key];
            img.src = "/image/" + item[key];
            tr.appendChild(img);
          }
          tbody.appendChild(tr);
        });
        table.appendChild(tbody);

        // Добавляем таблицу на страницу
        const tableContainer = document.getElementById('table-container');
        tableContainer.appendChild(table);
      })
      .catch(error => {
        // Обработка ошибок
        console.error('Ошибка:', error);
      });

      await fetch('https://127.0.0.1/json?table=houses&item=street')
  .then((response) => response.json())
  .then((data) => {

    for (const product of data.products) {
      const listItem = document.createElement("li");
      listItem.appendChild(document.createElement("strong")).textContent =
        product.Name;
      listItem.append(` can be found in ${product.Location}. Cost: `);
      listItem.appendChild(
        document.createElement("strong")
      ).textContent = `£${product.Price}`;
      myList.appendChild(listItem);
    }
  })
  .catch(console.error);


      await fetch('https://127.0.0.1/json?table=houses&item=street')
      .then(response => response.json())
      .then(data => {
        // Используйте полученные данные
        for (const product of data.products) {
          console.log(product.street);
        }
      })
      .catch(error => {
        // Обработка ошибок
        console.error('Ошибка:', error);
      });



            fetch('https://127.0.0.1/json?table=houses&item=street', {
                method: "GET",
                headers: {
                    "Content-Type": "application/json"
                }
                // body: JSON.stringify(myVar)
            })
  .then(function (response) {
    if (response.status !== 200) {
      return Promise.reject(new Error(response.statusText))
    }
    return Promise.resolve(response)
  })
  .then(function (response) {
    return response.json()
  })
  .then(function (datas) {

    const data = JSON.parse(datas);
    const myList = document.getElementById('myList');
    
    data.forEach(function(item) {
      const li = document.createElement('li');
      // li.textContent = item.name + ' - ' + item.age;
      li.textContent = item.street;
      myList.appendChild(li);
    });



    console.log('data', data)
  })
  .catch(function (error) {
    console.log('error', error)
  })

            await fetch('https://127.0.0.1/json?table=houses&item=*', {
                method: "GET",
                headers: {
                    "Content-Type": "application/json"
                }
                // body: JSON.stringify(myVar)
            })
  .then(function (response) {
    response.json().then(function (data) {
      console.log('data', data)
    })
  })

            await fetch('https://127.0.0.1/json?table=houses&item=*', {
                method: "GET",
                headers: {
                    "Content-Type": "application/json"
                }
                // body: JSON.stringify(myVar)
            })
                .then(response => response.json())
                .then(data => {
                    console.log(data);
                })
                .catch(error => {
                    console.error('Ошибка:', error);
                })


            const response = await fetch('https://127.0.0.1/json?table=houses&item=*', {
                method: "GET",
                headers: {
                    "Content-Type": "text/plain"
                }
                // body: JSON.stringify(myVar)
            })

            if (!response.ok) {
              throw new Error('Network response was not ok');
            }

            var text = response.text();
            const jsson = JSON.parse(text);
            console.log(jsson);
            // const json = await response.json();
            const json = response.json();
            console.log(json);
            console.log(`${json}`);
          } catch(error) {
            console.error('Error fetching data:', error);
          }
}