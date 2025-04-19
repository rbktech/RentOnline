#include "user.h"

#ifdef WIN32
#define WIFEXITED(X) true
#define WEXITSTATUS(X) true
#endif // WIN32

CUser::CUser(CListLink* listLink, const int& port)
    : CBaseCommand(listLink, port)
{
    m_command.insert({ 'L', &CUser::ReadListFarm });
    m_command.insert({ 'R', &CUser::ReadFarm });

    m_command.insert({ 'A', &CUser::CreateAccount });
    m_command.insert({ 'E', &CUser::SignInAccount });
    m_command.insert({ 'C', &CUser::CheckAccount });

    m_command.insert({ 'P', &CUser::PayFarm });
    m_command.insert({ 'K', &CUser::ReadListKey });

    // m_command.insert({ 'O', &CUser::ReadListOwnerFarm });
    // m_command.insert({ 'S', &CUser::CreateNewFarm });
}

CUser::~CUser()
{
    for(auto& p : m_listLink->m_user) {
        CTCPBase::Disconnect(p.second->socket);
        p.second->thread->join();
        delete p.second->thread;
    }
}

void CUser::AddLink(int socket)
{
    CreateLink(socket, &m_listLink->m_user);
}

void CUser::CommandProcess(const uint8_t& command, const TParseData& parse, TCommand* cmd)
{
    try {

        // auto funcSend = [=](const uint8_t* sendArray, const int& sizeSend) {
        // 	m_server.Send(cmd->socket, sendArray, sizeSend);
        // };

        auto funcCommand = m_command.at(command);
        if(funcCommand != nullptr) {

            bool sequel = (this->*funcCommand)(parse, cmd);
            if(sequel == true)
                SliceMessage(
                    m_stg.GetAnswer(), m_stg.GetSizeAnswer(), m_stg.sendArray, m_stg.sizeSend, cmd->socket, m_funcSend);

            memset(m_stg.sendArray, 0, SIZE_TRAFFIC);
            m_stg.sizeSend = SIZE_TRAFFIC;
        }

    } catch(const std::out_of_range& oor) {
    }
}

// ------------------------------------------------------------------------

/*bool CUser::Init(const TParseData& data, TCommand* cmd)
{
        if(data.size() == 1) {

                uint8_t guid[SIZE_GUID] = { 0 };

                GetGuid(guid, PRF_USER);

                m_listLink->m_user.insert({ std::string((char*)guid), cmd });
        }
}*/

// ------------------------------------------------------------------------

bool CUser::ReadListFarm(const TParseData& data, TCommand* cmd)
{
    FileResult resVal = FileResult::Success;
    const char* name = nullptr;

    if(data.size() == 4) {

        m_stg.numberPerson = data.at(1).first[0];
        // m_stg.dateBegin = CharToTime((char*)&data.at(2).first[0]);
        // m_stg.dateEnd = CharToTime((char*)&data.at(3).first[0]);

        m_stg.dateBegin = ToTime(&data.at(2).first[0]);
        m_stg.dateEnd = ToTime(&data.at(3).first[0]);

        SetDoubleTown(m_stg.pthFarm, m_stg.pthPhoto, data.at(0).first);
        auto dir = CDir::OpenDir(m_stg.pthFarm);
        if(dir != nullptr) {

            try {

                while((name = CDir::GetNameDir(dir)) != nullptr) {

                    SetDoubleFarm(m_stg.pthFarm, m_stg.pthPhoto, (uint8_t*)name);
                    resVal = CFile::OpenReadFile(m_stg.pthFarm, m_stg.bufferRead, m_stg.sizeRead);
                    if(resVal == FileResult::Success) {

                        uint8_t search[2] = { 0 };

                        // Сравнение
                        while(m_stg.countRead < m_stg.sizeRead) {
                            m_stg.countRead++;
                            if(m_stg.bufferRead[m_stg.countRead - 1] == 'P' &&
                                m_stg.bufferRead[m_stg.countRead] == '|') {
                                if(m_stg.numberPerson == m_stg.bufferRead[++m_stg.countRead]) {
                                    search[0] = 1;
                                }
                            }

                            if(m_stg.bufferRead[m_stg.countRead - 1] == 'D' &&
                                m_stg.bufferRead[m_stg.countRead] == '|') {

                                while(m_stg.bufferRead[m_stg.countRead] != '|' ||
                                    m_stg.bufferRead[m_stg.countRead + 1] != '$') {

                                    long dateBegin = 0;
                                    long dateEnd = 0;
                                    TServerTime* st = (TServerTime*)&m_stg.bufferRead[m_stg.countRead];

                                    dateBegin = ToTime(st->time_begin);
                                    dateEnd = ToTime(st->time_end);

                                    // Compare date begin and end
                                    if(dateBegin < m_stg.dateBegin && m_stg.dateBegin < dateEnd) {
                                        search[1] = 0;
                                    } else if(m_stg.dateBegin < dateBegin && m_stg.dateEnd < dateEnd) {
                                        search[1] = 1;
                                    }

                                    // m_stg.dateBegin = CharToTime((char*)&data.at(2).first[0]);
                                    // m_stg.dateEnd = CharToTime((char*)&data.at(3).first[0]);

                                    m_stg.countRead += sizeof(TServerTime);
                                }

                                search[1] = 1;
                                break;
                            }
                        }

                        // Итог сравнения
                        if(search[0] == 1 && search[1] == 1 && m_stg.countRead < m_stg.sizeRead &&
                            m_stg.countRead != 0) {

                            int number_foto = 0;

                            while(m_stg.countRead < m_stg.sizeRead) {
                                m_stg.countRead++;
                                if(m_stg.bufferRead[m_stg.countRead - 1] == 'L' &&
                                    m_stg.bufferRead[m_stg.countRead] == '|') {
                                    m_stg.countRead++;
                                    SEPARATE_BUNDLE_COUNT(m_stg.answer, m_stg.a_numberParams, m_stg.a_sizeBundle);
                                    while(m_stg.bufferRead[m_stg.countRead] != '|' ||
                                        m_stg.bufferRead[m_stg.countRead + 1] != '$') {
                                        if(m_stg.bufferRead[m_stg.countRead] == '|') {
                                            m_stg.countRead++;
                                            SEPARATE_ITEM_COUNT(m_stg.answer, m_stg.a_numberParams);
                                        } else
                                            *m_stg.answer++ = m_stg.bufferRead[m_stg.countRead++];
                                    }
                                    SEPARATE_ITEM_COUNT(m_stg.answer, m_stg.a_numberParams);
                                }

                                if(m_stg.bufferRead[m_stg.countRead - 1] == 'F' &&
                                    m_stg.bufferRead[m_stg.countRead] == '|') {
                                    number_foto = m_stg.bufferRead[++m_stg.countRead] - '0';
                                    break;
                                }
                            }

                            m_stg.sizeRead = MAX_SIZE_SEND;
                            if(number_foto != 0) {
                                m_stg.pthPhoto[P_FOTO] = '0';
                                resVal = CFile::OpenReadFile(m_stg.pthPhoto, m_stg.answer, m_stg.sizeRead);
                                if(resVal == FileResult::Success) {
                                    m_stg.answer += m_stg.sizeRead;
                                    SEPARATE_ITEM_COUNT(m_stg.answer, m_stg.a_numberParams);
                                    *m_stg.a_resultCommand = COMMAND_SUCCESS;
                                } else
                                    throw 1;
                            }
                        }
                        m_stg.ClearBufferRead();
                    }
                }

                DirResult resValDir = CDir::CloseDir(dir);
                if(resValDir == DirResult::Success)
                    *m_stg.a_resultServer = SERVER_SUCCESS;

            } catch(int error) {
            }
        }
    }

    SEPARATE_END(m_stg.answer);
    return true;
}

