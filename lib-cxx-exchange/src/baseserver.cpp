#include "baseserver.h"

CBaseServer::CBaseServer(CListLink* listLink, int port)
    : m_listLink(listLink)
{
    m_resolution = true;
    m_thrLink = new std::thread(&CBaseServer::ThreadLink, this, port);

    m_funcSend = [=](int sock, const uint8_t* sendArray, const int& sizeSend) {
        m_server.Send(sock, sendArray, sizeSend);
    };
}

CBaseServer::~CBaseServer()
{
    m_resolution = false;

    m_server.Disconnect(m_server.GetSocket());
    m_thrLink->join();
    delete m_thrLink;
}

// ------------------------------------------------------------------------

void CBaseServer::ThreadLink(int port)
{
    int sock = 0;

    while(m_resolution == true)

        if(m_server.Connect("127.0.0.1", port) == 0)

            while((sock = m_server.Accept()) > 0)
                AddLink(sock);
}

// ------------------------------------------------------------------------

void CBaseServer::ThreadReceive(TCommand* cmd)
{
    m_server.Recv(cmd->socket, [=](const uint8_t* data, const int& size) { FuncReceive(data, size, cmd); });
}
