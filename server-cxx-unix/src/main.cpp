// #include <pthread.h>
// #include <thread>

#include "core/door.h"
#include "core/moderator.h"
#include "core/user.h"

// CLinkLinux g_linkModerator;
// CLinkLinux g_linkLinux;
// CLinkLinux g_linkDoor;
// ListClients g_socketClient;
// ListClients g_socketDoor;

// CCommand g_command(&g_linkDoor, &g_socketDoor);

// CKnuckle g_knuckle;

/*void ShowListSocket(ListClients* list)
{
        printf("list socket: ");
        for(auto& p : g_socketClient)
                printf("%d ", p);
        printf("\n");
}*/

/*void* funcProcess(void* param)
{
        int socketClient = *reinterpret_cast<int*>(param);
        // TSettings settings;
        // TPart part;

        SendFunc func = [socketClient](const uint8_t* sendArray, const int& sizeSend) {
                g_linkLinux.Send(socketClient, sendArray, sizeSend);
        };

        while(1) {

                int result = 0;

                int sizeSend = 0;
                uint8_t sendArray[SIZE_TRAFFIC] = { 0 };

                int sizeRecv = SIZE_TRAFFIC;
                uint8_t recvArray[SIZE_TRAFFIC] = { 0 };

                settings.Clear();
                result = g_linkLinux.Receive(socketClient, recvArray, sizeRecv);
                if(result == 0) {

                        result = FillingMessage(recvArray, sizeRecv, part);
                        if(result == true) {

                                if(part.array[0] == 0xFA && part.array[1] == 0xFB) {
                                        for(auto& p : g_command.m_mapCommand) {
                                                if(part.array[2] == p.first) {
                                                        settings.InitAnswer(p.first);
                                                        (g_command.*p.second)(&part.array[3], sendArray, sizeRecv,
settings); SliceMessage(settings.GetAnswer(), settings.GetSizeAnswer(), sendArray, sizeSend, func);
                                                        //g_knuckle.SendData(settings.GetAnswer(),
settings.GetSizeAnswer(), p.first, func);
                                                        // func(settings.GetAnswer(), settings.GetSizeAnswer());
                                                        break;
                                                }
                                        }
                                }

                                part.ClearVal();
                                part.ClearBuf();
                        }

                } else
                        break;
        }

        for(auto itr = g_socketClient.begin(); itr != g_socketClient.end(); ++itr) {
                if(*itr == socketClient) {
                        g_socketClient.erase(itr);
                        break;
                }
        }

        ShowListSocket(&g_socketClient);

        return nullptr;
}*/

/*void* funcCheckKeyDoor(void* param)
{
        while(1) {
                std::this_thread::sleep_for(std::chrono::seconds(15));

                try {
                        std::time_t current_time = std::time(0);
                        //g_command.RefreshKey(current_time);
                        // printf("Current local time and date: %ld: %s", current_time,
std::asctime(std::localtime(&current_time))); } catch(int err) { printf("error program");
                }
        }

        return nullptr;
}*/

/*void* linkDoor(void* param)
{
        while(1) {
                int socketClient = 0;

                int result = g_linkDoor.Accept(socketClient);
                if(result == 0) {
                        g_socketDoor.push_back(socketClient);
                }
        }
}*/