// ------------------------------------------------------------------------

bool CUser::ReadFarm(const TParseData& data, TCommand* cmd)
{
    FileResult resVal = FileResult::Success;

    if(data.size() == 2) {
        SetDoubleTown(m_stg.pthFarm, m_stg.pthPhoto, data.at(0).first);
        SetDoubleFarm(m_stg.pthFarm, m_stg.pthPhoto, data.at(1).first);

        resVal = CFile::OpenReadFile(m_stg.pthFarm, m_stg.bufferRead, m_stg.sizeRead);
        if(resVal == FileResult::Success) {
            try {
                while(m_stg.countRead < m_stg.sizeRead) {
                    m_stg.countRead++;
                    if(m_stg.bufferRead[m_stg.countRead - 1] == 'R' && m_stg.bufferRead[m_stg.countRead] == '|') {
                        m_stg.countRead++;
                        SEPARATE_BUNDLE_COUNT(m_stg.answer, m_stg.a_numberParams, m_stg.a_sizeBundle);
                        while(
                            m_stg.bufferRead[m_stg.countRead] != '|' || m_stg.bufferRead[m_stg.countRead + 1] != '$') {
                            if(m_stg.bufferRead[m_stg.countRead] == '|') {
                                m_stg.countRead++;
                                SEPARATE_ITEM_COUNT(m_stg.answer, m_stg.a_numberParams);
                            } else
                                *m_stg.answer++ = m_stg.bufferRead[m_stg.countRead++];
                        }
                        SEPARATE_ITEM_COUNT(m_stg.answer, m_stg.a_numberParams);
                    }

                    if(m_stg.bufferRead[m_stg.countRead - 1] == 'C' && m_stg.bufferRead[m_stg.countRead] == '|') {
                        *m_stg.answer++ = '0'; // Number comments
                        SEPARATE_ITEM_COUNT(m_stg.answer, m_stg.a_numberParams);
                    }

                    if(m_stg.bufferRead[m_stg.countRead - 1] == 'F' && m_stg.bufferRead[m_stg.countRead] == '|') {
                        int number_foto = m_stg.bufferRead[++m_stg.countRead] - '0'; // Number photos
                        *m_stg.answer++ = m_stg.bufferRead[m_stg.countRead];
                        SEPARATE_ITEM_COUNT(m_stg.answer, m_stg.a_numberParams);
                        for(int i = 1; i < number_foto; i++) {
                            m_stg.pthPhoto[P_FOTO] = i + '0';
                            m_stg.sizeWrite = MAX_SIZE_SEND;
                            resVal = CFile::OpenReadFile(m_stg.pthPhoto, m_stg.answer, m_stg.sizeWrite);
                            if(resVal == FileResult::Success) {
                                m_stg.answer += m_stg.sizeWrite;
                                SEPARATE_ITEM_COUNT(m_stg.answer, m_stg.a_numberParams);
                            } else
                                throw 1;
                        }
                    }
                }

                *m_stg.a_resultServer = SERVER_SUCCESS;
                *m_stg.a_resultCommand = COMMAND_SUCCESS;

            } catch(int error) {
            }
        }
    }

    SEPARATE_END(m_stg.answer);
    return true;
}

// ------------------------------------------------------------------------

