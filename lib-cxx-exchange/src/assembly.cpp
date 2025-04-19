#include "assembly.h"

#define MARK_SIZE 5

#define MARK_SBEGIN "_FAB_"
#define MARK_BEGIN 0x95, 0x70, 0x65, 0x66, 0x95
#define MARK_CBEGIN (uint8_t)'_', (uint8_t)'F', (uint8_t)'A', (uint8_t)'B', (uint8_t)'_'

#define MARK_SSIZE_PACK "_HGD_"
#define MARK_SIZE_PACK 0x95, 0x72, 0x71, 0x68, 0x95
#define MARK_CSIZE_PACK (uint8_t)'_', (uint8_t)'H', (uint8_t)'G', (uint8_t)'D', (uint8_t)'_'

#define MARK_SEND "_AFE_"
#define MARK_END 0x95, 0x65, 0x70, 0x69, 0x95
#define MARK_CEND (uint8_t)'_', (uint8_t)'A', (uint8_t)'F', (uint8_t)'E', (uint8_t)'_'

void ConvertToSize(const uint8_t* in, const int& sizeIn, int& sizeOut)
{
    if(sizeIn == 4) {

        if(in[0] == '0') {

            sizeOut += (in[1] - '0') * 100;
            sizeOut += (in[2] - '0') * 10;
            sizeOut += (in[3] - '0');
        } else {

            sizeOut |= in[1] << 16;
            sizeOut |= in[0] << 24;
            sizeOut |= in[2] << 8;
            sizeOut |= in[3];
        }
    }
}

void ConvertOfSize(uint8_t* out, const int& sizeOut, const int& sizeIn)
{
    if(sizeOut == 4) {
        out[0] = sizeIn >> 24; // 4 byte
        out[1] = sizeIn >> 16;
        out[2] = sizeIn >> 8;
        out[3] = sizeIn;
    }
}

#pragma pack(push, 1)
struct TMark {
    uint8_t a = 0;
    uint8_t b = 0;
    uint8_t c = 0;
    uint8_t d = 0;
    uint8_t e = 0;
    uint8_t size[4] = { 0 };
    uint8_t value = 0;
};
#pragma pack(pop)

int ParsePackage(const uint8_t* in, const int& inSize, const uint8_t*& out, int& outSize)
{
    TMark* mrk = nullptr;

    for(int i = 0; i + MARK_SIZE <= inSize; i++) {

        mrk = (TMark*)&in[i];

        // if(mrk->a == '_' && mrk->b == 'F' && mrk->c == 'A' && mrk->d == 'B' && mrk->e == '_') {
        if(i < inSize && mrk->a == '_' && mrk->b == 'F' && mrk->c == 'A' && mrk->d == 'B' && mrk->e == '_') {

            if(inSize > &mrk->value - &in[i]) {

                ConvertToSize(mrk->size, 4, outSize);
                out = &mrk->value;

                // if(inSize >= outSize + MARK_SIZE) {
                if(inSize >= outSize + MARK_SIZE && i + 9 + outSize < inSize) {

                    mrk = (TMark*)&in[i + 9 + outSize];
                    if(mrk->a == '_' && mrk->b == 'A' && mrk->c == 'F' && mrk->d == 'E' && mrk->e == '_')
                        return i + 9 + outSize + MARK_SIZE;
                } else
                    return -1;

            } else
                return -1;
        }
    }

    return -1;
}

void MakePackage(const uint8_t* in, const int& inSize, uint8_t*& out, int& outSize)
{
    TMark* mrk = nullptr;

    mrk = (TMark*)out;
    mrk->a = '_';
    mrk->b = 'F';
    mrk->c = 'A';
    mrk->d = 'B';
    mrk->e = '_';
    ConvertOfSize(mrk->size, 4, inSize);
    memcpy(&mrk->value, in, inSize);

    mrk = (TMark*)&out[9 + inSize];
    mrk->a = '_';
    mrk->b = 'A';
    mrk->c = 'F';
    mrk->d = 'E';
    mrk->e = '_';

    outSize = 9 + inSize + 5;
}

/**
 * @brief
 * @param in
 * @param inSize = 1 mb
 * @param out
 * @param outSize = 1100 kb
 * @param func
 */
void SliceMessage(const uint8_t* in, const int& inSize, uint8_t* out, int& outSize, int sock, SendFunc func)
{
    int pos = 0;
    int size = 0;
    uint8_t numberPart[4] = { 0 };

    int number = inSize / MIN_SIZE_SEND;
    if(inSize % MIN_SIZE_SEND != 0)
        number++;

    ConvertOfSize(numberPart, 4, number);
    MakePackage(numberPart, 4, out, outSize);
    if(outSize > MIN_SIZE_SEND || func == nullptr)
        return;
    else
        func(sock, out, outSize);

    memset(out, 0, outSize);
    outSize = 0;

    do {

        size = inSize - pos < MIN_SIZE_SEND ? inSize - pos + 1 : MIN_SIZE_SEND;

        MakePackage(&in[pos], size, out, outSize);

        if(outSize > SIZE_TRAFFIC || func == nullptr)
            return;
        else
            func(sock, out, outSize);

        memset(out, 0, outSize);
        outSize = 0;

    } while(inSize >= (pos += size));
}

/**
 * @brief
 * @param in
 * @param inSize = 1100 kb
 * @param out
 * @param outSize = 1 mb
 * @param func
 */
bool FillingMessage(const uint8_t* in, const int& inSize, TPart& part)
{
    int pos_now = 0;
    int pos_prev = 0;

    int size = 0;
    const uint8_t* data = nullptr;

    int sizeOut = 0;
    const uint8_t* out = nullptr;

    part.m_buffer.Write(in, inSize);

    while(1) {

        data = part.m_buffer.Read();
        size = part.m_buffer.Size();

        if(data == nullptr || size <= 0)
            return false;

        pos_now = ParsePackage(data, size, out, sizeOut = 0);

        if(pos_now != -1 && out != nullptr && sizeOut > 0) {

            part.m_buffer.Flush(pos_now);

            pos_prev += pos_now;

            if(sizeOut == 4)
                part.ClearVal();

            if(part.count != 0) {

                if(part.pos + sizeOut > MAX_SIZE_SEND)
                    return false;

                memcpy(&part.array[part.pos], out, sizeOut);
                part.pos += sizeOut;
            } else
                ConvertToSize(out, 4, part.number);

            if(data != nullptr) {
                delete[] data;
                data = nullptr;
            }

            if(part.count++ == part.number) {
                part.count = part.number = 0;
                return true;
            }

        } else
            break;
    }

    if(data != nullptr) {
        delete[] data;
        data = nullptr;
    }

    return false;
}

// ------------------------------------------------------------------------

void ParseCommand(const uint8_t* in, int size, TParseData& parse)
{
    int count = 0;
    uint8_t buffer[MAX_SIZE_SEND] = { 0 };

    for(int position = 0; position < size; position++) {

        if(in[position] == 0xFA && in[position + 1] == 0xFB) {

            switch(in[position + 2]) {
                case 0x60: {
                    position += 2;
                    if(count != 0) {
                        uint8_t* tmp = new uint8_t[count] { 0 };
                        memcpy(tmp, buffer, count);

                        parse.push_back({ tmp, count });
                        count = 0;
                        memset(buffer, 0, MAX_SIZE_SEND);
                    }
                    break;
                }

                case 0xFF: {
                    return;
                }
            }

        } else {
            buffer[count++] = in[position];
        }
    }
}

CAssembly::CAssembly()
{
}

CAssembly::~CAssembly()
{
}
