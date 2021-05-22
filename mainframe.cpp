#include "mainframe.h"

// #include "time.h"

#define IP_CONNECT "192.168.1.66"
// #define IP_CONNECT "127.0.0.1"

BEGIN_EVENT_TABLE(CMainFrame, wxFrame)
	EVT_TIMER(TIMER_ID, CMainFrame::OnTimer)
END_EVENT_TABLE()

CMainFrame::CMainFrame()
	: wxFrame(nullptr, NewControlId(), wxT("Moderator"))
	, m_timer(this, TIMER_ID)
{
	m_resolution = true;
	m_setPerson = false;

	wxButton* btnCheck = new wxButton(this, NewControlId(), wxT("Check"));

	m_notebook = new wxNotebook(this, NewControlId());

	wxBoxSizer* main_box = new wxBoxSizer(wxVERTICAL);
	main_box->Add(btnCheck);
	main_box->Add(m_notebook, 1, wxEXPAND);
	SetSizerAndFit(main_box);

	SetSize(wxSize(800, 600));

	m_thrClient = new std::thread(&CMainFrame::Init, this);

	m_timer.Start(1000);

	srand(time(0));

	Bind(wxEVT_BUTTON, CMainFrame::CheckNewAccount, this, btnCheck->GetId());
}

CMainFrame::~CMainFrame()
{
	m_timer.Stop();
	m_resolution = false;
	m_client.Disconnect(m_client.GetSocket());
	m_thrClient->join();
	delete m_thrClient;
}

void CMainFrame::OnTimer(wxTimerEvent& event)
{
	if(m_setPerson == true) {
		m_setPerson = false;
		m_notebook->AddPage(new CPerson(m_notebook, m_person, &m_client), m_person.id);
	}
}

void CMainFrame::Init()
{
	while(m_resolution != false) {
		if(m_client.Connect(IP_CONNECT, 6000) == 0) {
			SOCKET id = m_client.GetSocket();
			m_client.Recv(id, [this](const uint8_t* data, const int& size) {
				FuncReceiveData(data, size);
			});
		}
	}
}

void CMainFrame::FuncReceiveData(const uint8_t* data, const int& size)
{
	if(FillingMessage(data, size, m_part) == true) {
		TParseData parse;

		ParseCommand(&m_part.array[3], m_part.pos, parse);

		// CFile::CreateWriteFile("ss.txt", (char*)m_part.array, m_part.pos);

		m_part.ClearVal();

		if(parse.size() == 7) {

			printf("ss");

			char picture[20] = { 0 };
			memcpy(&picture[16], ".jpg", 4);
			for(int i = 0; i < 15; i++)
				picture[i] = (rand() % 9 + 0) + '0';

			m_person.id = picture;

			m_person.guid = wxString(parse.at(3).first, parse.at(3).second);
			m_person.mail = wxString(parse.at(4).first, parse.at(4).second);
			m_person.password = wxString(parse.at(5).first, parse.at(5).second);

			// m_person.surname = wxString(parse.at(6).first, parse.at(6).second);
			// m_person.name = wxString(parse.at(7).first, parse.at(7).second);
			m_person.namePhoto = wxString::Format(wxT("./picture/%s.jpg"), picture);

			if(CFile::CreateWriteFile(m_person.namePhoto, (char*)parse.at(6).first, parse.at(6).second) == 0) {
				m_setPerson = true;

				/*int count = 0;
				uint8_t send[256] = { 0 };

				send[count++] = 0xFA;
				send[count++] = 0xFB;
				send[count++] = 0x41;

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

				send[count++] = 1;

				send[count++] = 0xFA;
				send[count++] = 0xFB;
				send[count++] = 0x60;

				memcpy(&send[count], m_person.guid.data(), m_person.guid.size());
				count += m_person.guid.size();

				send[count++] = 0xFA;
				send[count++] = 0xFB;
				send[count++] = 0x60;

				memcpy(&send[count], m_person.mail.data(), m_person.mail.size());
				count += m_person.mail.size();

				send[count++] = 0xFA;
				send[count++] = 0xFB;
				send[count++] = 0x60;

				send[count++] = 0xFA;
				send[count++] = 0xFB;
				send[count++] = 0xFF;
				send[count++] = '\n';

				SendFunc func = [=](int sock, const uint8_t* data, const int& size) {
					m_client.Send(sock, data, size);
				};

				uint8_t array[SIZE_TRAFFIC] = { 0 };
				int size = 0;

				SliceMessage(send, count, array, size, m_client.GetSocket(), func);*/
			}
		}
	}
}

void CMainFrame::CheckNewAccount(wxCommandEvent& event)
{
	int count = 0;
	uint8_t send[256] = { 0 };

	send[count++] = 0xFA;
	send[count++] = 0xFB;
	send[count++] = 0x43;

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

	send[count++] = 1;

	send[count++] = 0xFA;
	send[count++] = 0xFB;
	send[count++] = 0x60;

	/*memcpy(&send[count], m_person.guid.data(), m_person.guid.size());
	count += m_person.guid.size();

	send[count++] = 0xFA;
	send[count++] = 0xFB;
	send[count++] = 0x60;

	memcpy(&send[count], m_person.mail.data(), m_person.mail.size());
	count += m_person.mail.size();

	send[count++] = 0xFA;
	send[count++] = 0xFB;
	send[count++] = 0x60;*/

	send[count++] = 0xFA;
	send[count++] = 0xFB;
	send[count++] = 0xFF;
	send[count++] = '\n';

	SendFunc func = [=](int sock, const uint8_t* data, const int& size) {
		m_client.Send(sock, data, size);
	};

	uint8_t array[SIZE_TRAFFIC] = { 0 };
	int size = 0;

	SliceMessage(send, count, array, size, m_client.GetSocket(), func);
}
