const txtName = document.getElementById('ID_TXT_NAME');
const txtPassword = document.getElementById('ID_TXT_PASSWORD');
const lblStatus = document.getElementById('ID_LBL_STATUS');
lblStatus.style.display = "none";

async function getLogin(url) {

  let response_code;

  await fetch(url)
  .then(response => {

    response_code = response.status;

    return response.json();
  })
  .then(data => {

    console.log(data);

    if(Object.keys(data).length === 1) {

      lblStatus.style.display = "block";

      const item = data[0];
      if(response_code === 200) {
        lblStatus.innerText = "Success";
        localStorage.setItem('token', item["token"]);
        window.location.href = "/";
      } else {
        lblStatus.innerText = item["error"];
      }
    }
  })
  .catch(error => {

    lblStatus.innerText = error;
    lblStatus.style.display = "block";

    console.error('Ошибка:', error);
  });
}

const btnLogin = document.getElementById('ID_BTN_LOGIN');
btnLogin.addEventListener('click', (e) => {
  e.preventDefault();

  const name = txtName.value;
  const password = txtPassword.value;

  if(name !== "") {
    if(password !== "") {

      lblStatus.innerText = "";
      lblStatus.style.display = "none";

      getLogin(`/login?name=${txtName.value}&password=${txtPassword.value}`)
    } else {
      lblStatus.innerText = "Passwod is empty";
      lblStatus.style.display = "block";
    }

  } else {
    lblStatus.innerText = "Name is empty";
    lblStatus.style.display = "block";
  }
});