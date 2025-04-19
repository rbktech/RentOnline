#ifndef CMODERATOR_H
#define CMODERATOR_H

#include "basecommand.h"

class CModerator : public CBaseCommand
{
private:
    typedef bool (CModerator::*FuncCommand)(const TParseData&);

    std::map<uint8_t, FuncCommand> m_command;

    void AddLink(int socket) override;

    void CommandProcess(const uint8_t& command, const TParseData& parse, TCommand* cmd) override;

    // --------------------------------------------------------------------

    // Commands:

    /**
     * @brief Command "A"
     * @param data
     */
    bool ResponseFromModerator(const TParseData& data);

    /**
     * @brief Command "D"
     * @param data
     */
    bool PermissionModerator(const TParseData& data);

    /**
     * @brief Command "C"
     * @param data
     */
    bool CheckNewAccount(const TParseData& data);

public:
    CModerator(CListLink* listLink, const int& port);
    virtual ~CModerator();
};

#endif // CMODERATOR_H
