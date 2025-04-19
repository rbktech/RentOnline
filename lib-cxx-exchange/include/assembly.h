#ifndef CASSEMBLY_H
#define CASSEMBLY_H

#include "baseserver.h"
#include "ringbuffer.h"

#define MAX_SIZE_SEND 1024

typedef std::function<void(int sock, const uint8_t* message, const int& size)> SendFunc;

struct TArray {
    const uint8_t* first = nullptr;
    const int second = 0;
};

typedef std::vector<TArray> TParseData;

//#pragma pack(push, 1)
struct TPart {
    int pos = 0;
    int count = 0;
    int number = 0;

    uint8_t array[MAX_SIZE_SEND] = { 0 };
    CRingBuffer m_buffer;

    void ClearVal()
    {
        pos = count = number = 0;
        memset(array, 0, MAX_SIZE_SEND);
        // memset(this, 0, sizeof(TPart));
    }
};
//#pragma pack(pop)

void SliceMessage(const uint8_t* in, const int& inSize, uint8_t* out, int& outSize, int sock, SendFunc func = nullptr);
bool FillingMessage(const uint8_t* in, const int& inSize, TPart& part);

// ------------------------------------------------------------------------

void ParseCommand(const uint8_t* in, int size, TParseData& parse);

// ------------------------------------------------------------------------

class CAssembly
{
public:
    CAssembly();
    ~CAssembly();
};

#endif // CASSEMBLY_H
