#ifndef CDOOR_H
#define CDOOR_H

#include "basecommand.h"

#define NOT_ACTIVATED_FERM 0
#define ACTIVATED_FERM 1

class CDoor : public CBaseCommand
{
private:
    typedef bool (CDoor::*FuncCommand)(const TParseData&, TCommand*);

    std::map<uint8_t, FuncCommand> m_command;

    void AddLink(int socket) override;

    void CommandProcess(const uint8_t& command, const TParseData& parse, TCommand* cmd) override;

    void FuncReceive(const uint8_t* data, const int& size, TCommand* cmd);

    // Commands:
    // --------------------------------------------------------------------

    void RefreshKey(const std::time_t current_time);

public:
    CDoor(CListLink* listLinkm, const int& port);
    virtual ~CDoor();

    void CloseConnect(int socket);

    // static void SetTagToDoor(const int& socket, const uint8_t* tag);
};

#endif // CDOOR_H
