#include "tcpserverssl.h"

#include <fstream>

#include <printx.h>
#include <win/tools.h>

CTCPServerSSL::CTCPServerSSL()
{
    m_ssl_ctx = nullptr;
    m_path_certificate = nullptr;
    m_path_private_key = nullptr;

    // SERVER

    m_print_hex = false;
    m_print_message = false;
    m_receive_func = nullptr;
    m_close_func = nullptr;

    m_process_check = true;
    m_thr_check = new std::thread(&CTCPServerSSL::CheckThread, this);

    m_log = new std::ofstream("log.txt");
}

CTCPServerSSL::~CTCPServerSSL()
{
    m_log->close();
    delete m_log;

    if(m_ssl_ctx != nullptr)
        SSL_CTX_free(m_ssl_ctx);

    // SERVER

    DisconnectAll();

    m_process_check = false;
    if(m_thr_check != nullptr) {
        m_thr_check->join();
        delete m_thr_check;
        m_thr_check = nullptr;
    }
}

void CTCPServerSSL::CheckThread()
{
    int result = 0;
    int error_code = 0;
    int length_error_code = sizeof(error_code);

    while(m_process_check == true) {

        std::this_thread::sleep_for(std::chrono::seconds(1));

        std::lock_guard<std::mutex> guard(m_mtx);

        for(auto ritr = m_list_link.rbegin(); ritr != m_list_link.rend();) {

            TItemLink* p_item_link = (*ritr);

            if(p_item_link == nullptr) {
                ritr = decltype(ritr) { m_list_link.erase(std::next(ritr).base()) };
                continue;
            }

            result = getsockopt(p_item_link->socket, SOL_SOCKET, SO_ERROR, (char*)&error_code, &length_error_code);
            if(result != 0) {

                ritr = decltype(ritr) { m_list_link.erase(std::next(ritr).base()) };

                if(p_item_link->thread != nullptr) {

                    if (p_item_link->thread->joinable() == true)
                        p_item_link->thread->join();

                    delete p_item_link->thread;
                    p_item_link->thread = nullptr;
                }

                SSL_free(p_item_link->ssl);

                if(p_item_link->stream != nullptr) {
                    p_item_link->stream->close();
                    delete p_item_link->stream;
                    p_item_link->stream = nullptr;
                }

                if(p_item_link->file != nullptr) {
                    fclose(p_item_link->file);
                    p_item_link->file = nullptr;
                }

                if(p_item_link->delete_file == true) {
                    result = std::remove(p_item_link->name_file.c_str());
                    if(result != 0)
                        *m_log << "remove file...\terror\tname file:" << p_item_link->name_file << std::endl;
                }

                delete p_item_link;
                p_item_link = nullptr;

            } else
                ++ritr;
        }
    }
}

int CTCPServerSSL::CheckIp(const struct in_addr& addr)
{
    int result_a = 0;
    int result_b = 0;

    for(auto& p : m_list_link) {

        result_a = memcmp(&p->remote_addr.sin_addr, &addr, sizeof(struct in_addr));

        std::string ip_a = inet_ntoa(p->remote_addr.sin_addr);
        std::string ip_b = inet_ntoa(addr);

        if(ip_a == ip_b)
            result_b = 0;
        else
            result_b = 1;

        if(result_b == 0) {
            if(result_a != 0) {
                // *m_log << "check ip...\terror\tip:" << ip_a << "==" << "ip:" << ip_b << std::endl;
                *m_log << "check ip...\terror" << std::endl;
            }
        }

        if(result_a == 0 && result_b == 0)
            return 1;
    }

    return 0;
}

int CTCPServerSSL::Send(TItemLink* item_link, const char* data_send, const int& size_send)
{
    int result = 0;

    if(m_list_link.empty() == false) {

        for(auto& p : m_list_link)
            if(p == item_link)
                result |= SendSocket(p, data_send, size_send);

        return result;
    } else
        printf("error: list architecture is empty");

    return 1;
}

int CTCPServerSSL::SendSocket(TItemLink* item_link, const char* data_send, const int& size_send)
{
    int remote_port = 0;
    char* ip = nullptr;

    int size_question = 0;
    // int length_address = sizeof(item_link->remote_addr);

    *item_link->stream << "send...";

    size_question = SSL_write(item_link->ssl, data_send, size_send);
    if(size_question != -1) {

        ip = inet_ntoa(item_link->remote_addr.sin_addr);
        remote_port = htons(item_link->remote_addr.sin_port);

        // *item_link->stream << "\t\t\tsuccess\tid:" << item_link->socket << " ip:" << ip << " port:" << remote_port << " size:" << size_question;
        *item_link->stream << "\t\t\tsuccess\tid:" << item_link->socket << " port:" << remote_port << " size:" << size_question;

        if(m_print_message == true) {

            *item_link->stream << " message: ";
            if(m_print_hex == true)
                printx(data_send, size_question);
            else
                printf("%s\n", data_send);
        } else
            *item_link->stream << std::endl;

        return 0;
    }

    *item_link->stream << "\terror\tid:" << item_link->socket << " result:" << size_question << std::endl;
    ERR_print_errors_fp(item_link->file);
    item_link->delete_file = false;
    return 1;
}

