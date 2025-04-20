#include "core.h"

#include <cstring>
#include <iostream>
#include <fstream>

#include <win/tools.h>

// https://127.0.0.1/json?table=houses&item=*
// curl -d "table=houses&item=*" -X GET --verbose https://127.0.0.1/json

CCore::CCore()
{
    m_database = nullptr;
    m_builder["emitUTF8"] = true;
}

CCore::~CCore()
{
    if(m_database != nullptr)
        sqlite3_close(m_database);
}

const char* CCore::parse(const char* data_recv, const int& size_recv)
{
    m_message_separate.clear();
    m_command_separate.clear();

    char ch = 0;
    std::string value;
    for(int i = 0; i < size_recv; i++) {

        ch = data_recv[i];

        if(ch == '\r' || ch == '\n') {
            if(value.empty() == false) {
                m_message_separate.push_back(value);
                value.clear();

                if(m_message_separate.size() == 1) {

                    for(auto& p : m_message_separate.back()) {

                        if(p == ' ') {
                            if(value.empty() == false) {
                                m_command_separate.push_back(value);
                                value.clear();
                            }
                        } else
                            value.push_back(p);
                    }
                    m_command_separate.push_back(value);
                }
            }
        } else
            value.push_back(ch);
    }
    m_message_separate.push_back(value);

    return m_message_separate.back().c_str();
}

std::unordered_map<std::string, std::string> CCore::parseArguments(const char* arguments)
{
    std::string param, value;
    std::stringstream ss(arguments);
    std::unordered_map<std::string, std::string> queryParamsMap;

    while(std::getline(ss, param, '=') && std::getline(ss, value, '&')) {
        queryParamsMap[param] = value;
    }
    queryParamsMap[param] = value;

    return queryParamsMap;
}

int CCore::checkAuthorization(char* data_content, int& length_content, Json::Value& json_data)
{
    try {

        for(auto& p : m_message_separate) {
            std::string::size_type position = p.find("Authorization: Bearer ");
            if(position != std::string::npos) {
                std::string payload = m_crypto.getPayload(&p.at(22));
                if(payload.empty() == false) {

                    Json::Reader reader;

                    if(reader.parse(payload, json_data) == true) {

                        std::string time = json_data[0]["time"].asString();
                        // std::string guid = json_data[0]["guid"].asString();
                        // std::string login = json_data[0]["login"].asString();

                        if(m_crypto.validTime(time.c_str()) == 0) {
                            return 0;
                        } else {
                            throw "The access token has expired\r\nPlease obtain a new access token";
                        }
                    }
                }
            }
        }

        throw "Invalid access token\r\nPlease provide a valid access token";

    } catch(const char* error) {

        int size_json = 0;
        std::string json_result;
        Json::Value json_error;

        json_data.clear();
        json_error["error"] = error;
        json_data.append(json_error);

        json_result = Json::writeString(m_builder, json_data);
        size_json = json_result.size();
        if(size_json < length_content) {
            length_content = size_json;
            memcpy(data_content, json_result.c_str(), length_content);
        }
    }

    return 1;
}

