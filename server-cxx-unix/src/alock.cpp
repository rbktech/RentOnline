#include <cstring>
#include <stdint.h>
#include <string>
#include <time.h>

const char* PRF_FARM = "FF";
const char* PRF_USER = "UU";

#define SIZE_TIME 10
#define SIZE_PREFIX 2

#define SIZE_RAND 10

int SIZE_GUID = SIZE_TIME; // + SIZE_PREFIX;
int SIZE_DATA = SIZE_TIME;
int SIZE_HASH = SIZE_RAND;

// ------------------------------------------------------------------------

int PORT_USER = 4000;
int PORT_DOOR = 5000;
int PORT_MODERATOR = 6000;

int SIZE_BUFFER = 512; // For read/write file

// ------------------------------------------------------------------------

int SERVER_SUCCESS = 11;
int SERVER_ERROR = 12;

// ------------------------------------------------------------------------

int COMMAND_HEADER_SIZE = 3;

int COMMAND_SUCCESS = 21;
int COMMAND_ERROR = 22;

int COMMAND_NOT_FOUND = 23;

int COMMAND_READ_FARM_BEGIN = 24;
int COMMAND_READ_FARM_NEXT = 25;
int COMMAND_READ_FARM_END = 25;

// ------------------------------------------------------------------------

int ERROR_MIN_ITEM = 51;

// ------------------------------------------------------------------------

#pragma pack(push, 1)
struct TGuid {

    uint8_t data[SIZE_TIME] = { 0 };
    uint8_t prefix[SIZE_PREFIX] = { 0 };
};

#pragma pack(pop)

std::string GetGuid(uint8_t* array, const char* prefix)
{
    uint8_t arrayGuid[SIZE_GUID] = { 0 };
    std::string str;

    TGuid* gd = (TGuid*)arrayGuid;
    // memcpy(gd->prefix, prefix, SIZE_PREFIX);
    memcpy(gd->data, std::to_string(time(NULL)).data(), SIZE_TIME);

    for(int i = 0; i < SIZE_GUID; i++)
        str.push_back(arrayGuid[i]);

    return str;
}

void GetHash(uint8_t* array, const char* prefix)
{
    for(int i = 0; i < SIZE_HASH; i++)
        array[i] = rand() % 9 + 0 + 'A';
}