void CTCPServerSSL::RecvSocket(TItemLink* item_link)
{
    int remote_port = 0;
    char* ip = nullptr;

    int size_answer = 0;
    // int length_address = sizeof(item_link->remote_addr);

    const int size_recv = SIZE_RECV_BUFFER;
    char* data_recv = new char[SIZE_RECV_BUFFER] { 0 };

    do {

        size_answer = SSL_read(item_link->ssl, data_recv, size_recv);
        if(size_answer > 0) {

            ip = inet_ntoa(item_link->remote_addr.sin_addr);
            remote_port = htons(item_link->remote_addr.sin_port);

            // *item_link->stream << "recv...\t\t\tsuccess\tid:" << item_link->socket << " ip:" << ip << " port:" << remote_port << " size:" << size_answer;
            *item_link->stream << "recv...\t\t\tsuccess\tid:" << item_link->socket << " port:" << remote_port << " size:" << size_answer;

            if(m_print_message == true) {

                *item_link->stream << " message: ";
                if(m_print_hex == true)
                    printx(data_recv, size_answer);
                else
                    printf("%s\n", data_recv);
            } else
                *item_link->stream << std::endl;

            if(m_receive_func != nullptr)
                m_receive_func(item_link, data_recv, size_answer);

        } else if(size_answer == 0) {
            *item_link->stream << "recv...\t\t\tsuccess\tid:" << item_link->socket << " connection closed" << std::endl;
        } else {
            *item_link->stream << "recv...\t\t\terror\tid:" << item_link->socket << " result:" << size_answer
                               << std::endl;
            ERR_print_errors_fp(item_link->file);
            item_link->delete_file = false;
        }

    } while(size_answer > 0);

    CloseSocket(item_link->socket, item_link->stream);

    delete[] data_recv;

    if(m_close_func != nullptr && m_list_link.empty() == false && m_list_link.at(0)->socket == item_link->socket)
        m_close_func(item_link->socket);
}

int CTCPServerSSL::Disconnect(TItemLink* item_link)
{
    int result = 0;

    std::unique_lock<std::mutex> lock(m_mtx, std::defer_lock);
    lock.lock(); // Blocking mutex

    for(auto& p : m_list_link) {

        if(p == item_link) {
            result |= ShutdownSSL(item_link->ssl, item_link->file);
            result |= ShutdownSocket(item_link->socket, SD_BOTH, item_link->stream);
        }
    }

    lock.unlock(); // Unblocking mutex

    if(result != 0)
        item_link->delete_file = false;

    return result;
}

int CTCPServerSSL::DisconnectAll()
{
    int result = 0;

    for(auto ritr = m_list_link.rbegin(); ritr != m_list_link.rend(); ++ritr) {

        auto pritr = *ritr;

        if(pritr->type == CLIENT)
            result |= ShutdownSocket(pritr->socket, SD_BOTH); // SD_RECEIVE SD_SEND SD_BOTH
        if(pritr->type == SERVER)
            result |= CloseSocket(pritr->socket);

        if(pritr->thread->joinable() == true)
            pritr->thread->join();

        if(ritr == m_list_link.rend() - 1) {

            if(WSACleanup() == SOCKET_ERROR) {
                printf("WSACleanup...\terror\tcode:%lu wsa:%d result:%d\n", GetLastError(), WSAGetLastError(), result);
            }
            printf("WSACleanup...\tsuccess\n");
        }
    }

    return result;
}

int CTCPServerSSL::ShutdownSocket(const SOCKET& socket, const int& how, std::ofstream* stream)
{
    int result = shutdown(socket, how);
    if(result == SOCKET_ERROR) {

        if(stream != nullptr)
            *stream << "shutdown...\t\terror\tid:" << socket << " code:" << GetLastError()
                    << " wsa:" << WSAGetLastError() << " result:" << result << std::endl;
        else
            printf("shutdown...\terror\tid:%llu code:%lu wsa:%d result:%d\n", socket, GetLastError(), WSAGetLastError(),
                result);

        return 1;
    }

    if(stream != nullptr)
        *stream << "shutdown...\t\tsuccess\tid:" << socket << std::endl;
    else
        printf("shutdown...\tsuccess\tid:%llu\n", socket);

    return 0;
}

