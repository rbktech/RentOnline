#pragma once

#include <string>

#include <json/json.h>

class CQuery
{
private:
    static size_t WriteCallback(char* contents, size_t size, size_t nmemb, void* userp);

    void roundJson(const Json::Value& dataJson, std::vector<std::map<std::string, std::string>>& params);

public:
    CQuery();
    ~CQuery();

    int postYaPay(const std::string& idempotenceKey, const std::string& content, std::string& token);
};