int CCore::dataProcessing(const char* data_recv, const int& size_recv, char* data_http, int& size_http, char* data_content, int& length_content)
{
    ResponseCode responseCode = ResponseCode::internal_server_error;
    const char* request = nullptr;
    const char* command = nullptr;
    const char* version = nullptr;
    const char* content = nullptr;

    CHTTP http;

    http.setStatusCode(ResponseCode::internal_server_error);
    // http.setContentType(ContentType::text_plain);
    http.setContentTransferEncoding(ContentTransferEncoding::binary);
    http.setContentLength(0);
    http.setCharset(Charset::utf_8);
    http.setAccessControlAllowMethods("GET, POST, PUT, DELETE");
    // http.setAccessControlAllowOrigin("https://127.0.0.1, https://rbkdev.ru");
    http.setAccessControlAllowOrigin("*");

    content = parse(data_recv, size_recv);

    if(m_command_separate.size() == 3) {

        request = m_command_separate.at(0).c_str();
        command = m_command_separate.at(1).c_str();
        version = m_command_separate.at(2).c_str();

        if (memcmp(version, "HTTP/1.1", 8) != 0) {
            length_content = 0;
            responseCode = ResponseCode::http_version_not_supported;
        } else if (memcmp(request, "GET", 3) == 0) {

            if (memcmp(command, "/datebooking?", 13) == 0) {

                responseCode = GenerateJson(command + 13, data_content, length_content);
            } else if (memcmp(command, "/account", 7) == 0) {

                Json::Value json_data = Json::arrayValue;

                if(checkAuthorization(data_content, length_content, json_data) == 0) {
                    std::string arguments = "instruction=wXf5rKs8E2pJq9d7&table=users&id=" + json_data[0]["id"].asString() + "&login=" + json_data[0]["login"].asString();

                    responseCode = GenerateJson(arguments.c_str(), data_content, length_content);
                } else {
                    http.setContentType(ContentType::application_json);
                    responseCode = ResponseCode::unauthorized;
                }
            } else if (memcmp(command, "/login?", 7) == 0) {
                http.setContentType(ContentType::application_json);

                responseCode = LogIn(command + 7, data_content, length_content);
            } else if (memcmp(command, "/json?", 6) == 0) {
                http.setContentType(ContentType::application_json);

                responseCode = GenerateJson(command + 6, data_content, length_content);
            } else if (memcmp(command, "/css/", 5) == 0) {
                http.setContentType(ContentType::text_css);

                responseCode = OpenReadFile(command + 1, data_content, length_content);
            } else if (memcmp(command, "/script/", 8) == 0) {
                http.setContentType(ContentType::application_javascript);

                responseCode = OpenReadFile(command + 1, data_content, length_content);
            } else if (memcmp(command, "/image/", 7) == 0) {
                http.setContentType(ContentType::image_jpeg);

                responseCode = OpenReadFile(command + 1, data_content, length_content);
            } else if (memcmp(command, "/web/", 5) == 0) {
                http.setContentType(ContentType::text_html);

                responseCode = OpenReadFile(command + 1, data_content, length_content);
            } else if (memcmp(command, "/geo?", 5) == 0) {

                responseCode = GenerateJson(command + 5, data_content, length_content);
            } else if (memcmp(command, "/", 1) == 0) {
                http.setContentType(ContentType::text_html);

                responseCode = OpenReadFile("web/main.html", data_content, length_content);
            } else {
                length_content = 0;
                responseCode = ResponseCode::bad_request;
            }

        } else if (memcmp(request, "POST", 4) == 0) {

            if(memcmp(command, "/booking", 8) == 0) {

                Json::Value userJson;
                Json::Value houseJson;

                if(checkAuthorization(data_content, length_content, userJson) == 0) {
                    // std::string arguments = "instruction=wXf5rKs8E2pJq9d7&table=users&id=" + json_data[0]["id"].asString() + "&login=" + json_data[0]["login"].asString();

                    Json::Reader reader;
                    reader.parse(content, houseJson);

                    responseCode = Booking(userJson, houseJson, data_content, length_content);
                } else {
                    http.setContentType(ContentType::application_json);
                    responseCode = ResponseCode::unauthorized;
                }
            } else if(memcmp(command, "/signup", 7) == 0) {

                responseCode = SignUp(content, data_content, length_content);

            } else if(memcmp(command, "/paying", 7) == 0) {

                Json::Value userJson;
                Json::Value dataJson;

                if(checkAuthorization(data_content, length_content, userJson) == 0) {

                    responseCode = Paying(content, data_content, length_content);
                }else {
                    http.setContentType(ContentType::application_json);
                    responseCode = ResponseCode::unauthorized;
                }

            } else {
                length_content = 0;
                responseCode = ResponseCode::bad_request;
            }

        } else {
            length_content = 0;
            responseCode = ResponseCode::bad_request;
        }
    }

    http.setStatusCode(responseCode);
    http.setContentLength(length_content);
    size_http = http.makeHeader(data_http);

    std::ofstream test("message_send.txt");
    data_http[size_http] = '\0';
    data_content[length_content] = '\0';
    test << data_http << data_content << std::endl;
    test.close();

    return 0;
}

