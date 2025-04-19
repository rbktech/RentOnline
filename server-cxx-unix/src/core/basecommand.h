#ifndef CBASECOMMAND_H
#define CBASECOMMAND_H

#include "assembly.h"
#include <baseserver.h>

#include <cstdint>
#include <ctime>
#include <dirent.h>
#include <map>
#include <string>

#include <unix/dir.h>
#include <unix/file.h>
#include <unix/socket/tcpbase.h>

#define MAX_SIZE_SEND 1024

#define FILE_SIZE 512

#define SIZE_TRAFFIC 256

// #define SIZE_TIME 10

#define SEPARATE_ITEM(answer) \
    *answer++ = 0xFA;         \
    *answer++ = 0xFB;         \
    *answer++ = 0x60;
// #define SEPARATE_RESULT(answer) *answer++ = 0xFA; *answer++ = 0xFB; *answer++ = 0x22;
// #define SEPARATE_SIZE(answer) answer++; *answer++ = 0xFA; *answer++ = 0xFB; *answer++ = 0x45;
#define SEPARATE_BUNDLE(answer) \
    *answer++ = 0xFA;           \
    *answer++ = 0xFB;           \
    *answer++ = 0x78;
#define SEPARATE_END(answer) \
    *answer++ = 0xFA;        \
    *answer++ = 0xFB;        \
    *answer++ = 0xFF;        \
    *answer++ = '\n';

#define SEPARATE_ITEM_COUNT(answer, number_param) \
    *answer++ = 0xFA;                             \
    *answer++ = 0xFB;                             \
    *answer++ = 0x60;                             \
    (*number_param)++;

#define SEPARATE_BUNDLE_COUNT(answer, number_param, size_bundle) \
    *answer++ = 0xFA;                                            \
    *answer++ = 0xFB;                                            \
    *answer++ = 0x78;                                            \
    number_param = answer++;                                     \
    SEPARATE_ITEM_COUNT(answer, number_param);                   \
    (*size_bundle)++;

#define SEPARATE_A(answer) \
    *answer++ = 0xFA;      \
    *answer++ = 0xFB;      \
    *answer++ = 0x41;

#define SEPARATE_L(answer) \
    *answer++ = 0xFA;      \
    *answer++ = 0xFB;      \
    *answer++ = 0x4C;

#define SEPARATE_R(answer) \
    *answer++ = 0xFA;      \
    *answer++ = 0xFB;      \
    *answer++ = 0x52;

#define SEPARATE_K(answer) \
    *answer++ = 0xFA;      \
    *answer++ = 0xFB;      \
    *answer++ = 0x4B;

#define SEPARATE_P(answer) \
    *answer++ = 0xFA;      \
    *answer++ = 0xFB;      \
    *answer++ = 0x50;

#define SEPARATE_O(answer) \
    *answer++ = 0xFA;      \
    *answer++ = 0xFB;      \
    *answer++ = 0x4F;

#define SEPARATE_S(answer) \
    *answer++ = 0xFA;      \
    *answer++ = 0xFB;      \
    *answer++ = 0x53;

#define SEPARATE_E(answer) \
    *answer++ = 0xFA;      \
    *answer++ = 0xFB;      \
    *answer++ = 0x45;

#define PAYMENT_SUCCEEDED 0
#define PAYMENT_CANCELED 1
#define PAYMENT_WAITING_FOR_CAPTURE 2
#define REFUND_SUCCEEDED 3

// #define SIZE_MARK_DOOR 5

// ------------------------------------------------------------------------

extern int P_FOTO;

extern const char* PRF_USER;
extern const char* PRF_FARM;

extern std::string GetGuid(uint8_t* array, const char* prefix);
extern void GetHash(uint8_t* array, const char* prefix);

// ------------------------------------------------------------------------

extern int PORT_MODERATOR;
extern int PORT_USER;
extern int PORT_DOOR;

// ------------------------------------------------------------------------

extern int SIZE_BUFFER;
extern int SIZE_GUID;
extern int SIZE_HASH;
extern int SIZE_DATA;

// ------------------------------------------------------------------------

extern int SERVER_SUCCESS;
extern int SERVER_ERROR;

extern int COMMAND_HEADER_SIZE;

extern int COMMAND_SUCCESS;
extern int COMMAND_ERROR;

extern int COMMAND_NOT_FOUND;

extern int COMMAND_READ_FARM_BEGIN;
extern int COMMAND_READ_FARM_NEXT;
extern int COMMAND_READ_FARM_END;

extern int ERROR_MIN_ITEM;

// ------------------------------------------------------------------------

struct CSettings {

private:
    uint8_t m_arrayAnswer[MAX_SIZE_SEND] = { 0 };

    void ClearPath();

public:
    TCommand* m_cmd = nullptr;