bool CUser::CreateAccount(const TParseData& data, TCommand* cmd)
{
    FileResult resVal = FileResult::Success;

    uint8_t hash[SIZE_HASH] = { 0 };

    std::string guid = GetGuid(nullptr, PRF_USER);
    GetHash(hash, nullptr);

    if(data.size() == 5) {

        {
            m_stg.bufferWrite[m_stg.countWrite++] = 'F';
            m_stg.bufferWrite[m_stg.countWrite++] = 'F';
            m_stg.bufferWrite[m_stg.countWrite++] = 'F';
            m_stg.bufferWrite[m_stg.countWrite++] = 'F';
            m_stg.bufferWrite[m_stg.countWrite++] = '\n';

            m_stg.bufferWrite[m_stg.countWrite++] = 'A';
            m_stg.bufferWrite[m_stg.countWrite++] = '|';

            for(int i = 0; i < data.at(0).second; i++)
                m_stg.bufferWrite[m_stg.countWrite++] = data.at(0).first[i];

            m_stg.bufferWrite[m_stg.countWrite++] = '|';

            for(int i = 0; i < data.at(1).second; i++)
                m_stg.bufferWrite[m_stg.countWrite++] = data.at(1).first[i];

            memcpy(&m_stg.bufferWrite[m_stg.countWrite], "|$\nF|", 5);
            m_stg.countWrite += 5;

            memcpy(&m_stg.bufferWrite[m_stg.countWrite], hash, SIZE_HASH);
            m_stg.countWrite += SIZE_HASH;

            memcpy(&m_stg.bufferWrite[m_stg.countWrite], "|$\nO|0|$\nK|0|$\n", 15);
            m_stg.countWrite += 15;
        }

        SetDoubleUser(m_stg.pthUser, m_stg.pthUserDir, (uint8_t*)guid.data());

        DirResult resValDir = CDir::CreateDir(m_stg.pthUserDir);
        if(resValDir == DirResult::Success) {
            resVal = CFile::CreateWriteFile(m_stg.pthUser, m_stg.bufferWrite, m_stg.countWrite);
            if(resVal == FileResult::Success) {

                SetUser(m_stg.pthUserPhoto, (uint8_t*)guid.data());
                SetUserPhoto(m_stg.pthUserPhoto, hash);

                resVal = CFile::CreateWriteFile(m_stg.pthUserPhoto, data.at(4).first, data.at(4).second);
                if(resVal == FileResult::Success) {

                    m_listLink->m_user.insert({ guid, cmd });

                    SEPARATE_BUNDLE_COUNT(m_stg.answer, m_stg.a_numberParams, m_stg.a_sizeBundle);

                    memcpy(m_stg.answer, data.at(0).first, data.at(0).second);
                    m_stg.answer += data.at(0).second;

                    SEPARATE_ITEM_COUNT(m_stg.answer, m_stg.a_numberParams);

                    *m_stg.a_resultServer = SERVER_SUCCESS;
                    *m_stg.a_resultCommand = COMMAND_SUCCESS;

                    SEPARATE_END(m_stg.answer);

                    /*auto itr = m_listLink->m_moderator.begin();
                    if(itr != m_listLink->m_moderator.end()) {

                            TCommand* cmdModerator = itr->second;
                            if(cmdModerator != nullptr) {

                                    {
                                            memcpy(m_stg.answer, guid.data(), SIZE_GUID);
                                            m_stg.answer += SIZE_GUID;

                                            SEPARATE_ITEM(m_stg.answer);

                                            memcpy(m_stg.answer, data.at(0).first, data.at(0).second);
                                            m_stg.answer += data.at(0).second;

                                            SEPARATE_ITEM(m_stg.answer);

                                            memcpy(m_stg.answer, data.at(1).first, data.at(1).second);
                                            m_stg.answer += data.at(1).second;

                                            SEPARATE_ITEM(m_stg.answer);

                                            memcpy(m_stg.answer, data.at(2).first, data.at(2).second);
                                            m_stg.answer += data.at(2).second;

                                            SEPARATE_ITEM(m_stg.answer);

                                            memcpy(m_stg.answer, data.at(3).first, data.at(3).second);
                                            m_stg.answer += data.at(3).second;

                                            SEPARATE_ITEM(m_stg.answer);

                                            memcpy(m_stg.answer, data.at(4).first, data.at(4).second);
                                            m_stg.answer += data.at(4).second;

                                            SEPARATE_ITEM(m_stg.answer);

                                            *m_stg.a_resultServer = SERVER_SUCCESS;
                                            *m_stg.a_resultCommand = COMMAND_SUCCESS;

                                            SEPARATE_END(m_stg.answer);
                                    }

                                    SliceMessage(m_stg.GetAnswer(), m_stg.GetSizeAnswer(), m_stg.sendArray,
                    m_stg.sizeSend, cmdModerator->socket, m_funcSend);
                            }
                    }*/
                }
            }
        }
    }
    return true;
}