int CCore::GetQuery(const char* query, Json::Value& json_data)
{
    int result = 0;
    Json::Value json_error;
    char* errorMsg = nullptr;

    auto callback = [](void* data, int argc, char **argv, char **azColName) -> int {

        Json::Value* json = reinterpret_cast<Json::Value*>(data);
        Json::Value value;

        for(int i = 0; i < argc; i++)
            value[azColName[i]] = argv[i] ? argv[i] : "NULL";

        json->append(value);
        return 0;
    };

    result = sqlite3_exec(m_database, query, callback, &json_data, &errorMsg);
    if(result != SQLITE_OK) {
        // fprintf(stderr, "SQL error: %s\n", errorMsg);

        json_error["error"] = std::string("Database: ") + errorMsg;

        json_data.clear();
        json_data.append(json_error);

        sqlite3_free(errorMsg);
    }

    return result;
}

ResponseCode CCore::Paying(const char* content, char*& data_content, int& size_content)
{
    ResponseCode responseCode = ResponseCode::internal_server_error;
    Json::Value dataJson;
    Json::Value errorJson;
    Json::Reader reader;
    std::string json_result;
    int size_json = 0;

    try {

        if(reader.parse(content, dataJson) == true) {

            std::string idempotence_key = dataJson[0]["idempotence_key"].asString();

            std::string query = "UPDATE booking SET status = 'paying' WHERE idempotence = '" + idempotence_key + "'";
            if(GetQuery(query.c_str(), dataJson) == 0) {

                std::string result = Json::writeString(m_builder, dataJson);

                responseCode = ResponseCode::ok;
            }
        }

    } catch(const std::exception& e) {
        errorJson["error"] = std::string("Exception: ") + e.what();
        dataJson.clear();
        dataJson.append(errorJson);
    } catch(...) {
        errorJson["error"] = std::string("Exception: ...");
        dataJson.clear();
        dataJson.append(errorJson);
    }

    json_result = Json::writeString(m_builder, dataJson);
    size_json = json_result.size();
    if(size_json < size_content) {
        size_content = size_json;
        memcpy(data_content, json_result.c_str(), size_content);
    }

    return responseCode;
}

ResponseCode CCore::Booking(const Json::Value& userJson, const Json::Value& houseJson, char*& data_content, int& size_content)
{
    ResponseCode responseCode = ResponseCode::internal_server_error;
    std::string query;
    Json::Value dataJson(Json::arrayValue);
    Json::Value itemJson;

    int size_json = 0;
    std::string json_result;

    std::string token;

    std::string status = "booking";
    std::string datetime = getTimeString();
    std::string idempotence_key = getGuid();
    std::string from = houseJson[0]["from"].asString();
    std::string to = houseJson[0]["to"].asString();
    std::string user = userJson[0]["id"].asString();
    std::string house = houseJson[0]["id"].asString();
    std::string password = getGuid();
    std::string from_format = getTimeFormat(houseJson[0]["from"].asString().c_str());
    std::string to_format = getTimeFormat(houseJson[0]["to"].asString().c_str());
    std::string price = houseJson[0]["price"].asString();
    std::string description = houseJson[0]["description"].asString();
    std::string content = R"({"amount":{"value":")" + price + R"(","currency":"RUB"},"confirmation":{"type":"embedded"},"capture":"True","description":")" + description + "\"}";

    if(m_query.postYaPay(idempotence_key, content, token) == 0) {

        if(toLong(to.c_str()) > toLong(from.c_str())) {
            // INSERT INTO users (name, email, age) VALUES ('John Smith', 'john.smith@example.com', 30);
            query = "INSERT INTO booking (status, datetime, idempotence, begin, end, user, house, password, confirmation_token) VALUES ('" + status + "', " + datetime + ", '" + idempotence_key + "', " + from + ", " + to + ", " + user + ", " + house + ", '" + password + "', '" + token + "')";
            if(GetQuery(query.c_str(), dataJson) == 0) {

                itemJson["confirmation_token"] = token;
                itemJson["idempotence_key"] = idempotence_key;

                dataJson.append(itemJson);

                responseCode = ResponseCode::ok;
            }
        }
    }

    json_result = Json::writeString(m_builder, dataJson);
    size_json = json_result.size();
    if(size_json < size_content) {
        size_content = size_json;
        memcpy(data_content, json_result.c_str(), size_content);
    }

    return responseCode;
}