    char pthPhoto[34] = { '.', '/', 'f', 'e', 'r', 'm', 's', '/', '\0', '\0', '\0', '/', '\0', '\0', '\0', '\0', '\0',
        '\0', '\0', '\0', '\0', '\0', '/', 'f', 'o', 't', 'o', '_', '0', '.', 'j', 'p', 'g', '\0' };
    char pthFarm[32] = { '.', '/', 'f', 'e', 'r', 'm', 's', '/', '\0', '\0', '\0', '/', '\0', '\0', '\0', '\0', '\0',
        '\0', '\0', '\0', '\0', '\0', '/', 'f', 'i', 'l', 'e', '.', 't', 'x', 't', '\0' };
    char pthTFarm[33] = { '.', '/', 'f', 'e', 'r', 'm', 's', '/', '\0', '\0', '\0', '/', '\0', '\0', '\0', '\0', '\0',
        '\0', '\0', '\0', '\0', '\0', '/', '_', 'f', 'i', 'l', 'e', '.', 't', 'x', 't', '\0' };

    char pthUser[28] = { '.', '/', 'u', 's', 'e', 'r', 's', '/', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
        '\0', '/', 'f', 'i', 'l', 'e', '.', 't', 'x', 't', '\0' };
    char pthTUser[29] = { '.', '/', 'u', 's', 'e', 'r', 's', '/', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
        '\0', '/', '_', 'f', 'i', 'l', 'e', '.', 't', 'x', 't', '\0' };
    char pthUserDir[19] = { '.', '/', 'u', 's', 'e', 'r', 's', '/', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
        '\0', '\0', '\0' };
    char pthUserPhoto[34] = { '.', '/', 'u', 's', 'e', 'r', 's', '/', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
        '\0', '\0', '/', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '.', 'j', 'p', 'g', '\0' };

    uint8_t* answer = m_arrayAnswer;
    uint8_t* a_numberParams = &m_arrayAnswer[21];

    uint8_t* a_sizeBundle = &m_arrayAnswer[6];
    uint8_t* a_resultServer = &m_arrayAnswer[10];
    uint8_t* a_resultCommand = &m_arrayAnswer[14];

    int countRead = 0;
    int sizeRead = FILE_SIZE;
    uint8_t bufferRead[FILE_SIZE] = { 0 };

    int countWrite = 0;
    int sizeWrite = FILE_SIZE;
    uint8_t bufferWrite[FILE_SIZE] = { 0 };

    int sizeSend = 0;
    uint8_t sendArray[SIZE_TRAFFIC] = { 0 };

    int sizeUserPhoto = MAX_SIZE_SEND;

    DIR* dir = nullptr;
    uint8_t numberPerson = 0;
    long dateBegin = 0;
    long dateEnd = 0;

    // TParseData data;
    // TParsePack pack;

    SendFunc m_func;

    void ClearSearch()
    {
        dir = nullptr;
        numberPerson = dateBegin = dateEnd = 0;
    }

    void Clear()
    {
        // resVal = ProtocolResult::Success;

        memset(m_arrayAnswer, 0, MAX_SIZE_SEND);
        answer = m_arrayAnswer;
        a_numberParams = &m_arrayAnswer[21];

        ClearPath();
        ClearBufferRead();
        ClearBufferWrite();

        sizeUserPhoto = MAX_SIZE_SEND;

        // pack.Clear();
    }

    void ClearBufferRead(int size = FILE_SIZE)
    {
        countRead = 0;
        sizeRead = size;
        memset(bufferRead, 0, FILE_SIZE);
    }

    void ClearBufferWrite(int size = FILE_SIZE)
    {
        countWrite = 0;
        sizeWrite = size;
        memset(bufferWrite, 0, FILE_SIZE);
    }

    int GetSizeAnswer()
    {
        return answer - m_arrayAnswer;
    }

    uint8_t* GetAnswer()
    {
        return m_arrayAnswer;
    }

    void InitAnswer(uint8_t command);
};

#pragma pack(push, 1)
struct TMarkDoor {
    uint8_t a = 0;
    uint8_t b = 0;
    uint8_t c = 0;
    uint8_t d = 0;
    uint8_t e = 0;
    uint8_t value[10] = { 0 };
    uint8_t f = 0;
    uint8_t g = 0;
    uint8_t h = 0;
    uint8_t i = 0;
    uint8_t j = 0;
};
#pragma pack(pop)

class CBaseCommand : public CBaseServer
{
protected:
    TPart m_part;
    CSettings m_stg;

    void FuncReceive(const uint8_t* data, const int& size, TCommand* cmd);
    virtual void CommandProcess(const uint8_t& command, const TParseData& parse, TCommand* cmd) = 0;

    void ClearBuffer(uint8_t* buffer, int& count, int& size, const int& sizeBuffer = SIZE_BUFFER);

    long CharToTime(const char* time_details);

    TCommand* CreateLink(int socket, TListLink* list);

    long ToTime(const uint8_t* time);

public:
    CBaseCommand(CListLink* listLink, const int& port);
    ~CBaseCommand();

    static void SetTown(char* path, const uint8_t* town = nullptr);
    static void SetFarm(char* path, const uint8_t* ferm = nullptr);
    static void SetUser(char* path, const uint8_t* user = nullptr);

    static void SetDoubleTown(char* path_1, char* path_2, const uint8_t* town = nullptr);
    static void SetDoubleFarm(char* path_1, char* path_2, const uint8_t* ferm = nullptr);
    static void SetDoubleUser(char* path_1, char* path_2, const uint8_t* user = nullptr);

    static void SetUserPhoto(char* path, const uint8_t* hash);
};

#endif // CBASECOMMAND_H
