#ifndef CLISTLINK_H
#define CLISTLINK_H

#ifdef WIN32

#include <winsock2.h>

#endif

#include <unix/socket/tcpbase.h>

#include <map>
#include <stdexcept>
#include <string>
#include <thread>

// typedef std::function<void(const int& sock, const uint8_t* message, const int& size)> TransitFunc;

struct TCommand {

#ifdef UNIX
    int socket = 0;
#endif

#ifdef WIN32
    SOCKET socket = 0;
#endif

    int wait = 0;
    std::thread* thread = nullptr;

    // TransitFunc func = nullptr;
};

typedef std::map<std::string, TCommand*> TListLink;

class CListLink
{

private:
    TCommand* Get(std::string id, TListLink& list)
    {
        try {

            return list.at(id);

        } catch(const std::out_of_range& oor) {

            return nullptr;
        }
    }

public:
    TListLink m_door;
    TListLink m_user;
    TListLink m_moderator;

    CListLink();
    ~CListLink();

    // void Insert(std::string id, TCommand* cmd, TListLink& list)
    // {
    // 	list.insert({ id, cmd });
    // }

    bool Erase(std::string id, TListLink& list)
    {
        TCommand* cmd = Get(id, list);
        if(cmd != nullptr) {

            CTCPBase::Disconnect(cmd->socket);
            cmd->thread->join();
            delete cmd->thread;

            list.erase(id);

            return true;
        }

        return false;
    }

    int GetSocket(std::string id, TListLink& list)
    {
        TCommand* cmd = Get(id, list);
        if(cmd != nullptr)
            return cmd->socket;

        return -1;
    }
};

#endif // CLISTLINK_H
