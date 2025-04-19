#include "moderator.h"

CModerator::CModerator(CListLink* listLink, const int& port)
    : CBaseCommand(listLink, port)
{
    m_command.insert({ 'A', &CModerator::ResponseFromModerator });
    m_command.insert({ 'D', &CModerator::PermissionModerator });
    m_command.insert({ 'C', &CModerator::CheckNewAccount });
}

CModerator::~CModerator()
{
    for(auto& p : m_listLink->m_moderator) {
        CTCPBase::Disconnect(p.second->socket);
        p.second->thread->join();
        delete p.second->thread;
    }
}

void CModerator::AddLink(int socket)
{

    std::string guid = GetGuid(nullptr, PRF_USER);

    // std::string strGuid = std::string((char*)arrayGuid);

    TCommand* cmd = CreateLink(socket, &m_listLink->m_moderator);

    m_listLink->m_moderator.insert({ guid, cmd });
}

void CModerator::CommandProcess(const uint8_t& command, const TParseData& parse, TCommand* cmd)
{
    try {

        // auto func = m_command.at(command);
        // if(func != nullptr)
        // 	(this->*func)(parse);

        auto func = m_command.at(command);
        if(func != nullptr) {

            bool sequel = (this->*func)(parse);
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

bool CModerator::ResponseFromModerator(const TParseData& data)
{
    /*if(data.size() == 6) {

            try {

                    TCommand* cmd = m_listLink->m_user.at((char*)data.at(4).first);

                    SEPARATE_BUNDLE_COUNT(m_stg.answer, m_stg.a_numberParams, m_stg.a_sizeBundle);

                    memcpy(m_stg.answer, data.at(5).first, data.at(5).second);
                    m_stg.answer += data.at(5).second;

                    SEPARATE_ITEM_COUNT(m_stg.answer, m_stg.a_numberParams);
                    SEPARATE_END(m_stg.answer);

                    *m_stg.a_resultCommand = COMMAND_SUCCESS;
                    *m_stg.a_resultServer = SERVER_SUCCESS;

                    SliceMessage(m_stg.GetAnswer(), m_stg.GetSizeAnswer(), m_stg.sendArray, m_stg.sizeSend, cmd->socket,
    m_funcSend);

            } catch(const std::out_of_range& oor) {

            }
    }*/

    return false;
}

bool CModerator::PermissionModerator(const TParseData& data)
{
    FileResult resVal = FileResult::Success;

    uint8_t resultCommand = -1;

    if(data.size() == 6) {

        resultCommand = *data.at(4).first;

        SetDoubleUser(m_stg.pthUser, m_stg.pthTUser, data.at(5).first);

        resVal = CFile::OpenReadFile(m_stg.pthUser, m_stg.bufferRead, m_stg.sizeRead);
        if(resVal == FileResult::Success) {

            if(m_stg.bufferRead[0] == 'F' && m_stg.bufferRead[1] == 'F' && m_stg.bufferRead[2] == 'F' &&
                m_stg.bufferRead[3] == 'F') {

                uint8_t* pFile = &m_stg.bufferRead[0];
                int size = m_stg.sizeRead;

                if(resultCommand == 1) {
                    m_stg.bufferRead[0] = 'A';
                    m_stg.bufferRead[1] = 'A';
                    m_stg.bufferRead[2] = 'A';
                    m_stg.bufferRead[3] = 'A';
                } else if(resultCommand == 0) {
                    pFile = &m_stg.bufferRead[5];
                    size = m_stg.sizeRead - 5;
                }

                resVal = CFile::CreateWriteFile(m_stg.pthTUser, pFile, size);
                if(resVal == FileResult::Success) {

                    resVal = CFile::RemoveFile(m_stg.pthUser);
                    if(resVal == FileResult::Success) {

                        resVal = CFile::RenameFile(m_stg.pthTUser, m_stg.pthUser);
                        if(resVal == FileResult::Success) {
                            *m_stg.a_resultServer = SERVER_SUCCESS;
                            *m_stg.a_resultCommand = COMMAND_SUCCESS;
                        }
                    }
                }
            }
        }
    }

    SEPARATE_END(m_stg.answer);

    return false;
}

bool CModerator::CheckNewAccount(const TParseData& data)
{
    FileResult resVal = FileResult::Success;
    const char* name = nullptr;

    DIR* dir = CDir::OpenDir(m_stg.pthUser);
    if(dir != nullptr) {

        try {

            while((name = CDir::GetNameDir(dir)) != nullptr) {

                SetDoubleUser(m_stg.pthUser, m_stg.pthTUser, (uint8_t*)name);
                SetUser(m_stg.pthUserPhoto, (uint8_t*)name);

                resVal = CFile::OpenReadFile(m_stg.pthUser, m_stg.bufferRead, m_stg.sizeRead);
                if(resVal == FileResult::Success) {

                    if(m_stg.bufferRead[0] == 'F' && m_stg.bufferRead[1] == 'F' && m_stg.bufferRead[2] == 'F' &&
                        m_stg.bufferRead[3] == 'F') {

                        /*m_stg.bufferRead[1] = 'A';
                        m_stg.bufferRead[3] = 'A';

                        resVal = CFile::RenameFile(m_stg.pthUser, m_stg.pthTUser);
                        if(resVal == FileResult::Success) {
                                resVal = CFile::CreateWriteFile(m_stg.pthUser, m_stg.bufferRead, m_stg.sizeRead);
                                if(resVal == FileResult::Success) {
                                        resVal = CFile::RemoveFile(m_stg.pthTUser);
                                        if(resVal != FileResult::Success) {
                                                throw 1;
                                        }
                                } else
                                        throw 1;
                        } else
                                throw 1;*/

                        while(m_stg.countRead < m_stg.sizeRead) {

                            if(m_stg.bufferRead[m_stg.countRead] == 'A' &&
                                m_stg.bufferRead[m_stg.countRead + 1] == '|') {

                                m_stg.countRead++;
                                SEPARATE_BUNDLE_COUNT(m_stg.answer, m_stg.a_numberParams, m_stg.a_sizeBundle);
                                SEPARATE_ITEM_COUNT(m_stg.answer, m_stg.a_numberParams);

                                memcpy(m_stg.answer, name, SIZE_GUID);
                                m_stg.answer += SIZE_GUID;

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

                            if(m_stg.bufferRead[m_stg.countRead] == 'F' &&
                                m_stg.bufferRead[m_stg.countRead + 1] == '|') {

                                int countPhoto = 19;
                                while(m_stg.bufferRead[m_stg.countRead] != '|' ||
                                    m_stg.bufferRead[m_stg.countRead + 1] != '$') {

                                    m_stg.countRead++;

                                    if(m_stg.bufferRead[m_stg.countRead] != '|')
                                        m_stg.pthUserPhoto[countPhoto++] = m_stg.bufferRead[m_stg.countRead];
                                }

                                resVal = CFile::OpenReadFile(m_stg.pthUserPhoto, m_stg.answer, m_stg.sizeUserPhoto);
                                if(resVal == FileResult::Success) {
                                    m_stg.answer += m_stg.sizeUserPhoto;
                                    SEPARATE_ITEM_COUNT(m_stg.answer, m_stg.a_numberParams);
                                    SEPARATE_ITEM_COUNT(m_stg.answer, m_stg.a_numberParams);
                                }
                            }

                            m_stg.countRead++;
                        }
                        break;
                    }
                }
            }

            DirResult resValDir = CDir::CloseDir(dir);
            if(resValDir == DirResult::Success) {
                *m_stg.a_sizeBundle = 2;
                *m_stg.a_resultServer = SERVER_SUCCESS;
            }

        } catch(int error) {
        }
    }

    SEPARATE_END(m_stg.answer);
    return true;
}
