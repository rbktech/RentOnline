#include "linkLinux.hpp"

#include <cstring>
#include <sys/types.h>
#include <unistd.h>

#ifdef UNIX

#include <arpa/inet.h>
#include <netinet/in.h>
#include <sys/socket.h>

#endif

#ifdef WIN32

#include <winsock2.h>
#include <ws2tcpip.h>

#endif

// #include <netdb.h>

#include <iostream>

#define SIZE_MARK_DOOR 5
#define PORT_DOOR 1222

CLinkLinux::CLinkLinux()
{
    m_socketListener = 0;
}

CLinkLinux::~CLinkLinux()
{
}

bool CLinkLinux::InitClientAndSendGuid(const char* ip, const uint8_t* guid, const int& sizeGuid)
{
    const int sizeSend = SIZE_MARK_DOOR + sizeGuid;
    uint8_t message[sizeSend] = { 0xFD, 0xFA, 0xFC, 0xF3, 0xE8 };
    memcpy(&message[SIZE_MARK_DOOR], guid, sizeGuid);

    struct sockaddr_in addr;
    addr.sin_family = AF_INET;
    addr.sin_port = htons(PORT_DOOR);
    addr.sin_addr.s_addr = inet_addr(ip);

    int socketClient = socket(AF_INET, SOCK_STREAM, 0);
    if(socketClient < 0)
        return 1;
    perror("socket");

    int result = connect(socketClient, (struct sockaddr*)&addr, sizeof(addr));
    if(result < 0)
        return 1;
    perror("connect");

    if(send(socketClient, (char*)message, sizeSend, 0) == false)
        return 1;
    perror("send");

    if(close(socketClient) == true)
        return 1;
    perror("close");

    return 0;
}

bool CLinkLinux::Init(int port)
{
    int result = 0;
    struct sockaddr_in serv_addr;

    memset(&serv_addr, '0', sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    serv_addr.sin_port = htons(port);

    m_socketListener = socket(AF_INET, SOCK_STREAM, 0);
    if(m_socketListener == 0) {
        std::cout << "Error socket" << std::endl;
        return 1;
    } else
        std::cout << "Success socket" << std::endl;

    result = bind(m_socketListener, (struct sockaddr*)&serv_addr, sizeof(serv_addr));
    if(result != 0) {
        perror("bind");
        return 1;
    } else
        std::cout << "Success bind" << std::endl;

    result = listen(m_socketListener, 3);
    if(result != 0) {
        std::cout << "Error bind " << result << std::endl;
        return 1;
    } else
        std::cout << "Success listen" << std::endl;

    return 0;
}

bool CLinkLinux::Accept(int& socketClient)
{
    struct sockaddr_in cli_addr;
    socklen_t cli_addr_size = sizeof(cli_addr);
    socketClient = accept(m_socketListener, (struct sockaddr*)&cli_addr, &cli_addr_size);
    if(socketClient == 0) {
        std::cout << "Error accept: " << socketClient << std::endl;
        return 1;
    } else {
        std::cout << "Success accept: " << socketClient << std::endl;
        return 0;
    }
}

bool CLinkLinux::Send(const int& socketClient, const uint8_t* message, const int& size)
{
    std::cout << "Send socket client: " << socketClient << std::endl;

    int result = send(socketClient, (char*)message, size, 0);
    if(result == -1) {
        perror("send");
        std::cout << "Error send " << result << std::endl;
        return 1;
    } else {
        std::cout << "Bytes send: " << result << std::endl;
        std::cout << "Message: " << message << std::endl;
        return 0;
    }
}

bool CLinkLinux::Receive(const int& socketClient, uint8_t* message, int& size)
{
    std::cout << "Received socket client: " << socketClient << std::endl;

    int result = recv(socketClient, (char*)message, size, 0);
    if(result > 0) {
        std::cout << "Bytes received: " << (size = result) << std::endl;
        std::cout << "Message: " << message << std::endl;
        return 0;
    } else {
        if(result == 0) {
            std::cout << "Connection closed: " << socketClient << std::endl;
        } else {
            perror("recv");
            std::cout << "Error recv: " << result << std::endl;
        }
        return 1;
    }
}

bool CLinkLinux::Close(int* socket)
{
    int result = 0;

    if(socket == nullptr)
        socket = &m_socketListener;

    result = close(*socket);
    if(result != 0) {
        // printf("error %d: close socket %d\n", result, *socket);
        return 1;
    } else
        // printf("success: close socket %d\n", *socket);

        return 0;
}
