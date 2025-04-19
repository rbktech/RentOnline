#ifndef CBASESERVER_H
#define CBASESERVER_H

#include "listlink.h"

#include <unix/socket/tcpserver.h>

class CBaseServer
{
private:
    bool m_resolution;
    std::thread* m_thrLink;
    void ThreadLink(int port);

    virtual void FuncReceive(const uint8_t* data, const int& size, TCommand* cmd) = 0;
    virtual void AddLink(int socket) = 0;

protected:
    CTCPServer m_server;

    CListLink* m_listLink;

    void ThreadReceive(TCommand* cmd);

    std::function<void(int, const uint8_t*, const int&)> m_funcSend;

public:
    CBaseServer(CListLink* listLink, int port);
    virtual ~CBaseServer();
};

#endif // CBASESERVER_H
