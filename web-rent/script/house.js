const token = localStorage.getItem('token')
const id_house = localStorage.getItem('id')
const lblBooking = document.getElementById("ID_LBL_BOOKING");
const pnlTonenValid = document.getElementById("ID_PNL_TOKEN_VALID");

let g_price;
let g_description;

const lblAddress = document.getElementById("ID_LBL_ADDRESS");
const lblCountRoom = document.getElementById("ID_LBL_COUNT_ROOM");
const lblDistanceCenter = document.getElementById("ID_LBL_DISTANCE_CENTER");
const lblRating = document.getElementById("ID_LBL_RATING");
const lblPrice = document.getElementById("ID_LBL_PRICE");
const imgHouse = document.getElementById("ID_IMG_HOUSE");

if(token !== null && token.length !== 0) {
  pnlTonenValid.style.display = "block";
  lblBooking.style.display = "none";
} else {
  pnlTonenValid.style.display = "none";
  lblBooking.style.display = "block";
}

async function getHouse()
{
  await fetch(`/json?instruction=R2f5G8hLj7K9tPqN&table=houses&id=${id_house}`)
  .then(response => response.json())
  .then(data => {
    if(Object.keys(data).length === 1) {
      const item = data[0];
      
      g_price = item["price"];
      g_description = lblAddress.textContent = `${item["city"]} ${item["street"]} ${item["house"]} ${item["corps"]} ${item["apartment"]}`;
      lblCountRoom.textContent = `Количество комнат: ${item["count room"]}`;
      lblDistanceCenter.textContent = `Расстояние до центра: ${item["distance center"]}`;
      lblRating.textContent = `Рейтинг ${item["rating"]}`;
      lblPrice.textContent = `Цена: ${item["price"]}/час`;
      imgHouse.src = `/image/${item["preview"]}`;
    }
  })
  .catch(error => {
    console.error('Ошибка:', error);
  });
}

getHouse();

// ----------------------------------------------------------------------------------------------------------------------------------------------------
