let token = localStorage.getItem('token')
if(token === null)
  throw new Error('Error token is null');

// -----------------------------------------------------------------------

// Показать полупрозрачный DIV, чтобы затенить страницу
// (форма располагается не внутри него, а рядом, потому что она не должна быть полупрозрачной)
function showCover() {
  let coverDiv = document.createElement('div');
  coverDiv.id = 'cover-div';

  document.body.style.overflowY = 'hidden';

  document.body.append(coverDiv);
}

function hideCover() {
  document.getElementById('cover-div').remove();
  document.body.style.overflowY = '';
}

function showPrompt(text, callback) {
  showCover();
  let form = document.getElementById('prompt-form');
  let container = document.getElementById('prompt-form-container');
  document.getElementById('prompt-message').innerHTML = text;

  function complete() {
    hideCover();
    container.style.display = 'none';
    document.onkeydown = null;

    if(callback != null) {
      callback();
    }
  }

  form.onsubmit = function() {

    complete();
    return false;
  };

  document.onkeydown = function(e) {
    if (e.key == 'Escape' || e.key == 'Enter') {
      complete();
    }
  };

  container.style.display = 'block';
}

// -----------------------------------------------------------------------

const imgAccount = document.getElementById('imgAccount');
const txtLastName = document.getElementById('txtLastName');
const txtFirstName = document.getElementById('txtFirstName');
const txtFatherName = document.getElementById('txtFatherName');
const txtPassport = document.getElementById('txtPassport');

async function account() {

  let response_code;

  fetch('/account', {
    method: "GET",
    headers: {
      "Authorization": `Bearer ${token}`
    }
  })
  .then(response => {

    response_code = response.status;

    return response.json();
  })
  .then(data => {

    if(data !== null) {
      if(Object.keys(data).length === 1) {
        const item = data[0];

        if(response_code === 200) {

          console.log(`/image/${item['photo']}`);

          imgAccount.src = `/image/${item['photo']}`;
          txtLastName.value = item['last name'];
          txtFirstName.value = item['first name'];
          txtFatherName.value = item['father name'];
          txtPassport.value = item['passport'];
        } else {
          showPrompt(item['error'], function() {

            if(response_code === 401) {
              localStorage.removeItem("token");
              window.location.href = "/";
            }
          });
        }
      }
    }
  })
  .catch(error => {
    console.error("Error:", error);
  });
}
function client() {
}
function owner() {
}
function cleaner() {
}
function master() {
}

function openTab(evt, tabName) {
  var i, tabcontent, tablinks;
  tabcontent = document.getElementsByClassName("tabcontent");
  for (i = 0; i < tabcontent.length; i++) {
      tabcontent[i].style.display = "none";
  }
  tablinks = document.getElementsByClassName("tablinks");
  for (i = 0; i < tablinks.length; i++) {
      tablinks[i].className = tablinks[i].className.replace(" active", "");
  }
  document.getElementById(tabName).style.display = "block";
  evt.currentTarget.className += " active";

  if(token !== null) {

    if(tabName == 'Account') {
      account();
    } else if(tabName == 'Client') {
      client();
    } else if(tabName == 'Owner') {
      owner();
    } else if(tabName == 'Cleaner') {
      cleaner();
    } else if(tabName == 'Master') {
      master();
    }
  }
}

document.getElementById("defaultOpen").click();