ResponseCode CCore::SignUp(const char* arguments, char*& data_content, int& size_content)
{
    ResponseCode responseCode = ResponseCode::internal_server_error;
    size_t size_json = 0;
    Json::Value json_error;
    Json::Value json_data(Json::arrayValue);
    std::string json_result;
    std::string query;

    try {

        auto queryParamsMap = parseArguments(arguments);

        query = "SELECT login FROM users WHERE login = '" + queryParamsMap["login"] + "'";

        if(GetQuery(query.c_str(), json_data) == 0) {

            if(json_data.empty() == true) {

                query = "INSERT INTO users (data, guid, login, password, client, owner, cleaner, master) VALUES ('" + getTimeFormatMS() + "', '" + getGuid() + "', '" + queryParamsMap.at("login") + "', '" + queryParamsMap.at("password") + "', 1, 0, 0, 0)";
                if(GetQuery(query.c_str(), json_data) == 0)
                    responseCode = ResponseCode::ok;

            } else {
                json_error["error"] = "User with this login already exists";
                json_data.clear();
                json_data.append(json_error);

                responseCode = ResponseCode::conflict;
            }
        }

    } catch(const std::exception& e) {
        json_error["error"] = std::string("Exception: ") + e.what();
        json_data.clear();
        json_data.append(json_error);
    } catch(...) {
        json_error["error"] = std::string("Exception: ...");
        json_data.clear();
        json_data.append(json_error);
    }

    json_result = Json::writeString(m_builder, json_data);
    size_json = json_result.size();
    if(size_json < size_content) {
        size_content = size_json;
        memcpy(data_content, json_result.c_str(), size_content);
    }

    return responseCode;
}

ResponseCode CCore::LogIn(const char* arguments, char*& data_content, int& size_content)
{
    ResponseCode responseCode = ResponseCode::internal_server_error;
    size_t size_json = 0;
    Json::Value json_error;
    Json::Value json_data(Json::arrayValue);
    std::string json_result;

    try {

        auto queryParamsMap = parseArguments(arguments);

        std::string query = "SELECT id, password, login FROM users WHERE login = '" + queryParamsMap.at("name") + "'";

        if(GetQuery(query.c_str(), json_data) == 0) {

            if(json_data.empty() == false) {
                Json::Value& json_item = json_data[0];
                if(json_item.empty() == false) {

                    std::string password = json_item["password"].asString();
                    if(queryParamsMap.at("password") == password) {
                        json_item.removeMember("password");

                        json_item["time"] = getTimeString(10800000);

                        json_result = Json::writeString(m_builder, json_data);

                        json_item.clear();

                        json_item["token"] = m_crypto.create(json_result.c_str(), json_result.size());;

                        responseCode = ResponseCode::ok;

                    } else {
                        json_error["error"] = "Password wrong";
                        json_data.clear();
                        json_data.append(json_error);

                        responseCode = ResponseCode::unauthorized;
                    }

                } else {
                    json_error["error"] = "User with this login not exists";
                    json_data.clear();
                    json_data.append(json_error);

                    responseCode = ResponseCode::unauthorized;
                }

            } else {
                json_error["error"] = "User with this login not exists";
                json_data.clear();
                json_data.append(json_error);

                responseCode = ResponseCode::unauthorized;
            }
        }

    } catch(const std::exception& e) {
        json_error["error"] = std::string("Exception: ") + e.what();
        json_data.clear();
        json_data.append(json_error);
    } catch(...) {
        json_error["error"] = std::string("Exception: ...");
        json_data.clear();
        json_data.append(json_error);
    }

    json_result = Json::writeString(m_builder, json_data);
    size_json = json_result.size();
    if(size_json < size_content) {
        size_content = size_json;
        memcpy(data_content, json_result.c_str(), size_content);
    }

    return responseCode;
}

