let isEndOfPage = false;
let offsetHeight;
let countLoadHouse = 0;

function getHouse(e) {
  e.preventDefault();
  console.log("Нажата кнопка:", "id");
}

async function loadHouse()
{
  const tableContainer = document.querySelector('#table-container');
  const rowCount = tableContainer.childElementCount;

  if(countLoadHouse != rowCount)
    return;

  countLoadHouse++;

  await fetch('json?instruction=e4N8UyM7CzHg5D14&table=houses&item=date&offset=' + `${rowCount}`)
  .then(response => response.json())
  .then(data => {
    if(Object.keys(data).length === 1)
    {
      // Создаем таблицу HTML
      const table = document.createElement('table');

      // Создаем заголовок таблицы
      /*const thead = document.createElement('thead');
      const tr = document.createElement('tr');
      for (const key in data[0]) {
        const th = document.createElement('th');
        th.textContent = key;
        tr.appendChild(th);
      }
      thead.appendChild(tr);
      table.appendChild(thead);*/

      // Создаем тело таблицы
      const tbody = document.createElement('tbody');

      const item = data[0];
      if(item != null) {
        const tr = document.createElement('tr');
        if(tr != null) {

          let container = document.createElement("div");
          if(container != null) {
            container.className = "div_house";

            let button = document.createElement("button");
            if(button != null) {
              button.className = "btn_house"
              button.value = item["id"];
              button.addEventListener("click", function(event) {
                event.preventDefault();
                window.location.href = "web/house.html";
                localStorage.setItem('id', item['id']);
              });

              let image = document.createElement('img');
              image.src = `image/${item["preview"]}`;
              image.alt = "Picture with batton";
              button.appendChild(image);
            }
            container.appendChild(button);

            // Создаем контейнер для лейблов
            let labelsContainer = document.createElement("div");
            if(labelsContainer != null) {
              labelsContainer.className = "lbl_house";
              labelsContainer.style.justifyContent = "space-between";
              labelsContainer.style.display = "flex";

              // Создаем первый лейбл
              let label1 = document.createElement("label");
              label1.textContent = `${item["city"]} ${item["street"]} ${item["house"]} ${item["corps"]} ${item["apartment"]}`;
              labelsContainer.appendChild(label1);
              
              // Создаем второй лейбл
              let label2 = document.createElement("label");
              label2.textContent = `Рейтинг ${item["rating"]}`;
              labelsContainer.appendChild(label2);
            }
            container.appendChild(labelsContainer);

            labelsContainer = document.createElement("div");
            if(labelsContainer != null) {
              labelsContainer.className = "lbl_house";
              labelsContainer.style.justifyContent = "space-between";
              labelsContainer.style.display = "flex";

              let label3 = document.createElement("label");
              label3.textContent = `Количество комнат: ${item["count room"]}`;
              labelsContainer.appendChild(label3);
            }
            container.appendChild(labelsContainer);

            labelsContainer = document.createElement("div");
            if(labelsContainer != null) {
              labelsContainer.className = "lbl_house";
              labelsContainer.style.justifyContent = "space-between";
              labelsContainer.style.display = "flex";

              let label3 = document.createElement("label");
              label3.textContent = `Расстояние до центра: ${item["distance center"]}`;
              labelsContainer.appendChild(label3);
            }
            container.appendChild(labelsContainer);

            labelsContainer = document.createElement("div");
            if(labelsContainer != null) {
              labelsContainer.className = "lbl_house";
              labelsContainer.style.justifyContent = "space-between";
              labelsContainer.style.display = "flex";

              let label3 = document.createElement("label");
              label3.textContent = `Цена: ${item["price"]}/час`;
              labelsContainer.appendChild(label3);
            }
            container.appendChild(labelsContainer);

          }
          tr.appendChild(container);
        }
        tbody.appendChild(tr);
      }

      /*data.forEach(item => {
        const tr = document.createElement('tr');
        for (const key in item) {

          if(key == "preview") {

            const img = document.createElement('img');
            //td.textContent = item[key];
            img.src = "image/" + item[key];
            tr.appendChild(img);
          }
        }
        tbody.appendChild(tr);
      });*/
      table.appendChild(tbody);

      // Добавляем таблицу на страницу
      const tableContainer = document.getElementById('table-container');
      tableContainer.appendChild(table);
    }
  })
  .catch(error => {
    // Обработка ошибок
    console.error('Ошибка:', error);
  });
}

const btnUser = document.getElementById("ID_BTN_USER");
btnUser.addEventListener('click', () => {
  window.location.href = 'web/user.html';
});

const btnLogOut = document.getElementById("ID_BTN_LOGOUT");
btnLogOut.addEventListener('click', () => {
  localStorage.removeItem("token");
  window.location.href = "/";
});

const btnSignUp = document.getElementById("ID_BTN_SIGNUP");
btnSignUp.addEventListener('click', () => {
  window.location.href = 'web/signup.html';
});

const btnLogIn = document.getElementById("ID_BTN_LOGIN");
btnLogIn.addEventListener('click', () => {
  window.location.href = 'web/login.html';
});

const token = localStorage.getItem('token')
if(token !== null && token.length !== 0) {
  btnUser.style.display = "block";
  btnLogOut.style.display = "block";
  btnSignUp.style.display = "none";
  btnLogIn.style.display = "none";
} else {
  btnUser.style.display = "none";
  btnLogOut.style.display = "none";
  btnSignUp.style.display = "block";
  btnLogIn.style.display = "block";
}

// -----------------------------------------------------------------------