bool CUser::SignInAccount(const TParseData& data, TCommand* cmd)
{
    FileResult resVal = FileResult::Success;
    const char* name = nullptr;

    if(data.size() == 2) {

        auto dir = CDir::OpenDir(m_stg.pthUser);
        if(dir != nullptr) {
            try {
                while((name = CDir::GetNameDir(dir)) != nullptr) {
                    try {

                        SetUser(m_stg.pthUser, (uint8_t*)name);

                        m_stg.ClearBufferRead();

                        resVal = CFile::OpenReadFile(m_stg.pthUser, m_stg.bufferRead, m_stg.sizeRead);
                        if(resVal == FileResult::Success) {

                            do {
                                if(m_stg.bufferRead[m_stg.countRead] == 'A' &&
                                    m_stg.bufferRead[m_stg.countRead + 1] == '|') {
                                    m_stg.countRead += 2;

                                    for(int i = 0; i < data.at(0).second; i++)
                                        if(m_stg.bufferRead[m_stg.countRead++] != data.at(0).first[i])
                                            throw 1;

                                    m_stg.countRead++;

                                    for(int i = 0; i < data.at(1).second; i++)
                                        if(m_stg.bufferRead[m_stg.countRead++] != data.at(1).first[i])
                                            throw 1;

                                    throw '1';
                                }

                                // function
                            } while(++m_stg.countRead < m_stg.sizeRead);
                        }

                    } catch(int next_user) {
                    }
                }
                *m_stg.a_resultCommand = COMMAND_NOT_FOUND;
            } catch(char success) {

                SEPARATE_BUNDLE_COUNT(m_stg.answer, m_stg.a_numberParams, m_stg.a_sizeBundle);

                memcpy(m_stg.answer, name, SIZE_GUID);
                m_stg.answer += SIZE_GUID;
                SEPARATE_ITEM_COUNT(m_stg.answer, m_stg.a_numberParams);

                *m_stg.a_resultCommand = COMMAND_SUCCESS;
            }
        }
        DirResult resValDir = CDir::CloseDir(dir);
        if(resValDir == DirResult::Success)
            *m_stg.a_resultServer = SERVER_SUCCESS;
    }

    SEPARATE_END(m_stg.answer);

    return true;
}

bool CUser::CheckAccount(const TParseData& data, TCommand* cmd)
{
    FileResult resVal = FileResult::Success;
    const char* name = nullptr;

    if(data.size() == 1 && data.at(0).second > 2 && data.at(0).first[0] == 'A' && data.at(0).first[1] == 'M') {

        auto dir = CDir::OpenDir(m_stg.pthUser);
        if(dir != nullptr) {
            try {
                while((name = CDir::GetNameDir(dir)) != nullptr) {
                    try {

                        SetUser(m_stg.pthUser, (uint8_t*)name);

                        m_stg.ClearBufferRead();

                        resVal = CFile::OpenReadFile(m_stg.pthUser, m_stg.bufferRead, m_stg.sizeRead);
                        if(resVal == FileResult::Success) {

                            do {
                                if(m_stg.bufferRead[m_stg.countRead] == 'A' &&
                                    m_stg.bufferRead[m_stg.countRead + 1] == '|') {
                                    m_stg.countRead += 2;

                                    for(int i = 2; i < data.at(0).second; i++)
                                        if(m_stg.bufferRead[m_stg.countRead++] != data.at(0).first[i])
                                            throw 1;

                                    throw '1';
                                }

                                // function
                            } while(++m_stg.countRead < m_stg.sizeRead);
                        }

                    } catch(int next_user) {
                    }
                }
                *m_stg.a_resultCommand = COMMAND_NOT_FOUND;
            } catch(char success) {

                uint8_t result = -1;

                if(m_stg.bufferRead[0] == 'F')
                    if(m_stg.bufferRead[1] == 'F')
                        if(m_stg.bufferRead[2] == 'F')
                            if(m_stg.bufferRead[3] == 'F')
                                result = 2;

                if(m_stg.bufferRead[0] == 'A')
                    if(m_stg.bufferRead[1] == 'A')
                        if(m_stg.bufferRead[2] == 'A')
                            if(m_stg.bufferRead[3] == 'A')
                                result = 1;

                if(m_stg.bufferRead[0] == 'A')
                    if(m_stg.bufferRead[1] == '|')
                        result = 0;

                SEPARATE_BUNDLE_COUNT(m_stg.answer, m_stg.a_numberParams, m_stg.a_sizeBundle);

                *m_stg.answer++ = result;
                SEPARATE_ITEM_COUNT(m_stg.answer, m_stg.a_numberParams);

                if(result == 0) {
                    memcpy(m_stg.answer, name, SIZE_GUID);
                    m_stg.answer += SIZE_GUID;
                    SEPARATE_ITEM_COUNT(m_stg.answer, m_stg.a_numberParams);
                }

                *m_stg.a_resultCommand = COMMAND_SUCCESS;
            }
        }
        DirResult resValDir = CDir::CloseDir(dir);
        if(resValDir == DirResult::Success)
            *m_stg.a_resultServer = SERVER_SUCCESS;
    }

    SEPARATE_END(m_stg.answer);

    return true;
}

// ------------------------------------------------------------------------

int CUser::PayScript(const uint8_t* token, const int sizeToken, int delay)
{
    int result = 0;
    const int size_name_script = 16;
    uint8_t run_script[size_name_script + sizeToken + 1] = "./pay_script.py ";
    memcpy(&run_script[size_name_script], token, sizeToken);

    std::string str = std::string((char*)run_script);

    int status = system((char*)run_script);

    std::this_thread::sleep_for(std::chrono::milliseconds(delay));

    if(status >= 0) {
        if(WIFEXITED(status)) {
            result = WEXITSTATUS(status);
            printf("Program returned normally, exit code %d\n", result);
        } else {
            printf("Program exited abnormaly\n");
            throw 1;
        }
    } else {
        printf("Error: %s\n", strerror(errno));
        throw 1;
    }

    if(result == PAYMENT_SUCCEEDED)
        printf("PAYMENT_SUCCEEDED\n");
    else if(result == PAYMENT_CANCELED)
        throw printf("PAYMENT_CANCELED\n");
    else if(result == PAYMENT_WAITING_FOR_CAPTURE)
        throw printf("PAYMENT_WAITING_FOR_CAPTURE\n");
    else if(result == REFUND_SUCCEEDED)
        throw printf("REFUND_SUCCEEDED\n");

    return result;
}

