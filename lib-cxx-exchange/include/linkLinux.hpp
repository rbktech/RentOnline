#ifndef CLINKLINUX_HPP
#define CLINKLINUX_HPP

//#include <map>
#include <stdint.h>

// typedef std::map<std::string, const char*> TPackege;

class CLinkLinux
{
private:
    int m_socketListener;

public:
    CLinkLinux();
    ~CLinkLinux();

    bool Init(int port);
    bool Accept(int& socketClient);
    bool Send(const int& socketClient, const uint8_t* message, const int& size);
    bool Receive(const int& socketClient, uint8_t* message, int& size);
    bool Close(int* socket = nullptr);

    static bool InitClientAndSendGuid(const char* ip, const uint8_t* guid, const int& sizeGuid);

    // void AddPackage(TPackege packege, const char* message);
};

#endif // CLINKLINUX_HPP
