#include "basecommand.h"

#define SIZE_TOWN 3
#define P_FOLDER 8
#define P_USERS P_FOLDER
#define P_FERMS P_FOLDER + SIZE_TOWN

int P_FOTO = P_FERMS + SIZE_GUID + 7;

#define PHT_BEGIN 19
#define PHT_ENDIN 29

void CSettings::InitAnswer(uint8_t command)
{
    *answer++ = 0xFA;
    *answer++ = 0xFB;
    *answer++ = command;
    SEPARATE_BUNDLE(answer);
    *answer++ = 1;
    SEPARATE_ITEM(answer);
    *answer++ = SERVER_ERROR;
    SEPARATE_ITEM(answer);
    *answer++ = COMMAND_ERROR;
    SEPARATE_ITEM(answer);
}

void CSettings::ClearPath()
{
    CBaseCommand::SetTown(pthPhoto);
    CBaseCommand::SetFarm(pthPhoto);

    CBaseCommand::SetDoubleTown(pthFarm, pthTFarm);
    CBaseCommand::SetDoubleFarm(pthFarm, pthTFarm);

    CBaseCommand::SetDoubleUser(pthUser, pthTUser);
}

CBaseCommand::CBaseCommand(CListLink* listLink, const int& port)
    : CBaseServer(listLink, port)
    , m_stg()
{
}

CBaseCommand::~CBaseCommand()
{
}

// ------------------------------------------------------------------------

void CBaseCommand::ClearBuffer(uint8_t* buffer, int& count, int& size, const int& sizeBuffer)
{
    count = 0;
    size = sizeBuffer;
    memset(buffer, 0, sizeBuffer);
}

TCommand* CBaseCommand::CreateLink(int socket, TListLink* list)
{
    TCommand* cmd = new TCommand();
    cmd->socket = socket;
    cmd->thread = new std::thread(&CBaseCommand::ThreadReceive, this, cmd);

    return cmd;
}

// ------------------------------------------------------------------------

void CBaseCommand::FuncReceive(const uint8_t* data, const int& size, TCommand* cmd)
{
    if(FillingMessage(data, size, m_part) == true) {

        if(m_part.array[0] == 0xFA && m_part.array[1] == 0xFB) {

            TParseData parse;

            ParseCommand(&m_part.array[3], m_part.pos, parse);
            if(parse.empty() == false) {

                m_stg.InitAnswer(m_part.array[2]);

                CommandProcess(m_part.array[2], parse, cmd);

                for(auto& p : parse) {
                    if(p.first != nullptr) {
                        delete[] p.first;
                        p.first = nullptr;
                    }
                }

                m_stg.Clear();
                parse.clear();

                // m_part.ClearBuf();
                m_part.ClearVal();
            }
        }
    }
}

// ------------------------------------------------------------------------

void CBaseCommand::SetUserPhoto(char* path, const uint8_t* hash)
{
    if(hash != nullptr)
        for(int i = PHT_BEGIN; i < PHT_ENDIN; i++)
            path[i] = *hash++;
    else
        for(int i = PHT_BEGIN; i < PHT_ENDIN; i++)
            path[i] = 0;
}

void CBaseCommand::SetTown(char* path, const uint8_t* town)
{
    if(town != nullptr)
        for(int i = P_FOLDER; i < P_FERMS; i++)
            path[i] = *town++;
    else
        for(int i = P_FOLDER; i < P_FERMS; i++)
            path[i] = 0;
}

void CBaseCommand::SetFarm(char* path, const uint8_t* ferm)
{
    if(ferm != nullptr)
        for(int i = P_FERMS + 1; i < P_FERMS + SIZE_GUID + 1; i++)
            path[i] = *ferm++;
    else
        for(int i = P_FERMS + 1; i < P_FERMS + SIZE_GUID + 1; i++)
            path[i] = 0;
}

void CBaseCommand::SetUser(char* path, const uint8_t* user)
{
    if(user != nullptr)
        for(int i = P_FOLDER; i < P_FOLDER + SIZE_GUID; i++)
            path[i] = *user++;
    else
        for(int i = P_FOLDER; i < P_FOLDER + SIZE_GUID; i++)
            path[i] = 0;
}

void CBaseCommand::SetDoubleTown(char* path_1, char* path_2, const uint8_t* town)
{
    if(town != nullptr)
        for(int i = P_FOLDER; i < P_FERMS; i++)
            path_1[i] = path_2[i] = *town++;
    else
        for(int i = P_FOLDER; i < P_FERMS; i++)
            path_1[i] = path_2[i] = 0;
}

void CBaseCommand::SetDoubleFarm(char* path_1, char* path_2, const uint8_t* ferm)
{
    if(ferm != nullptr)
        for(int i = P_FERMS + 1; i < P_FERMS + SIZE_GUID + 1; i++)
            path_1[i] = path_2[i] = *ferm++;
    else
        for(int i = P_FERMS + 1; i < P_FERMS + SIZE_GUID + 1; i++)
            path_1[i] = path_2[i] = 0;
}

void CBaseCommand::SetDoubleUser(char* path_1, char* path_2, const uint8_t* user)
{
    if(user != nullptr)
        for(int i = P_FOLDER; i < P_FOLDER + SIZE_GUID; i++)
            path_1[i] = path_2[i] = *user++;
    else
        for(int i = P_FOLDER; i < P_FOLDER + SIZE_GUID; i++)
            path_1[i] = path_2[i] = 0;
}

long CBaseCommand::CharToTime(const char* time_details)
{
    struct tm time_info;
    // strptime(time_details, "%d-%m-%Y %H:%M:%S", &time_info);
    return mktime(&time_info);
}

long CBaseCommand::ToTime(const uint8_t* date_time)
{
    try {
        return atoi((char*)date_time);
    } catch(const std::out_of_range& oor) {
        return -1;
    }
}
