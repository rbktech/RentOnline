#pragma once

#include <openssl/crypto.h>
#include <openssl/err.h>
#include <openssl/ssl.h>
#include <openssl/x509.h>

#include <cstdint>
#include <cstdio>
#include <functional>
#include <string>
#include <thread>
#include <utility>
#include <vector>
#include <winsock2.h>
#include <mutex>

#define UNUSED(identifier)

#define SIZE_SEND_BUFFER 5242880
#define SIZE_RECV_BUFFER 1024
#define SIZE_HTTP_BUFFER 1024

enum : int { CLIENT, SERVER };

struct TItemLink {
    int type = 0;
    SSL* ssl = nullptr;
    SOCKET socket;
    FILE* file = nullptr;
    std::ofstream* stream = nullptr;
    SOCKADDR_IN locale_addr;
    SOCKADDR_IN remote_addr;
    std::thread* thread = nullptr;
    std::string name_file;
    bool delete_file = true;
};

class CTCPServerSSL
{
private:

    std::mutex m_mtx;

    std::ofstream* m_log;

    SSL_CTX* m_ssl_ctx;

    const char* m_path_certificate;
    const char* m_path_private_key;

    // SERVER

    bool m_print_hex;
    bool m_print_message;
    std::function<void(TItemLink*, const char*, const int&)> m_receive_func;

    // -------------------------------------------------------------------

    bool m_process_check;
    std::thread* m_thr_check;

    void CheckThread();
    int CheckIp(const struct in_addr& addr);

    std::function<int(const SOCKET&)> m_accept_func;

    void Listen(const SOCKET& socket_server);

protected:
    // SERVER

    std::function<void(const SOCKET&)> m_close_func;

    // -------------------------------------------------------------------

    // int m_locale_port;
    // int m_remote_port;
    // const char* m_address;

    std::vector<TItemLink*> m_list_link;

    // -------------------------------------------------------------------

    void RecvSocket(TItemLink* item_link);

    int SendSocket(TItemLink* item_link, const char* data_send, const int& size_send);

    int CloseSocket(const SOCKET& socket, std::ofstream* stream = nullptr);

    int ShutdownSocket(const SOCKET& socket, const int& how, std::ofstream* stream = nullptr);

public:
    CTCPServerSSL();
    ~CTCPServerSSL();

    void SetCertificate(const char* path_certificate)
    {
        m_path_certificate = path_certificate;
    }

    void SetPrivateKey(const char* path_private_key)
    {
        m_path_private_key = path_private_key;
    }

    int ConnectSSL();
    int ShutdownSSL(SSL* ssl, FILE* file);

    // SERVER

    int Send(TItemLink* item_link, const char* data_send, const int& size_send);

    int Disconnect(TItemLink* item_link);

    int DisconnectAll();

    int Connect();

    // -------------------------------------------------------------------

    void SetPrintHex(bool enable)
    {
        m_print_hex = enable;
    }

    void SetPrintMessage(bool enable)
    {
        m_print_message = enable;
    }

    void SetReceiveFunction(std::function<void(TItemLink*, const char*, const int&)> func)
    {
        m_receive_func = std::move(func);
    }

    void SetCloseFunction(std::function<void(const SOCKET&)> func)
    {
        m_close_func = std::move(func);
    }

    /*void SetRemotePort(const int& remote_port)
    {
        m_remote_port = remote_port;
    }

    void SetLocalPort(const int& locale_port)
    {
        m_locale_port = locale_port;
    }

    void SetAddress(const char* address)
    {
        m_address = address;
    }*/

    std::vector<TItemLink*>& GetListLink()
    {
        return m_list_link;
    }
};