bool CUser::PayFarm(const TParseData& data, TCommand* cmd)
{
    int result = 0;
    uint8_t tag[SIZE_GUID] = { 0 };

    FileResult resVal = FileResult::Success;

    try {
        if(data.size() == 6) {

            SEPARATE_BUNDLE_COUNT(m_stg.answer, m_stg.a_numberParams, m_stg.a_sizeBundle);

            // result = PayScript(data.at(5).first, data.at(5).second, 3000);
            if(result != 0) {
                *m_stg.answer++ = '1';
                throw 1;
            } else
                *m_stg.answer++ = '0';

            SEPARATE_ITEM_COUNT(m_stg.answer, m_stg.a_numberParams);

            SetDoubleTown(m_stg.pthFarm, m_stg.pthTFarm, data.at(0).first);
            SetDoubleFarm(m_stg.pthFarm, m_stg.pthTFarm, data.at(1).first);
            SetDoubleUser(m_stg.pthUser, m_stg.pthTUser, data.at(4).first);
            memcpy(tag, &std::to_string(time(NULL))[0], SIZE_GUID);

            {
                TCommand* cmdDoor = m_listLink->m_door.at((char*)data.at(1).first);
                if(cmdDoor != nullptr) {

                    TMarkDoor mdr;
                    mdr.a = 0xFD;
                    mdr.b = 0xFA;
                    mdr.c = 0xFC;
                    mdr.d = 0xF3;
                    mdr.e = 0xE8;

                    memcpy(mdr.value, tag, SIZE_GUID);

                    mdr.f = 0;
                    mdr.g = 0;
                    mdr.h = 0;
                    mdr.i = 0;
                    mdr.j = 0;

                    m_server.Send(cmdDoor->socket, (uint8_t*)&mdr, sizeof(TMarkDoor));

                    *m_stg.answer++ = '0';
                    SEPARATE_ITEM_COUNT(m_stg.answer, m_stg.a_numberParams);
                }
            }

            resVal = CFile::OpenReadFile(m_stg.pthFarm, m_stg.bufferRead, m_stg.sizeRead);
            if(resVal == FileResult::Success) {
                while(m_stg.countRead < m_stg.sizeRead) {
                    m_stg.bufferWrite[m_stg.countWrite++] = m_stg.bufferRead[m_stg.countRead++];
                    if(m_stg.bufferRead[m_stg.countRead - 1] == 'D' && m_stg.bufferRead[m_stg.countRead] == '|') {
                        m_stg.bufferWrite[m_stg.countWrite++] = '|';
                        memcpy(&m_stg.bufferWrite[m_stg.countWrite], data.at(4).first, data.at(4).second);
                        m_stg.countWrite += data.at(4).second;
                        m_stg.bufferWrite[m_stg.countWrite++] = '|';
                        memcpy(&m_stg.bufferWrite[m_stg.countWrite], data.at(2).first, data.at(2).second);
                        m_stg.countWrite += data.at(2).second;
                        m_stg.bufferWrite[m_stg.countWrite++] = '|';
                        memcpy(&m_stg.bufferWrite[m_stg.countWrite], data.at(3).first, data.at(3).second);
                        m_stg.countWrite += data.at(3).second;
                    }

                    /*if(m_stg.bufferRead[m_stg.countRead - 1] == 'I' && m_stg.bufferRead[m_stg.countRead] == '|') {
                            for(int i(0), j(countRead + 1); bufferRead[j] != '|' || bufferRead[j + 1] != '$'; i++, j++)
                                    ip[i] = bufferRead[j];

                            result = CLinkLinux::InitClientAndSendGuid(ip, tag, SIZE_GUID);
                            if(result == 1)
                                    throw 1;

                            int sizeSend = SIZE_MARK_DOOR + SIZE_GUID;
                            uint8_t message[sizeSend] = { 0xFD, 0xFA, 0xFC, 0xF3, 0xE8 };
                            memcpy(&message[SIZE_MARK_DOOR], tag, SIZE_GUID);

                            result = m_linkDoor->Send(m_socketDoor->back(), message, sizeSend);
                            if(result != 0)
                                    throw 1;
                    }*/
                }

                resVal = CFile::RenameFile(m_stg.pthFarm, m_stg.pthTFarm);
                if(resVal == FileResult::Success) {
                    resVal = CFile::CreateWriteFile(m_stg.pthFarm, m_stg.bufferWrite, m_stg.countWrite);
                    if(resVal == FileResult::Success) {
                        resVal = CFile::RemoveFile(m_stg.pthTFarm);
                        if(resVal != FileResult::Success) {
                            throw 1;
                        }
                    } else
                        throw 1;
                } else
                    throw 1;
            } else
                throw 1;

            m_stg.ClearBufferRead();
            m_stg.ClearBufferWrite();

            resVal = CFile::OpenReadFile(m_stg.pthUser, m_stg.bufferRead, m_stg.sizeRead);
            if(resVal == FileResult::Success) {
                while(m_stg.countRead < m_stg.sizeRead) {
                    m_stg.bufferWrite[m_stg.countWrite++] = m_stg.bufferRead[m_stg.countRead++];
                    if(m_stg.bufferRead[m_stg.countRead - 1] == 'K' && m_stg.bufferRead[m_stg.countRead] == '|') {
                        m_stg.bufferWrite[m_stg.countWrite++] = '|';
                        int number_key = m_stg.bufferRead[(++m_stg.countRead)++] - '0';
                        m_stg.bufferWrite[m_stg.countWrite++] = ++number_key + '0';
                        memcpy(&m_stg.bufferWrite[m_stg.countWrite], "|#|0|", 5);
                        m_stg.countWrite += 5;
                        memcpy(&m_stg.bufferWrite[m_stg.countWrite], data.at(1).first, data.at(1).second);
                        m_stg.countWrite += data.at(1).second;
                        m_stg.bufferWrite[m_stg.countWrite++] = '|';
                        memcpy(&m_stg.bufferWrite[m_stg.countWrite], data.at(2).first, data.at(2).second);
                        m_stg.countWrite += data.at(2).second;
                        m_stg.bufferWrite[m_stg.countWrite++] = '|';
                        memcpy(&m_stg.bufferWrite[m_stg.countWrite], data.at(3).first, data.at(3).second);
                        m_stg.countWrite += data.at(3).second;
                        m_stg.bufferWrite[m_stg.countWrite++] = '|';
                        memcpy(&m_stg.bufferWrite[m_stg.countWrite], tag, SIZE_GUID);
                        m_stg.countWrite += SIZE_GUID;
                    }
                }

                resVal = CFile::RenameFile(m_stg.pthUser, m_stg.pthTUser);
                if(resVal == FileResult::Success) {
                    resVal = CFile::CreateWriteFile(m_stg.pthUser, m_stg.bufferWrite, m_stg.countWrite);
                    if(resVal == FileResult::Success) {
                        resVal = CFile::RemoveFile(m_stg.pthTUser);
                        if(resVal == FileResult::Success) {
                            *m_stg.a_resultServer = SERVER_SUCCESS;
                            *m_stg.a_resultCommand = COMMAND_SUCCESS;
                        } else
                            throw 1;
                    } else
                        throw 1;
                } else
                    throw 1;

            } else
                throw 1;
        } else
            throw 1;

    } catch(int error) {
        SEPARATE_ITEM_COUNT(m_stg.answer, m_stg.a_numberParams);
    } catch(const std::out_of_range& oor) {
        SEPARATE_ITEM_COUNT(m_stg.answer, m_stg.a_numberParams);
    }

    SEPARATE_END(m_stg.answer);
    return true;
}

