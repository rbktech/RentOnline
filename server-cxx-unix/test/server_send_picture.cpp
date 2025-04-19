#include "gtest/gtest.h"

#include <assembly.h>
#include <unix/file.h>

class GTServerSendPicture : public ::testing::Test
{
protected:
    void SetUp() override {};
    void TearDown() override {};
};

TEST_F(GTServerSendPicture, SendPicture)
{
    CTCPServer g_server;

    int sizeSend = 0;
    uint8_t sendArray[SIZE_TRAFFIC] = { 0 };

    int count = 0;
    int size = MAX_SIZE_SEND;
    uint8_t send[MAX_SIZE_SEND] = { 0 };

    send[count++] = 0xFA;
    send[count++] = 0xFB;
    send[count++] = 'M';

    memcpy(&send[count++], "Mail", 4);

    send[count += 3] = 0xFA;
    send[++count] = 0xFB;
    send[++count] = 0x60;

    memcpy(&send[++count], "Password", 8);

    send[count += 8] = 0xFA;
    send[++count] = 0xFB;
    send[++count] = 0x60;

    memcpy(&send[++count], "Name", 4);

    send[count += 4] = 0xFA;
    send[++count] = 0xFB;
    send[++count] = 0x60;

    memcpy(&send[++count], "SurName", 7);

    send[count += 7] = 0xFA;
    send[++count] = 0xFB;
    send[++count] = 0x60;

    FileResult result = CFile::OpenReadFile("./picture/e88f3028afe762960b7a2c11837b34d1.jpg", &send[++count], size);
    if(result != FileResult::Success) {
        printf("File not open");
        ASSERT_EQ(result, FileResult::Success);
    }

    send[count += size] = 0xFA;
    send[++count] = 0xFB;
    send[++count] = 0x60;

    send[++count] = 0xFA;
    send[++count] = 0xFB;
    send[++count] = 0xFF;

    if(g_server.Connect("127.0.0.1", 6000) == 0) {
        SOCKET sock = g_server.Accept();

        SendFunc func = [&g_server](int sock, const uint8_t* sendArray, const int& sizeSend) {

            printf("size send: %d\n", sizeSend);
            g_server.Send(sock, sendArray, sizeSend);
        };

        printf("Send...");
        SliceMessage(send, count, sendArray, sizeSend, sock, func);
        printf("\tsize message: %d\n", count);

        g_server.Disconnect(sock);
        g_server.Disconnect(g_server.GetSocket());
    }
}