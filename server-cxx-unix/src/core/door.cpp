#include "door.h"

CDoor::CDoor(CListLink* listLink, const int& port)
    : CBaseCommand(listLink, port)
{
}

CDoor::~CDoor()
{
    for(auto& p : m_listLink->m_door) {
        CTCPBase::Disconnect(p.second->socket);
        p.second->thread->join();
        delete p.second->thread;
    }
}

void CDoor::AddLink(int socket)
{
    CreateLink(socket, &m_listLink->m_door);
}

void CDoor::CommandProcess(const uint8_t& command, const TParseData& parse, TCommand* cmd)
{
    try {

        auto func = m_command.at(command);
        if(func != nullptr)
            (this->*func)(parse, cmd);

    } catch(const std::out_of_range& oor) {
    }
}

void CDoor::FuncReceive(const uint8_t* data, const int& size, TCommand* cmd)
{
    if(size == 20) {

        TMarkDoor* mrk = (TMarkDoor*)data;

        if(mrk->a == '_' && mrk->b == 'S' && mrk->c == 'D' && mrk->d == 'L' && mrk->e == '_')
            if(mrk->f == '_' && mrk->g == 'K' && mrk->h == 'M' && mrk->i == 'V' && mrk->j == '_')
                m_listLink->m_door.insert({ std::string((char*)mrk->value, SIZE_GUID), cmd });
    }
}

/*void CDoor::SetTagToDoor(const int& socket, const uint8_t* tag)
{
}*/

// ------------------------------------------------------------------------

void CDoor::CloseConnect(int socket)
{
    uint8_t message[] = { 0xF5, 0xA7, 0x43, 0xB6, 0xD1 };
    int size = sizeof(message) / sizeof(message[0]);

    m_server.Send(socket, message, size);

    CTCPBase::Disconnect(socket);
}