// ------------------------------------------------------------------------

bool CUser::ReadListKey(const TParseData& data, TCommand* cmd)
{
    FileResult resVal = FileResult::Success;

    if(data.size() == 1) {

        if(data.at(0).first[0] == 'A') {
            SEPARATE_BUNDLE_COUNT(m_stg.answer, m_stg.a_numberParams, m_stg.a_sizeBundle);
            *m_stg.a_resultCommand = COMMAND_SUCCESS;
            *m_stg.a_resultServer = SERVER_SUCCESS;
            *m_stg.answer++ = 'A';
            SEPARATE_ITEM_COUNT(m_stg.answer, m_stg.a_numberParams);
            SEPARATE_END(m_stg.answer);
            return true;
        }

        SetUser(m_stg.pthUser, data.at(0).first);

        resVal = CFile::OpenReadFile(m_stg.pthUser, m_stg.bufferRead, m_stg.sizeRead);
        if(resVal == FileResult::Success) {

            do {
                if(m_stg.bufferRead[m_stg.countRead - 1] == 'K' && m_stg.bufferRead[m_stg.countRead] == '|') {

                    SEPARATE_BUNDLE_COUNT(m_stg.answer, m_stg.a_numberParams, m_stg.a_sizeBundle);
                    char number_count = m_stg.bufferRead[(++m_stg.countRead)++]; // Numbers keys
                    *m_stg.answer++ = number_count;
                    SEPARATE_ITEM_COUNT(m_stg.answer, m_stg.a_numberParams);

                    if(number_count == '0') {
                        // *m_stg.a_resultCommand = COMMAND_NOT_FOUND;
                        *m_stg.a_resultCommand = COMMAND_SUCCESS;
                        *m_stg.a_resultServer = SERVER_SUCCESS;
                        break;
                    }

                    while(m_stg.countRead < m_stg.sizeRead &&
                        (m_stg.bufferRead[m_stg.countRead] != '|' || m_stg.bufferRead[m_stg.countRead + 1] != '$')) {
                        m_stg.countRead++;
                        if(m_stg.bufferRead[m_stg.countRead] == '#' && m_stg.bufferRead[m_stg.countRead + 1] == '|') {
                            m_stg.countRead += 3;
                            SEPARATE_BUNDLE_COUNT(m_stg.answer, m_stg.a_numberParams, m_stg.a_sizeBundle);
                            // SEPARATE_BUNDLE(m_stg.answer);
                        } else {
                            if(m_stg.bufferRead[m_stg.countRead] == '|') {
                                SEPARATE_ITEM_COUNT(m_stg.answer, m_stg.a_numberParams);
                            } else
                                *m_stg.answer++ = m_stg.bufferRead[m_stg.countRead];
                        }
                    }

                    *m_stg.a_resultCommand = COMMAND_SUCCESS;
                    *m_stg.a_resultServer = SERVER_SUCCESS;
                }

            } while(m_stg.countRead++ < m_stg.sizeRead);
        }
    }

    SEPARATE_END(m_stg.answer);
    return true;
}

// ------------------------------------------------------------------------