ResponseCode CCore::OpenReadFile(const char* path, char*& data_content, int& size_content)
{
    ResponseCode responseCode = ResponseCode::internal_server_error;

    FILE* file = fopen(path, "rb");
    if(file != nullptr) {

        // fseek(file, 0, SEEK_END);
        // size_file = ftell(file);
        // fseek(file, 0, SEEK_SET);

        size_content = fread(data_content, sizeof(char), size_content, file);
        if(size_content != 0)
            responseCode = ResponseCode::ok;

        fclose(file);

    } else {
        size_content = 0;
        responseCode = ResponseCode::not_found;
    }

    return responseCode;
}

ResponseCode CCore::GenerateJson(const char* arguments, char*& data_content, int& size_content)
{
    ResponseCode responseCode = ResponseCode::internal_server_error;
    size_t size_json = 0;
    std::string query;
    std::string json_result;
    const char* instruction;

    Json::Value json_error;
    Json::Value json_data(Json::arrayValue);

    try {

        auto queryParamsMap = parseArguments(arguments);

        instruction = queryParamsMap.at("instruction").c_str();

        if(memcmp(instruction, "e4N8UyM7CzHg5D14", 16) == 0) {
            // json?instruction=e4N8UyM7CzHg5D14&table=houses&item=date&offset=0
            // SELECT * FROM houses ORDER BY date DESC LIMIT 1 OFFSET 0
            query = "SELECT * FROM " + queryParamsMap.at("table") + " ORDER BY " + queryParamsMap.at("item") + " DESC LIMIT 1 OFFSET " + queryParamsMap.at("offset");
        } else if(memcmp(instruction, "J9f6KtR7WpLqX2f5", 16) == 0) {
            // json?instruction=J9f6KtR7WpLqX2f5&table=houses&item=date&date=2023-05-30&time=12:00:00
            // SELECT * FROM houses WHERE created_at < '2023-05-30 12:00:00' ORDER BY created_at DESC LIMIT 1
            query = "SELECT * FROM " + queryParamsMap.at("table") + " WHERE " + queryParamsMap.at("item") + " < '" + queryParamsMap.at("date") + " " + queryParamsMap.at("time") + "' ORDER BY " + queryParamsMap.at("item") + " DESC LIMIT 1";
        } else if(memcmp(instruction, "B2cE4dF6gH8iJ0K4", 16) == 0) {
            // instruction=B2cE4dF6gH8iJ0K4&login=login&password=password
            // INSERT INTO users (name, email, age) VALUES ('John Doe', 'johndoe@example.com', 25)
            query = "INSERT INTO users (data, guid, login, password) VALUES ('" + getTimeFormatMS() + "', '" + getGuid() + "', '" + queryParamsMap.at("login") + "', '" + queryParamsMap.at("password") + "')";
        } else if(memcmp(instruction, "L5mN7oP9qR1sT3Ug", 16) == 0) {
            // json?instruction=L5mN7oP9qR1sT3Ug&table=houses&item=preview
            // SELECT * FROM houses
            // SELECT street FROM houses
            query = "SELECT " + queryParamsMap.at("item") + " FROM " + queryParamsMap.at("table");
        } else if(memcmp(instruction, "wXf5rKs8E2pJq9d7", 16) == 0) {
            // instruction=wXf5rKs8E2pJq9d7&table=users&item=preview
            // SELECT * FROM users WHERE id = 123 AND login = 'John';
            query = "SELECT * FROM " + queryParamsMap.at("table") + " WHERE id = " + queryParamsMap.at("id") + " AND login = '" + queryParamsMap.at("login") + "'";
        } else if(memcmp(instruction, "R2f5G8hLj7K9tPqN", 16) == 0) {
            // instruction=R2f5G8hLj7K9tPqN&table=houses&id=id
            // SELECT * FROM houses WHERE id = <значение id>;
            query = "SELECT * FROM " + queryParamsMap.at("table") + " WHERE id = " + queryParamsMap.at("id");
        } else if(memcmp(instruction, "HhRrK6d7f8gD1J2L", 16) == 0) {
            // instruction=HhRrK6d7f8gD1J2L&id=id
            // SELECT * FROM table_name WHERE column1 = 'value1' AND column2 != 'value2';
            query = "SELECT * FROM booking WHERE house = " + queryParamsMap.at("id") + " AND status != 'enable'";
        } else if(memcmp(instruction, "7f2k8e3z6d9x1h5m", 16) == 0) {
            // instruction=7f2k8e3z6d9x1h5m
        }

        if(query.empty() == false) {
            if(GetQuery(query.c_str(), json_data) == 0)
                responseCode = ResponseCode::ok;
        } else
            responseCode = ResponseCode::bad_request;

    } catch(const std::exception& e) {
        json_error["error"] = std::string("Exception: ") + e.what();
        json_data.clear();
        json_data.append(json_error);
    } catch(...) {
        json_error["error"] = std::string("Exception: ...");
        json_data.clear();
        json_data.append(json_error);
    }

    json_result = Json::writeString(m_builder, json_data);
    size_json = json_result.size();
    if(size_json < size_content) {
        size_content = size_json;
        memcpy(data_content, json_result.c_str(), size_content);
    }

    return responseCode;
}

