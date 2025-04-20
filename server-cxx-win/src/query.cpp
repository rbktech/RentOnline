#include "query.h"

#include <curl/curl.h>

#define AUTHORIZATION "yandex_money_authorization.txt"

CQuery::CQuery()
{
}

CQuery::~CQuery()
{
}

void CQuery::roundJson(const Json::Value& dataJson, std::vector<std::map<std::string, std::string>>& params)
{
    unsigned int sizeJson = 0;

    if(dataJson.isObject() == true) {

        auto members = dataJson.getMemberNames();
        for(auto& p : members) {
            roundJson(dataJson[p], params);

            if(dataJson[p].isObject() != true && dataJson[p].isArray() != true) {

                if(params.empty() == true)
                    params.emplace_back();

                params.back().insert({ p, dataJson[p].asString()});
            }
        }

    } else if(dataJson.isArray() == true) {

        sizeJson = dataJson.size();
        for(unsigned int i = 0; i < sizeJson; i++) {

            params.emplace_back();

            roundJson(dataJson[i], params);
        }
    }
}

size_t CQuery::WriteCallback(char* contents, size_t size, size_t nmemb, void* userp)
{
    ((std::string*)userp)->append((char*)contents, size * nmemb);
    return size * nmemb;
}

int CQuery::postYaPay(const std::string& idempotenceKey, const std::string& content, std::string& token)
{
    CURL *curl;
    CURLcode res;
    std::string readBuffer;

    curl_global_init(CURL_GLOBAL_ALL);

    curl = curl_easy_init();
    if(curl) {

        curl_easy_setopt(curl, CURLOPT_URL, "https://api.yookassa.ru/v3/payments");

        struct curl_slist* header = nullptr;
        header = curl_slist_append(header, "Content-Type: application/json");
        header = curl_slist_append(header, "Authorization: Basic " AUTHORIZATION);
        header = curl_slist_append(header, "YM-User-Agent: Windows/10 Python/3.11.4 YooKassa.Python/2.3.6");
        header = curl_slist_append(header, ("Idempotence-Key: " + idempotenceKey).c_str());
        curl_easy_setopt(curl, CURLOPT_HTTPHEADER, header);
        curl_easy_setopt(curl, CURLOPT_POSTFIELDS, content.c_str());
        curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, 0);
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, WriteCallback);
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, &readBuffer);

        res = curl_easy_perform(curl);
        if(res != CURLE_OK)
            fprintf(stderr, "curl_easy_perform() failed: %s\n", curl_easy_strerror(res));

        curl_easy_cleanup(curl);
    }

    curl_global_cleanup();

    Json::Value dataJson;
    Json::Reader reader;
    std::vector<std::map<std::string, std::string>> params;

    try {

        if(reader.parse(readBuffer, dataJson) == true)
            roundJson(dataJson, params);

        if(params.at(0).at("status") == "pending") {

            token = params.at(0).at("confirmation_token");

            return 0;
        }

    } catch(...) {

    }

    return 1;
}