#ifndef CPERSON_H
#define CPERSON_H

#include <wx/wx.h>
#include <wx/notebook.h>

#include <assembly.h>

#include <socket/tcpclient.h>

struct TPerson {
	wxString id;

	wxString guid;
	wxString surname;
	wxString name;
	wxString mail;
	wxString password;
	wxString namePhoto;

	std::vector<int> delPage;
};

enum { BTN_ID_YES = wxID_HIGHEST + 1, BTN_ID_NO };

class CPerson : public wxPanel
{
private:
	wxStaticBitmap* InitImage(wxString namePhoto);

	CTCPClient* m_client;

	wxString m_mail;
	wxString m_guid;
	wxString m_password;
	wxString m_path;

	TPerson* m_person;
	wxNotebook* m_parent;

	void OnPermissionModerator(wxCommandEvent& event);

public:
	CPerson(wxNotebook* parent, TPerson* person, CTCPClient* client);
	~CPerson();
};

#endif // CPERSON_H