int CTCPServerSSL::CloseSocket(const SOCKET& socket, std::ofstream* stream)
{
    int result = closesocket(socket);
    if(result == SOCKET_ERROR) {

        if(stream != nullptr)
            *stream << "close...\t\terror\tid:" << socket << " code:" << GetLastError() << " wsa:" << WSAGetLastError()
                    << " result:" << result << std::endl;
        else
            printf("close...\terror\tid:%llu code:%lu wsa:%d result:%d\n", socket, GetLastError(), WSAGetLastError(),
                result);

        return 1;
    }

    if(stream != nullptr)
        *stream << "close...\t\tsuccess\tid:" << socket << std::endl;
    else
        printf("close...\tsuccess\tid:%llu\n", socket);

    return 0;
}

int CTCPServerSSL::Connect()
{
    char* ip = nullptr;
    int locale_port = 0;

    int result = 0;
    WSADATA wsaData;
    SOCKET socket_server = 0;
    SOCKADDR_IN locale_addr;
    SOCKADDR_IN remote_addr;

    ZeroMemory(&locale_addr, sizeof(SOCKADDR_IN));
    locale_addr.sin_family = AF_INET;
    locale_addr.sin_port = htons(443);
    locale_addr.sin_addr.S_un.S_addr = INADDR_ANY;

    ZeroMemory(&remote_addr, sizeof(SOCKADDR_IN));

    printf("WSAStartup...");
    result = WSAStartup(MAKEWORD(2, 2), &wsaData);
    if(result != NO_ERROR) {
        printf("\terror code %lu: wsa %d: result %d\n", GetLastError(), WSAGetLastError(), result);
        WSACleanup();
        return 1;
    } else
        printf("\tsuccess\n");

    printf("socket...");
    socket_server = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    if(socket_server == INVALID_SOCKET) {
        printf("\terror\tcode:%lu wsa:%d\n", GetLastError(), WSAGetLastError());
        WSACleanup();
        return 1;
    } else
        printf("\tsuccess\tid:%llu\n", socket_server);

    printf("bind... ");
    result = bind(socket_server, (SOCKADDR*)&locale_addr, sizeof(locale_addr));
    if(result == SOCKET_ERROR) {
        printf(
            "\terror\tid:%llu code:%lu wsa:%d result:%d\n", socket_server, GetLastError(), WSAGetLastError(), result);
        CloseSocket(socket_server);
        WSACleanup();
        return 1;
    } else {

        ip = inet_ntoa(locale_addr.sin_addr);
        locale_port = htons(locale_addr.sin_port);

        printf("\tsuccess\tid:%llu ip:%s port:%d\n", socket_server, ip, locale_port);
    }

    printf("listen...");
    result = listen(socket_server, SOMAXCONN);
    if(result == SOCKET_ERROR) {
        printf(
            "\terror\tid:%llu code:%lu wsa:%d result:%d\n", socket_server, GetLastError(), WSAGetLastError(), result);
        CloseSocket(socket_server);
        WSACleanup();
        return 1;
    } else
        printf("\tsuccess\tid:%llu\n", socket_server);

    TItemLink* item = new TItemLink;
    item->type = SERVER;
    item->socket = socket_server;
    item->locale_addr = locale_addr;
    item->thread = new std::thread(&CTCPServerSSL::Listen, this, socket_server);
    m_list_link.push_back(item);

    return result;
}

