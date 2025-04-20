#pragma once

#include <vector>
#include <string>
#include <unordered_map>

#include <json/json.h>
#include <sqlite3.h>

#include <http.h>

#include "crypto.h"
#include "query.h"

class CCore
{
private:
    std::vector<std::string> m_message_separate;
    std::vector<std::string> m_command_separate;

    CQuery m_query;
    CCrypto m_crypto;
    sqlite3* m_database;
    Json::StreamWriterBuilder m_builder;

    const char* parse(const char* data_recv, const int& size_recv);

    std::unordered_map<std::string, std::string> parseArguments(const char* arguments);

    int checkAuthorization(char* data_content, int& length_content, Json::Value& json_data);

    ResponseCode Paying(const char* content, char*& data_content, int& size_content);

    ResponseCode Booking(const Json::Value& userJson, const Json::Value& houseJson, char*& data_content, int& size_content);

    ResponseCode GenerateJson(const char* arguments, char*& data_content, int& size_content);

    ResponseCode OpenReadFile(const char* path, char*& data_content, int& size_content);

    ResponseCode LogIn(const char* arguments, char*& data_content, int& size_content);

    ResponseCode SignUp(const char* arguments, char*& data_content, int& size_content);

    int GetQuery(const char* query, Json::Value& json_data);

public:
    CCore();
    ~CCore();

    int dataProcessing(const char* data_recv, const int& size_recv, char* data_http, int& size_http, char* data_content, int& size_content);

    int connectDatabase(const char* path);
    std::string selectDatabase(const char* request);
    int parseMessage(const char* message);

    void enableBooking();
};