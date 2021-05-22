#include "person.h"

CPerson::CPerson(wxWindow* parent, const TPerson& person, CTCPClient* client)
	: wxPanel(parent)
{
	m_client = client;

	m_guid = person.guid;
	m_mail = person.mail;
	m_password = person.password;
	m_path = person.namePhoto;

	wxButton* no = new wxButton(this, BTN_ID_NO, wxT("No"));
	wxButton* yes = new wxButton(this, BTN_ID_YES, wxT("Yes"));

	wxTextCtrl* surname = new wxTextCtrl(this, NewControlId(), person.name);
	wxTextCtrl* name = new wxTextCtrl(this, NewControlId(), person.surname);
	// wxTextCtrl* patronymic = new wxTextCtrl(this, NewControlId());

	wxBoxSizer* h_box = nullptr;
	// wxBoxSizer* v_box = nullptr;
	wxFlexGridSizer* gr_box = nullptr;
	wxBoxSizer* main_box = new wxBoxSizer(wxVERTICAL);

	h_box = new wxBoxSizer(wxHORIZONTAL);

	gr_box = new wxFlexGridSizer(2, 2, 0, 0);
	gr_box->Add(new wxStaticText(this, wxID_ANY, wxT("Surname")));
	gr_box->Add(surname);
	gr_box->Add(new wxStaticText(this, wxID_ANY, wxT("Name")));
	gr_box->Add(name);

	h_box->Add(gr_box);
	h_box->Add(InitImage(person.namePhoto));

	main_box->Add(h_box);

	h_box = new wxBoxSizer(wxHORIZONTAL);
	h_box->Add(no, 1);
	h_box->Add(yes, 1);
	main_box->Add(h_box, 0, wxEXPAND);

	Bind(wxEVT_BUTTON, CPerson::OnPermissionModerator, this, BTN_ID_NO);
	Bind(wxEVT_BUTTON, CPerson::OnPermissionModerator, this, BTN_ID_YES);

	SetSizerAndFit(main_box);
}

CPerson::~CPerson()
{
}

void CPerson::OnPermissionModerator(wxCommandEvent& event)
{
	int count = 0;
	uint8_t send[256] = { 0 };

	send[count++] = 0xFA;
	send[count++] = 0xFB;
	send[count++] = 0x44; // D

	send[count++] = 0xFA; // NEW BUNDLE
	send[count++] = 0xFB;
	send[count++] = 0x78;

	send[count++] = 1; // NUMBER BUNDLE

	send[count++] = 0xFA;
	send[count++] = 0xFB;
	send[count++] = 0x60;

	send[count++] = 11; // SERVER ERROR

	send[count++] = 0xFA;
	send[count++] = 0xFB;
	send[count++] = 0x60;

	send[count++] = 21; // COMMAND ERROR

	send[count++] = 0xFA;
	send[count++] = 0xFB;
	send[count++] = 0x60;

	send[count++] = 0xFA; // NEW BUNDLE
	send[count++] = 0xFB;
	send[count++] = 0x78;

	send[count++] = 2;

	send[count++] = 0xFA;
	send[count++] = 0xFB;
	send[count++] = 0x60;

	if(event.GetId() == BTN_ID_YES)
		send[count++] = 0;
	else if(event.GetId() == BTN_ID_NO)
		send[count++] = 1;

	send[count++] = 0xFA;
	send[count++] = 0xFB;
	send[count++] = 0x60;

	memcpy(&send[count], m_guid.data(), m_guid.size());
	count += m_guid.size();

	send[count++] = 0xFA;
	send[count++] = 0xFB;
	send[count++] = 0x60;

	send[count++] = 0xFA;
	send[count++] = 0xFB;
	send[count++] = 0xFF;
	send[count++] = '\n';

	uint8_t array[1100] = { 0 };
	int size = 0;

	SliceMessage(send, count, array, size, m_client->GetSocket(), [=](int sock, const uint8_t* data, const int& size) {
		m_client->Send(sock, data, size);
	});
}

wxStaticBitmap* CPerson::InitImage(wxString namePhoto)
{
	wxImage img(namePhoto, wxBITMAP_TYPE_JPEG);
	wxSize size = img.GetSize();

	int x = size.x / 400;
	int y = size.y / 400;

	if(y > 2) {

		x = size.x / x;
		y = size.y / y;

		img.Rescale(x, y);
	}

	wxStaticBitmap* btm = new wxStaticBitmap(this, NewControlId(), wxBitmap(img));
	btm->SetMinSize(wxSize(400, 400));
	btm->SetMaxSize(wxSize(400, 400));

	return btm;
}