int CCore::connectDatabase(const char* path)
{
    std::cout << "open database...";
    int result = sqlite3_open(path, &m_database);
    if(result != 0) {
        std::cout << "\terror\t" << sqlite3_errmsg(m_database) << std::endl;
        return 1;
    }

    std::cout << "\tsuccess" << std::endl;
    return 0;
}

std::string CCore::selectDatabase(const char* request)
{
    int result = 0;
    unsigned size_send = 0;
    char* errorMsg = nullptr;

    Json::Value json_data(Json::arrayValue);

    result = sqlite3_exec(m_database, request,
                          [](void* data, int argc, char **argv, char **azColName) -> int {

                              Json::Value* json = reinterpret_cast<Json::Value*>(data);
                              Json::Value value;

                              for(int i = 0; i < argc; i++)
                                  value[azColName[i]] = argv[i] ? argv[i] : "NULL";

                              json->append(value);
                              return 0;
                          }
            , &json_data, &errorMsg);

    if(result != SQLITE_OK) {
        fprintf(stderr, "SQL error: %s\n", errorMsg);
        sqlite3_free(errorMsg);
        return "";
    } else {
        fprintf(stdout, "Operation done successfully\n");
    }

    return Json::writeString(m_builder, json_data);
}

int CCore::parseMessage(const char* message)
{
    Json::Reader reader;
    Json::Value value_jj = Json::arrayValue;
    std::string text = "{\"database\":{\"ip\":\"127.0.0.1\",\"name\":\"base\",\"user\":\"userbase\",\"pass\":\"2222\"},"
                       "\"log\":{\"dir\":\"logs\",\"remote\":\"/var/www/html/base/logs/\"}}";

    if(!reader.parse(message, value_jj))
        return 1;

    std::string ip_db = value_jj["database"]["ip"].asString();
    std::string name_db = value_jj["database"]["name"].asString();
    std::string user_db = value_jj["database"]["user"].asString();
    std::string pass_db = value_jj["database"]["pass"].asString();
    std::string dir_log = value_jj["log"]["dir"].asString();
    std::string dir_log_remote = value_jj["log"]["remote"].asString();

    return 0;
}

void CCore::enableBooking()
{
    Json::Value dataJson;
    std::string time = getTimeString();
    std::string query = "UPDATE booking SET status = 'enable' WHERE status == 'booking' AND datetime + 900000 < " + time;

    GetQuery(query.c_str(), dataJson);

    std::string result = Json::writeString(m_builder, dataJson);
}