void CTCPServerSSL::Listen(const SOCKET& socket_server)
{
    int result = 0;
    int remote_port = 0;
    char* ip = nullptr;
    std::string client_guid;

    SOCKET socket_client = 0;
    SOCKADDR_IN locale_addr;
    SOCKADDR_IN remote_addr;
    int length_address = sizeof(SOCKADDR_IN);

    ZeroMemory(&locale_addr, sizeof(SOCKADDR_IN));
    ZeroMemory(&remote_addr, sizeof(SOCKADDR_IN));

    while(socket_client != INVALID_SOCKET) {

        try {

            socket_client = accept(socket_server, (SOCKADDR*)&remote_addr, &length_address); // remote_addr change !!!
            if(socket_client != INVALID_SOCKET) {

                // u_long unblock_mode = 1; // Включение неблокирующего режима
                // ioctlsocket(socket_client, FIONBIO, &unblock_mode);

                ip = inet_ntoa(remote_addr.sin_addr);
                remote_port = htons(remote_addr.sin_port);
                client_guid = getGuid();

                // if(CheckIp(remote_addr.sin_addr) == 0) {
                if(true) {

                    std::string name_file = client_guid + ".txt";

                    FILE* file = fopen(name_file.c_str(), "a");
                    std::ofstream* stream = new std::ofstream(name_file);

                    // *stream << "accept...\t\tsuccess id:" << socket_client << " ip:" << ip << " port:" << remote_port << std::endl;
                    *stream << "accept...\t\tsuccess id:" << socket_client << " port:" << remote_port << std::endl;

                    SSL* ssl = SSL_new(m_ssl_ctx);
                    if(ssl != nullptr) {
                        *stream << "SSL_new...\t\tsuccess id:" << socket_client << std::endl;

                        result = SSL_set_fd(ssl, (int)socket_client);
                        if(result != -1) {
                            *stream << "SSL_set_fd...\tsuccess id:" << socket_client << std::endl;

                            int time = 1000;
                            result = setsockopt(socket_client, SOL_SOCKET, SO_RCVTIMEO, (const char*)&time, sizeof(time));

                            result = SSL_accept(ssl);
                            if(result != -1) {
                                *stream << "SSL_accept...\tsuccess id:" << socket_client << std::endl;
                                *stream << "SSL connection using " << SSL_get_cipher(ssl) << std::endl;

                                u_long block_mode = 0; // Включение блокирующего режима
                                ioctlsocket(socket_client, FIONBIO, &block_mode);

                                TItemLink* item = new TItemLink;
                                item->type = CLIENT;
                                item->ssl = ssl;
                                item->socket = socket_client;
                                item->file = file;
                                item->stream = stream;
                                item->remote_addr = remote_addr;
                                item->thread = new std::thread(&CTCPServerSSL::RecvSocket, this, item);
                                item->name_file = name_file;
                                m_list_link.push_back(item);

                            } else {
                                *stream << "SSL_accept...\terror id:" << socket_client << std::endl;
                                ERR_print_errors_fp(file);
                                stream->close();
                                fclose(file);
                            }

                        } else {
                            *stream << "SSL_set_fd...\terror id:" << socket_client << std::endl;
                            ERR_print_errors_fp(file);
                            stream->close();
                            fclose(file);
                        }

                    } else {
                        *stream << "SSL_new...\terror id:" << socket_client << std::endl;
                        ERR_print_errors_fp(file);
                        stream->close();
                        fclose(file);
                    }
                } else {
                    *m_log << "ip exist... \t\terror\tid:" << socket_client << " ip:" << ip << " port:" << remote_port << " guid:" << client_guid << std::endl;
                    ShutdownSocket(socket_client, SD_BOTH, m_log);
                    CloseSocket(socket_client, m_log);
                }

            } else {
                printf("accept...\terror code %lu: wsa %d: id %llu\n", GetLastError(), WSAGetLastError(), socket_client);
            }

        } catch(...) {
            printf("accept...\texception code %lu: wsa %d: id %llu\n", GetLastError(), WSAGetLastError(), socket_client);
            while(true);
        }
    }

    if(m_close_func != nullptr && m_list_link.empty() == false && m_list_link.at(0)->socket == socket_server)
        m_close_func(socket_client);
}

int CTCPServerSSL::ShutdownSSL(SSL* ssl, FILE* file)
{
    int result = 0;

    result = SSL_shutdown(ssl);
    if(result == 0)
        result = SSL_shutdown(ssl); // Need to make second call SSL_shutdown()

    if(result < 0)
        ERR_print_errors_fp(file);

    return 0;
}

int CTCPServerSSL::ConnectSSL()
{
    int result = 0;

    SSL_load_error_strings();
    SSLeay_add_ssl_algorithms();

    const SSL_METHOD* ssl_method = SSLv23_server_method();
    if(ssl_method == nullptr) {
        ERR_print_errors_fp(stderr);
        return 1;
    }

    m_ssl_ctx = SSL_CTX_new(ssl_method);
    if(m_ssl_ctx == nullptr) {
        ERR_print_errors_fp(stderr);
        return 1;
    }

    result = SSL_CTX_use_certificate_file(m_ssl_ctx, m_path_certificate, SSL_FILETYPE_PEM);
    if(result <= 0) {
        ERR_print_errors_fp(stderr);
        return 1;
    }

    result = SSL_CTX_use_PrivateKey_file(m_ssl_ctx, m_path_private_key, SSL_FILETYPE_PEM);
    if(result <= 0) {
        ERR_print_errors_fp(stderr);
        return 1;
    }

    result = SSL_CTX_check_private_key(m_ssl_ctx);
    if(result == 0) {
        ERR_print_errors_fp(stderr);
        fprintf(stderr, "Private key does not match the certificate public key\n");
        return 1;
    }

    return 0;
}