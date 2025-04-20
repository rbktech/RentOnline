#include <iostream>

#include "core.h"
#include "tcpserverssl.h"

#include <win/tools.h>

CCore g_core;
CTCPServerSSL g_server_ssl;
bool g_process = true;

int g_size_http = SIZE_HTTP_BUFFER;
char* g_data_http = new char[SIZE_HTTP_BUFFER] { 0 };

int g_size_content = SIZE_SEND_BUFFER;
char* g_data_content = new char[SIZE_SEND_BUFFER] { 0 };

std::mutex g_mtx;

BOOL WINAPI consoleHandler(DWORD signal) {
    if(signal == CTRL_CLOSE_EVENT) {

        delete[] g_data_http;
        delete[] g_data_content;

        // printf("Консоль закрыта! Нажмите Enter, чтобы продолжить...");
        fflush(stdout);
        // getchar();
    }
    return TRUE;
}

void close_socket(const SOCKET& socket)
{
    // g_process = false;
}

void recv_socket(TItemLink* item_link, const char* data_recv, const int& size_recv)
{
    std::lock_guard<std::mutex> guard(g_mtx);

    int result = 0;
    g_size_http = SIZE_HTTP_BUFFER;
    g_size_content = SIZE_SEND_BUFFER;

    result = g_core.dataProcessing(data_recv, size_recv, g_data_http, g_size_http, g_data_content, g_size_content);
    if(result == 0) {

        g_server_ssl.Send(item_link, g_data_http, g_size_http);
        g_server_ssl.Send(item_link, g_data_content, g_size_content);
    }
}

int main(int argc, char** argv)
{
    int result = 0;
    unsigned size_list = 0;

    SetConsoleCtrlHandler(consoleHandler, TRUE);

    g_server_ssl.SetCertificate("cert/localhost.crt");
    g_server_ssl.SetPrivateKey("cert/localhost.decrypted.key");

    g_server_ssl.SetReceiveFunction(recv_socket);
    g_server_ssl.SetCloseFunction(close_socket);

    result = g_core.connectDatabase("database/rent.db");
    if(result == 0) {

        result = g_server_ssl.ConnectSSL();
        if(result == 0) {
            std::cout << "ssl... \tsuccess\tinit" << std::endl;

            result = g_server_ssl.Connect();
            if(result == 0) {
                std::cout << "server... \tsuccess\tinit" << std::endl;

                while(g_process == true) {

                    std::this_thread::sleep_for(std::chrono::seconds(1));

                    g_core.enableBooking();

                    auto& list_link = g_server_ssl.GetListLink();
                    if(list_link.size() != size_list) {
                        size_list = list_link.size();

                        system("cls");

                        for(auto& item_link : list_link) {

                            std::cout << "id:" << item_link->socket << std::endl;
                        }
                    }
                }
            } else
                std::cout << "server... \terror\tinit" << std::endl;

        } else
            std::cout << "ssl... \terror\tinit" << std::endl;
    }

    getchar();
    return 0;
}