bool CUser::CreateNewFarm(const TParseData& data, TCommand* cmd)
{
    FileResult resVal = FileResult::Success;

    // char path_users[] = { '.', '/', 'u', 's', 'e', 'r', 's', '/', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
    // '\0', '\0', '/', 'f', 'i', 'l', 'e', '.', 't', 'x', 't', '\0' }; char path_temp_users[] = { '.', '/', 'u', 's',
    // 'e', 'r', 's', '/', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '/', '_', 'f', 'i', 'l', 'e',
    // '.', 't', 'x', 't', '\0' };

    // char path_foto[] = { '.', '/', 'f', 'e', 'r', 'm', 's', '/', '\0', '\0', '\0', '/', '\0', '\0', '\0', '\0', '\0',
    // '\0', '\0', '\0', '\0', '\0', '/', 'f', 'o', 't', 'o', '_', '\0', '.', 'j', 'p', 'g', '\0' }; char path_ferms[] =
    // { '.', '/', 'f', 'e', 'r', 'm', 's', '/', '\0', '\0', '\0', '/', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
    // '\0', '\0', '/', 'f', 'i', 'l', 'e', '.', 't', 'x', 't', '\0' }; char path_ferms_dir[] = { '.', '/', 'f', 'e',
    // 'r', 'm', 's', '/', '\0', '\0', '\0', '/', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0' };

    TParseData parse;
    int size = SIZE_BUFFER;
    int countRead = 0;
    int countWrite = 0;
    uint8_t bufferRead[SIZE_BUFFER] = { 0 };
    uint8_t bufferWrite[SIZE_BUFFER] = { 0 };
    uint8_t guid[SIZE_GUID] = { 0 };

    /*SEPARATE_S(answer);
    SEPARATE_BUNDLE(answer);
    uint8_t* result_server = answer++;
    *result_server = SERVER_ERROR;
    SEPARATE_ITEM(answer);

    ParseCommand(in, sizeIn, data);*/

    unsigned number_photo = data.at(13).first[0];

    memcpy(guid, &std::to_string(time(NULL))[0], SIZE_GUID);

    if(data.size() == 14 + number_photo) {

        SetDoubleUser(m_stg.pthUser, m_stg.pthTUser, data.at(0).first);

        SetDoubleTown(m_stg.pthFarm, m_stg.pthPhoto, data.at(1).first);
        SetDoubleFarm(m_stg.pthFarm, m_stg.pthPhoto, guid);

        DirResult resValDir = CDir::CreateDir(m_stg.pthFarm);
        if(resValDir == DirResult::Success) {

            bufferWrite[countWrite++] = 'P';
            bufferWrite[countWrite++] = '|';
            for(const uint8_t* ch = data.at(2).first; *ch != '\0';)
                bufferWrite[countWrite++] = *ch++;

            memcpy(&bufferWrite[countWrite], "|$\nD|$\nL|", 9);
            countWrite += 9;

            memcpy(&bufferWrite[countWrite], guid, SIZE_GUID);
            countWrite += SIZE_GUID;
            bufferWrite[countWrite++] = '|';

            for(const uint8_t* ch = data.at(1).first; *ch != '\0';)
                bufferWrite[countWrite++] = *ch++;
            bufferWrite[countWrite++] = '|';
            for(const uint8_t* ch = data.at(3).first; *ch != '\0';)
                bufferWrite[countWrite++] = *ch++;
            bufferWrite[countWrite++] = '|';
            for(const uint8_t* ch = data.at(4).first; *ch != '\0';)
                bufferWrite[countWrite++] = *ch++;
            bufferWrite[countWrite++] = '|';
            for(const uint8_t* ch = data.at(5).first; *ch != '\0';)
                bufferWrite[countWrite++] = *ch++;
            bufferWrite[countWrite++] = '|';
            for(const uint8_t* ch = data.at(6).first; *ch != '\0';)
                bufferWrite[countWrite++] = *ch++;
            bufferWrite[countWrite++] = '|';
            for(const uint8_t* ch = data.at(7).first; *ch != '\0';)
                bufferWrite[countWrite++] = *ch++;
            bufferWrite[countWrite++] = '|';
            for(const uint8_t* ch = data.at(8).first; *ch != '\0';)
                bufferWrite[countWrite++] = *ch++;
            memcpy(&bufferWrite[countWrite], "|$\nR|", 5);
            countWrite += 5;
            for(const uint8_t* ch = data.at(9).first; *ch != '\0';)
                bufferWrite[countWrite++] = *ch++;
            bufferWrite[countWrite++] = '|';
            for(const uint8_t* ch = data.at(10).first; *ch != '\0';)
                bufferWrite[countWrite++] = *ch++;
            bufferWrite[countWrite++] = '|';
            for(const uint8_t* ch = data.at(11).first; *ch != '\0';)
                bufferWrite[countWrite++] = *ch++;
            memcpy(&bufferWrite[countWrite], "|$\nI|", 5);
            countWrite += 5;
            for(const uint8_t* ch = data.at(12).first; *ch != '\0';)
                bufferWrite[countWrite++] = *ch++;
            memcpy(&bufferWrite[countWrite], "|$\nU|", 5);
            countWrite += 5;
            for(const uint8_t* ch = data.at(0).first; *ch != '\0';)
                bufferWrite[countWrite++] = *ch++;
            memcpy(&bufferWrite[countWrite], "|$\nF|", 5);
            countWrite += 5;

            bufferWrite[countWrite++] = number_photo + '0';

            memcpy(&bufferWrite[countWrite], "|$\nC|0|$", 8);
            countWrite += 8;

            resVal = CFile::CreateWriteFile(m_stg.pthFarm, bufferWrite, countWrite);
            if(resVal == FileResult::Success) {

                countWrite = 0;
                memset(bufferWrite, 0, SIZE_BUFFER);

                resVal = CFile::OpenReadFile(m_stg.pthUser, bufferRead, size);
                if(resVal == FileResult::Success) {

                    while(countRead < size) {
                        bufferWrite[countWrite++] = bufferRead[countRead++];
                        if(bufferRead[countRead - 1] == 'O' && bufferRead[countRead] == '|') {
                            bufferWrite[countWrite++] = bufferRead[countRead++];
                            int number_ferms = bufferRead[countRead++] - '0';
                            bufferWrite[countWrite++] = ++number_ferms + '0';
                            bufferWrite[countWrite++] = '|';
                            memcpy(&bufferWrite[countWrite], guid, SIZE_GUID);
                            countWrite += SIZE_GUID;
                        }
                    }

                    resVal = CFile::RenameFile(m_stg.pthUser, m_stg.pthTUser);
                    if(resVal == FileResult::Success) {

                        resVal = CFile::CreateWriteFile(m_stg.pthUser, bufferWrite, countWrite);
                        if(resVal == FileResult::Success) {

                            resVal = CFile::RemoveFile(m_stg.pthTUser);
                            if(resVal == FileResult::Success) {

                                try {
                                    for(unsigned i = 0; i < number_photo; i++) {
                                        if(parse.size() > 14 + i) {
                                            m_stg.pthPhoto[P_FOTO] = i + '0';
                                            resVal = CFile::CreateWriteFile(
                                                m_stg.pthPhoto, parse.at(14 + i).first, parse.at(14 + i).second);
                                            if(resVal != FileResult::Success)
                                                throw 1;

                                        } else
                                            throw 1;
                                    }
                                    *m_stg.a_resultServer = SERVER_SUCCESS;
                                } catch(int error) {
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    SEPARATE_END(m_stg.answer);
    return true;
}

bool CUser::ReadListOwnerFarm(const TParseData& data, TCommand* cmd)
{
    int count = 1;
    int size = SIZE_BUFFER;
    uint8_t buffer[SIZE_BUFFER] = { 0 };

    int countFerm = 1;
    int sizeFerm = SIZE_BUFFER;
    uint8_t bufferFerm[SIZE_BUFFER] = { 0 };

    FileResult resVal = FileResult::Success;

    // char path_user[] = { '.', '/', 'u', 's', 'e', 'r', 's', '/', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
    // '\0', '\0', '/', 'f', 'i', 'l', 'e', '.', 't', 'x', 't', '\0' }; char path_ferm[] = { '.', '/', 'f', 'e', 'r',
    // 'm', 's', '/', '\0', '\0', '\0', '/', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '/', 'f', 'i',
    // 'l', 'e', '.', 't', 'x', 't', '\0' }; char path_foto[] = { '.', '/', 'f', 'e', 'r', 'm', 's', '/', '\0', '\0',
    // '\0', '/', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '/', 'f', 'o', 't', 'o', '_', '0', '.',
    // 'j', 'p', 'g', '\0' };

    /*SEPARATE_O(answer);
    SEPARATE_BUNDLE(answer);
    uint8_t* result_server = answer++;
    *result_server = SERVER_ERROR;
    SEPARATE_ITEM(answer);

    TParseData data;
    ParseCommand(in, sizeIn, data);*/
    if(data.size() == 2) {

        SetUser(m_stg.pthUser, data.at(0).first);

        resVal = CFile::OpenReadFile(m_stg.pthUser, buffer, size);
        if(resVal == FileResult::Success) {
            do {
                if(buffer[count - 1] == 'O' && buffer[count] == '|') {

                    // char number_count = buffer[(++count)++];
                    // *answer++ = number_count;
                    // SEPARATE_ITEM(answer);

                    while(count < size && (buffer[count] != '|' || buffer[count + 1] != '$')) {

                        SetDoubleTown(m_stg.pthFarm, m_stg.pthPhoto, data.at(1).first);
                        SetDoubleFarm(m_stg.pthFarm, m_stg.pthPhoto, &buffer[++count]);
                        count += SIZE_GUID;

                        resVal = CFile::OpenReadFile(m_stg.pthFarm, bufferFerm, sizeFerm);
                        if(resVal == FileResult::Success) {
                            // SEPARATE_BUNDLE(answer)
                            do {
                                if(bufferFerm[countFerm - 1] == 'L' && bufferFerm[countFerm] == '|') {

                                    while(countFerm++ < sizeFerm &&
                                        (bufferFerm[countFerm] != '|' || bufferFerm[countFerm + 1] != '$')) {

                                        // if(bufferFerm[countFerm] == '|') {
                                        // 	SEPARATE_ITEM(answer);
                                        // } else
                                        // 	*answer++ = bufferFerm[countFerm];
                                    }
                                    // SEPARATE_ITEM(answer);
                                    break;
                                }
                            } while(countFerm++ < sizeFerm);
                        } else {
                            // SEPARATE_END(answer);
                            return true;
                        }

                        countFerm = 0;
                        sizeFerm = SIZE_BUFFER;
                        memset(bufferFerm, 0, SIZE_BUFFER);

                        resVal = CFile::OpenReadFile(m_stg.pthPhoto, bufferFerm, sizeFerm);
                        if(resVal == FileResult::Success) {
                            // memcpy(answer, bufferFerm, sizeFerm);
                            // answer += sizeFerm;
                            // SEPARATE_ITEM(answer);
                        } else {
                            // SEPARATE_END(answer);
                            return true;
                            ;
                        }
                    }
                    *m_stg.a_resultServer = SERVER_SUCCESS;
                    break;
                }
            } while(count++ < size);
        }
    }

    // SEPARATE_END(answer);
    // ClearTBuffer(data);
    return true;
}
