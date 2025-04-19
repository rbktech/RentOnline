#include "mainframe.h"

class wxMiniApp : public wxApp
{
public:
    virtual bool OnInit()
    {
        wxInitAllImageHandlers();
        return (new CMainFrame())->Show();
    }
};

IMPLEMENT_APP(wxMiniApp);