/*void* control(void* param)
{
        int result = 0;

        while(1) {
                int command;
                scanf("%d", &command);

                switch(command) {
                case 0: {
                        uint8_t message[] = { 0xF5, 0xA7, 0x43, 0xB6,  0xD1 };
                        int size = sizeof(message) / sizeof(message[0]);

                        // for(auto& p : g_socketDoor)
                        // 	g_linkDoor.Send(p, message, size);

                        g_socketDoor.clear();

                        break;
                }
                case 1: {
                        const uint8_t messageSend[] = { 0xB3, 0xA2, 0x8A, 0x4C,  0x76 };
                        const int sizeSend = sizeof(messageSend) / sizeof(messageSend[0]);

                        int sizeRecv = 5;
                        uint8_t messageRecv[sizeRecv] = { 0 };

                        for(auto& p : g_socketDoor) {
                                //g_linkDoor.Send(p, messageSend, sizeSend);
                                //result = g_linkDoor.Receive(p, messageRecv, sizeRecv);

                                try {

                                        if(result == 0 && sizeRecv == sizeSend) {
                                                for(int i = 0; i < sizeSend; i++)
                                                        if(messageRecv[i] != messageSend[i])
                                                                throw 1;
                                                printf("success: check link door\n");
                                        } else
                                                throw 1;

                                } catch(int error) {
                                        printf("error: check link door\n");
                                        //g_linkDoor.Close(&p);
                                }
                        }

                        break;
                }
                }
        }
}

// ------------------------------------------------------------------------

#pragma pack(push, 1)
struct TMarkModerator {
        uint8_t a = 0;
        uint8_t b = 0;
        uint8_t c = 0;
        uint8_t id[SIZE_GUID] = { 0 };
        uint8_t result = 0;
};
#pragma pack(pop)


void* processModerator(void* param)
{
        int sock = 0;

        //g_linkLinux.Accept(sock);

        int sizeRecv = 0;
        uint8_t recv[MAX_SIZE_SEND] = { 0 };

        // TPart part;

        SendFunc func = [socketClient](const uint8_t* sendArray, const int& sizeSend) {
                g_linkLinux.Send(socketClient, sendArray, sizeSend);
        };

        while(1) {

                if(g_linkLinux.Receive(sock, recv, sizeRecv) == true) {
                        if(FillingMessage(recv, sizeRecv, part) == true) {
                                if(part.pos == sizeof(TMarkModerator)) {
                                        TMarkModerator* mark = (TMarkModerator*)part.array;
                                        if(muser(&listLink);
                ark->a == 0xFA && mark->b == 0xFB && mark->c == 0x4D)
                                                g_command.SetUserResolution(mark->id, mark->result);
                                }

                                part.ClearBuf();
                                part.ClearVal();
                        }
                }
        }
}*/

// ------------------------------------------------------------------------

int main(int argc, char** argv)
{
    char cmd[20] = { 0 };

    CListLink listLink;
    CUser user(&listLink, PORT_USER);
    CDoor door(&listLink, PORT_DOOR);
    CModerator moderator(&listLink, PORT_MODERATOR);

    bool exit = true;

    while(exit == true) {

        fgets(cmd, 20, stdin);

        for(int i = 0; i < 20; i++) {
            if(cmd[i] == '1' && cmd[i + 1] == '1')
                exit = false;

            if(cmd[i] == '2' && cmd[i + 1] == '2') {
                for(auto& p : listLink.m_door)
                    door.CloseConnect(p.second->socket);

                listLink.m_door.clear();
            }
        }
    }

    /*try {

            if(g_linkLinux.Init(PORT) != 0)
                    throw 1;

            if(g_linkDoor.Init(PORT_DOOR) != 0)
                    throw 1; // printf("Link door error");

            pthread_t tid_check_key;
            pthread_attr_t attr_check_key;
            pthread_attr_init(&attr_check_key);
            pthread_create(&tid_check_key, &attr_check_key, funcCheckKeyDoor, nullptr);

            pthread_t tid_link_door;
            pthread_attr_t attr_link_door;
            pthread_attr_init(&attr_link_door);
            pthread_create(&tid_link_door, &attr_link_door, linkDoor, nullptr);

            pthread_t tid_control;
            pthread_attr_t attr_control;
            pthread_attr_init(&attr_control);
            pthread_create(&tid_control, &attr_control, control, nullptr);

            pthread_t tid_moderator;
            pthread_attr_t attr_moderator;
            pthread_attr_init(&attr_moderator);
            pthread_create(&tid_moderator, &attr_moderator, processModerator, nullptr);

            while(1) {
                    int socketClient = 0;

                    int result = g_linkLinux.Accept(socketClient);
                    if(result == 0) {

                            g_socketClient.push_back(socketClient);

                            ShowListSocket(&g_socketClient);

                            pthread_t tid;
                            pthread_attr_t attr;
                            pthread_attr_init(&attr);
                            pthread_create(&tid, &attr, funcProcess, reinterpret_cast<void*>(&g_socketClient.back()));
                    }
            }

    } catch(int error_app) {
            printf("ERROR APPLICATION");

    }*/

    // printf("end");
    // getchar();

    return 0;
}
