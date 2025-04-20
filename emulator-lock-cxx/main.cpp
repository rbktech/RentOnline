#include <thread>

#include <assembly.h>
#include <unix/socket/tcpclient.h>

CTCPClientU g_client;

void FuncSend(int sock, const uint8_t* data, const int& size)
{
    g_client.Send(g_client.GetSocket(), data, size);
}

void FuncRecv(const uint8_t* data, const int& size)
{
    uint8_t arraySend[SIZE_TRAFFIC] = { 0 };
    int sizeSend = SIZE_TRAFFIC;

    SliceMessage(data, size, arraySend, sizeSend, 0, &FuncSend);
}

int main(int argc, char** argv)
{
    while(true) {

        if(g_client.Connect("127.0.0.1", 5000) == 0)

            g_client.Recv(g_client.GetSocket(), &FuncRecv);

        std::this_thread::sleep_for(std::chrono::seconds(5));
    }

    return 0;
}