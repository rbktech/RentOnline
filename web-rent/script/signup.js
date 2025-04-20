const txtName = document.getElementById('ID_TXT_NAME');
const txtPassword = document.getElementById('ID_TXT_PASSWORD');
const lblStatus = document.getElementById('ID_LBL_STATUS');
lblStatus.style.display = "none";

async function postSignUp(content) {

  await fetch('/signup', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8'
    },
    body: content
  })
  .then(response => {

    if(response.status === 200) {
      lblStatus.style.display = "block";
      lblStatus.innerText = "Success";
      window.location.href = "/";
    } else {
      return response.json();
    }
  })
  .then(data => {
    if(Object.keys(data).length === 1) {
      const item = data[0];
      lblStatus.style.display = "block";
      lblStatus.innerText = item["error"];
    }
  })
  .catch(error => {

    lblStatus.style.display = "block";
    lblStatus.innerText = error;

    console.error('Ошибка:', error);
  });
}

const btnSignUp = document.getElementById('ID_BTN_SIGNUP');
btnSignUp.addEventListener('click', (e) => {
  e.preventDefault();

  const name = txtName.value;
  const password = txtPassword.value;

  if(name !== "") {
    if(password !== "") {

      lblStatus.innerText = "";
      lblStatus.style.display = "none";

      postSignUp(`login=${txtName.value}&password=${txtPassword.value}`);
    } else {
      lblStatus.innerText = "Passwod is empty";
      lblStatus.style.display = "block";
    }

  } else {
    lblStatus.innerText = "Name is empty";
    lblStatus.style.display = "block";
  }
});