const interval = setInterval(() => {

  if(document.body.offsetHeight < window.innerHeight) {
    loadHouse();
  } else {
    clearInterval(interval)
  }

}, 0);

window.addEventListener('scroll', function()
{
  if(isEndOfPage === false && window.innerHeight + window.scrollY > document.body.offsetHeight)
  {
    isEndOfPage = true;
    loadHouse();
  }

  if(offsetHeight != document.body.offsetHeight) {
    offsetHeight = document.body.offsetHeight;
    isEndOfPage = false;
  }
});

// -----------------------------------------------------------------------

function start_map(data) {

ymaps.ready(function () {
  var myMap = new ymaps.Map('ID_MAP', {
          center: [55.751574, 37.573856],
          zoom: 11,
          behaviors: ['default', 'scrollZoom']
      }, {
          searchControlProvider: 'yandex#search'
      });

    clusterer = new ymaps.Clusterer({

      preset: 'islands#blueIcon',
      groupByCoordinates: false,
      clusterDisableClickZoom: true,
      clusterHideIconOnBalloonOpen: false,
      geoObjectHideIconOnBalloonOpen: false,
      clusterBalloonContentLayout: null,
    });

    getPointData = function(item) {
      return {
          // balloonContentHeader: '<font size=3><b><a target="_blank" href="https://yandex.com">Your link can be here</a></b></font>',
          // balloonContentHeader: '<font size=3>ул. Таллинская д. 11 корп. 1 кв. 57</font>',
          balloonContentHeader: `<font size=3>${item["street"]} ${item["house"]} ${item["corps"]} ${item["apartment"]}</font>`,

          // balloonContentBody: '<p>Your name: <input name="login"></p><p>The phone in the format 2xxx-xxx:  <input></p><p><input type="submit" value="Send"></p>',
          // balloonContentBody: '<img src="image.jpg" style="width: 300px; height: 150px"> <div><p class="short">Рейтинг 4.7</p> <p class="short">Цена: 250/час</p></div> <p class="short">Количество комнат: 3</p>',
          // balloonContentBody: `<img src="image/${item.preview}" style="width: 300px; height: 150px"> <p class="short">Рейтинг ${item.rating}</p> <p class="short">Цена: ${item.price}/час</p> <p class="short">Количество комнат: ${item["count room"]}</p>`,
          balloonContentBody: `<button class="btn_house" onclick="window.location.href = 'web/house.html'; localStorage.setItem('id', ${item.id})"> <img src="image/${item.preview}" style="width: 300px; height: 150px"></button> <p class="short">Рейтинг ${item.rating}</p> <p class="short">Цена: ${item.price}/час</p> <p class="short">Количество комнат: ${item["count room"]}</p>`,

          // balloonContentFooter: '<font size=1>Расстояние до центра: 8.5 км</font> balloon <strong> ' + index + '</strong>',
          // balloonContentFooter: '<font size=1>Расстояние до центра: 8.5 км</font>',
          balloonContentFooter: `<font size=1>Расстояние до центра: ${item["distance center"]} км</font>`,
          // clusterCaption: '&#x25cf House'
      };
    }

    getPointOptions = function () {
      return {

        // preset: 'islands#blueHomeIcon',
        iconLayout: 'default#image',
        iconImageHref: 'image/ic_home.png',
        iconImageSize: [32, 32],
        iconImageOffset: [-16, -16],
      };
    };

    geoObjects = [];

    data.forEach(item => {
      geoObjects.push(new ymaps.Placemark([item.latitude, item.longitude], getPointData(item), getPointOptions()));
    });

    clusterer.options.set({
      gridSize: 80,
      clusterDisableClickZoom: false
    });

    clusterer.add(geoObjects);
    myMap.geoObjects.add(clusterer);

    myMap.setBounds(clusterer.getBounds(), {
      checkZoomRange: true
    });
});

}

// ------------------------------------------------------------------------------------------------------------------------------------------------

async function getHouseGeo()
{
  fetch('/geo?instruction=L5mN7oP9qR1sT3Ug&table=houses&item=*')
  .then(response => {

    response_code = response.status;

    return response.json();
  })
  .then(data => {
    start_map(data);
  })
  .catch(error => {
    console.error("Error:", error);
  });
}

getHouseGeo();

// ------------------------------------------------------------------------------------------------------------------------------------------------

const bottomPanel = document.querySelector('.bottom-panel');

const inputSearch = document.getElementById("ID_INPUT_SEARCH");
const divMap = document.getElementById("ID_DIV_MAP");
const divList = document.getElementById("ID_DIV_LIST");
const btn_search = document.getElementById("ID_BTN_SEARCH");

inputSearch.addEventListener('click', () => {
  bottomPanel.classList.toggle('hidden');
  bottomPanel.style.height = bottomPanel.classList.contains('hidden') ? '0' : '100px';
});

let x_scroll;
let y_scrool;

const btn_map = document.getElementById("ID_BTN_MAP");
btn_map.addEventListener('click', () => {

  if (divMap.classList.contains('hidden')) {
    divMap.classList.remove('hidden');
    divList.classList.add('hidden');
    document.body.style.overflow = "hidden";

    x_scroll = window.scrollX;
    y_scrool = window.scrollY;
    window.scrollTo(0, 0);

    btn_map.innerText = "List";

  } else {
    divList.classList.remove('hidden');
    divMap.classList.add('hidden');
    document.body.style.overflow = "auto";
    window.scrollTo(x_scroll, y_scrool);

    btn_map.innerText = "Map";
  }
});