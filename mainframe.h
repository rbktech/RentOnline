#ifndef CMAINFRAME_H
#define CMAINFRAME_H

#include "person.h"

#include <file.h>

// #include <socket/tcpclient.h>

class CMainFrame : public wxFrame
{
private:
	enum { TIMER_ID = wxID_HIGHEST + 1 };

	TPart m_part;
	TPerson m_person;

	bool m_resolution;
	bool m_setPerson;
	std::thread* m_thrClient;
	CTCPClient m_client;

	wxTimer m_timer;
	wxNotebook* m_notebook;

	void OnTimer(wxTimerEvent& event);

	void Init();
	void FuncReceiveData(const uint8_t* data, const int& size);

	void CheckNewAccount(wxCommandEvent& event);

	wxDECLARE_EVENT_TABLE();

public:
	CMainFrame();
	~CMainFrame();
};

#endif // CMAINFRAME_H