void CDoor::RefreshKey(const std::time_t current_time)
{
    std::vector<std::string> param_key;
    std::time_t begin_time = 0;
    std::time_t end_time = 0;

    int sizeRead = SIZE_BUFFER;
    int countRead = 0;
    uint8_t bufferRead[SIZE_BUFFER] = { 0 };

    int sizeWrite = SIZE_BUFFER;
    int countWrite = 0;
    uint8_t bufferWrite[SIZE_BUFFER] = { 0 };
    uint8_t* p_number_key = nullptr;

    int sizeReadFerm = SIZE_BUFFER;
    int countReadFerm = 0;
    uint8_t bufferReadFerm[SIZE_BUFFER] = { 0 };

    int sizeWriteFerm = SIZE_BUFFER;
    int countWriteFerm = 0;
    uint8_t bufferWriteFerm[SIZE_BUFFER] = { 0 };

    FileResult resVal = FileResult::Success;

    // char path_users[] = { '.', '/', 'u', 's', 'e', 'r', 's', '/', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
    // '\0', '\0', '/', 'f', 'i', 'l', 'e', '.', 't', 'x', 't', '\0' }; char path_temp_users[] = { '.', '/', 'u', 's',
    // 'e', 'r', 's', '/', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '/', '_', 'f', 'i', 'l', 'e',
    // '.', 't', 'x', 't', '\0' };

    // char path_ferm[] = { '.', '/', 'f', 'e', 'r', 'm', 's', '/', '\0', '\0', '\0', '/', '\0', '\0', '\0', '\0', '\0',
    // '\0', '\0', '\0', '\0', '\0', '/', 'f', 'i', 'l', 'e', '.', 't', 'x', 't', '\0' }; char path_temp_ferm[] = { '.',
    // '/', 'f', 'e', 'r', 'm', 's', '/', '\0', '\0', '\0', '/', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
    // '\0', '/', '_', 'f', 'i', 'l', 'e', '.', 't', 'x', 't', '\0' };

    const char* name = nullptr;
    bool change_file = false;

    auto dir = CDir::OpenDir(m_stg.pthUser);
    if(dir != nullptr) {

        while((name = CDir::GetNameDir(dir)) != nullptr) {

            ClearBuffer(bufferRead, countRead, sizeRead);
            SetDoubleUser(m_stg.pthUser, m_stg.pthTUser, (uint8_t*)name);

            resVal = CFile::OpenReadFile(m_stg.pthUser, bufferRead, sizeRead);
            if(resVal == FileResult::Success) {

                change_file = false;
                int number_key = 0;
                std::string t_string;

                do {

                    if(number_key != 0) {

                        if(bufferRead[countRead] == '|' && bufferRead[countRead + 1] == '$' ||
                            bufferRead[countRead] == '|' && bufferRead[countRead + 1] == '#') {

                            if(t_string.empty() == false) {
                                t_string.erase(0, 1);
                                t_string.push_back('|');
                                std::string p_string;
                                for(auto& p : t_string) {
                                    if(p == '|') {
                                        param_key.push_back(p_string);
                                        p_string.clear();
                                    } else
                                        p_string.push_back(p);
                                }

                                if(param_key.size() == 5) {

                                    try {

                                        int enable_ferm = std::stoi(param_key[0]);
                                        begin_time = std::stoi(param_key[2]);
                                        end_time = std::stoi(param_key[3]);

                                        switch(enable_ferm) {
                                            case NOT_ACTIVATED_FERM:

                                                *p_number_key = number_key + '0';
                                                memcpy(&bufferWrite[countWrite], "|#|", 3);
                                                countWrite += 3;

                                                if(begin_time < current_time) {
                                                    // add key
                                                    change_file = true;
                                                    bufferWrite[countWrite++] = '1';
                                                } else
                                                    bufferWrite[countWrite++] = '0';

                                                bufferWrite[countWrite++] = '|';
                                                memcpy(&bufferWrite[countWrite], param_key[1].data(), SIZE_GUID);
                                                countWrite += SIZE_GUID;
                                                bufferWrite[countWrite++] = '|';
                                                memcpy(&bufferWrite[countWrite], param_key[2].data(), SIZE_DATA);
                                                countWrite += SIZE_DATA;
                                                bufferWrite[countWrite++] = '|';
                                                memcpy(&bufferWrite[countWrite], param_key[3].data(), SIZE_DATA);
                                                countWrite += SIZE_DATA;
                                                bufferWrite[countWrite++] = '|';
                                                memcpy(&bufferWrite[countWrite], param_key[4].data(), SIZE_GUID);
                                                countWrite += SIZE_GUID;

                                                break;
                                            case ACTIVATED_FERM:
                                                if(end_time < current_time) {
                                                    // delete key

                                                    bool readStart = false;
                                                    ClearBuffer(bufferReadFerm, countReadFerm, sizeReadFerm);
                                                    ClearBuffer(bufferWriteFerm, countWriteFerm, sizeWriteFerm);
                                                    SetDoubleTown(m_stg.pthFarm, m_stg.pthTFarm, (uint8_t*)"111");
                                                    SetDoubleFarm(m_stg.pthFarm, m_stg.pthTFarm,
                                                        (uint8_t*)param_key.at(1).data());

                                                    resVal = CFile::OpenReadFile(
                                                        m_stg.pthFarm, bufferReadFerm, sizeReadFerm);
                                                    if(resVal == FileResult::Success) {

                                                        do {

                                                            bufferWriteFerm[countWriteFerm++] =
                                                                bufferReadFerm[countReadFerm];
                                                            if(bufferReadFerm[countReadFerm] == 'D' &&
                                                                bufferReadFerm[++countReadFerm] == '|') {

                                                                while(bufferReadFerm[countReadFerm] != '|' ||
                                                                    bufferReadFerm[countReadFerm + 1] != '$') {
                                                                    int t_count = countReadFerm;

                                                                    try {
                                                                        t_count += 1;
                                                                        for(int i(0); i < SIZE_GUID; i++, t_count++)
                                                                            if(name[i] != bufferReadFerm[t_count])
                                                                                throw '1';

                                                                        t_count += 1;
                                                                        for(int i(0); i < SIZE_DATA; i++, t_count++)
                                                                            if(param_key.at(2)[i] !=
                                                                                bufferReadFerm[t_count])
                                                                                throw '1';

                                                                        t_count += 1;
                                                                        for(int i(0); i < SIZE_DATA; i++, t_count++)
                                                                            if(param_key.at(3)[i] !=
                                                                                bufferReadFerm[t_count])
                                                                                throw '1';

                                                                        countReadFerm = t_count;
                                                                        bufferWriteFerm[countWriteFerm++] =
                                                                            bufferReadFerm[countReadFerm];
                                                                        break;

                                                                    } catch(const char find) {
                                                                        int end = SIZE_GUID + SIZE_DATA * 2 + 3;
                                                                        for(int i = 0; i < end; i++)
                                                                            bufferWriteFerm[countWriteFerm++] =
                                                                                bufferReadFerm[countReadFerm++];
                                                                    }
                                                                }
                                                            }

                                                        } while(++countReadFerm < sizeReadFerm);

                                                        change_file = true;
                                                        *p_number_key = --number_key + '0';

                                                        resVal = CFile::RenameFile(m_stg.pthFarm, m_stg.pthTFarm);
                                                        if(resVal == FileResult::Success) {
                                                            resVal = CFile::CreateWriteFile(
                                                                m_stg.pthFarm, bufferWriteFerm, countWriteFerm);
                                                            if(resVal == FileResult::Success) {
                                                                resVal = CFile::RemoveFile(m_stg.pthTFarm);
                                                                if(resVal != FileResult::Success)
                                                                    throw 1;
                                                            }
                                                        }
                                                    }

                                                } else {

                                                    *p_number_key = number_key + '0';
                                                    memcpy(&bufferWrite[countWrite], "|#|", 3);
                                                    countWrite += 3;
                                                    memcpy(&bufferWrite[countWrite], param_key[0].data(), 1);
                                                    countWrite += 1;
                                                    bufferWrite[countWrite++] = '|';
                                                    memcpy(&bufferWrite[countWrite], param_key[1].data(), SIZE_GUID);
                                                    countWrite += SIZE_GUID;
                                                    bufferWrite[countWrite++] = '|';
                                                    memcpy(&bufferWrite[countWrite], param_key[2].data(), SIZE_DATA);
                                                    countWrite += SIZE_DATA;
                                                    bufferWrite[countWrite++] = '|';
                                                    memcpy(&bufferWrite[countWrite], param_key[3].data(), SIZE_DATA);
                                                    countWrite += SIZE_DATA;
                                                    bufferWrite[countWrite++] = '|';
                                                    memcpy(&bufferWrite[countWrite], param_key[4].data(), SIZE_GUID);
                                                    countWrite += SIZE_GUID;
                                                }
                                                break;
                                            default:
                                                throw 1;
                                        }

                                    } catch(const std::out_of_range& oor) {
                                        throw 1;
                                    }
                                } else
                                    throw 1;

                                param_key.clear();
                            }

                            if(bufferRead[++countRead] == '$') {
                                bufferWrite[countWrite++] = '|';
                                break;
                            }

                            t_string.clear();
                        } else
                            t_string.push_back(bufferRead[countRead]);

                    } else {
                        bufferWrite[countWrite++] = bufferRead[countRead];
                        if(bufferRead[countRead] == 'K' && bufferRead[++countRead] == '|') {
                            bufferWrite[countWrite++] = bufferRead[countRead];
                            p_number_key = &bufferWrite[countWrite++];
                            number_key = bufferRead[++countRead] - '0';
                        }
                    }

                } while(++countRead < sizeRead);

                if(change_file == true) {
                    while(countRead < sizeRead)
                        bufferWrite[countWrite++] = bufferRead[countRead++];

                    resVal = CFile::RenameFile(m_stg.pthUser, m_stg.pthTUser);
                    if(resVal == FileResult::Success) {
                        resVal = CFile::CreateWriteFile(m_stg.pthUser, bufferWrite, countWrite);
                        if(resVal == FileResult::Success) {
                            resVal = CFile::RemoveFile(m_stg.pthTUser);
                            if(resVal != FileResult::Success)
                                throw 1;
                        }
                    }
                }
            }
        }

        DirResult resValDir = CDir::CloseDir(dir);
        if(resValDir != DirResult::Success)
            throw 1;
    }
}
