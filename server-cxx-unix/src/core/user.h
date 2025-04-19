#ifndef CCOMMAND_HPP
#define CCOMMAND_HPP

#include "basecommand.h"

struct TServerTime {
    uint8_t separate_1 = 0;
    uint8_t time_begin[10] = { 0 };
    uint8_t separate_2 = 0;
    uint8_t time_end[10] = { 0 };
    uint8_t separate_3 = 0;
    uint8_t user_id[10] = { 0 };
};

class CUser : public CBaseCommand
{
private:
    typedef bool (CUser::*FuncCommand)(const TParseData&, TCommand*);

    std::map<uint8_t, FuncCommand> m_command;

    void AddLink(int socket) override;

    void CommandProcess(const uint8_t& command, const TParseData& parse, TCommand* cmd) override;

    // Commands:
    // --------------------------------------------------------------------

    /**
     * @brief Command "L"
     * @param data
     * @param cmd
     * @return
     */
    bool ReadListFarm(const TParseData& data, TCommand* cmd); // +

    /**
     * @brief Command "R"
     * @param data
     * @param cmd
     * @return
     */
    bool ReadFarm(const TParseData& data, TCommand* cmd); // +

    /**
     * @brief Command "A"
     * @param data
     * @param cmd
     * @return
     */
    bool CreateAccount(const TParseData& data, TCommand* cmd); // +

    /**
     * @brief Command "C"
     * @param data
     * @param cmd
     * @return
     */
    bool CheckAccount(const TParseData& data, TCommand* cmd); // +

    /**
     * @brief Command "E"
     * @param data
     * @param cmd
     * @return
     */
    bool SignInAccount(const TParseData& data, TCommand* cmd); // +

    bool PayFarm(const TParseData& data, TCommand* cmd);

    /**
     * @brief Command "K"
     * @param data
     */
    bool ReadListKey(const TParseData& data, TCommand* cmd);

    // Unused
    // --------------------------------------------------------------------

    bool CreateNewFarm(const TParseData& data, TCommand* cmd);
    bool ReadListOwnerFarm(const TParseData& data, TCommand* cmd);

    // --------------------------------------------------------------------

    int PayScript(const uint8_t* token, const int sizeToken, int delay);

public:
    CUser(CListLink* listLink, const int& port);
    virtual ~CUser();
};

#endif // CCOMMAND_HPP
