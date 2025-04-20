#include "crypto.h"

#include <win/tools.h>

#define SECRET_KEY ""

CCrypto::CCrypto()
{
    m_secret_key = SECRET_KEY;
}

CCrypto::~CCrypto()
{
}

std::string CCrypto::base64_encode(const unsigned char* input, int length)
{
    // Создание объекта BIO для кодирования
    BIO *bio, *b64;
    b64 = BIO_new(BIO_f_base64());
    BIO_set_flags(b64, BIO_FLAGS_BASE64_NO_NL);
    bio = BIO_new(BIO_s_mem());
    BIO_push(b64, bio);

    // Кодирование данных
    BIO_write(b64, input, length);
    BIO_flush(b64);

    // Получение зашифрованных данных
    BUF_MEM *bptr;
    BIO_get_mem_ptr(b64, &bptr);

    // Копирование данных в строку и добавление завершающего нуля
    std::string result(bptr->data, bptr->length);
    // result += '\0';

    // Освобождение памяти
    BIO_free_all(b64);

    return result;
}

std::string CCrypto::create(const char* data_payload, const int& size_payload)
{
    // Создание пары ключей
    //std::string secret_key = "my_secret_key";
    unsigned char key[EVP_MAX_KEY_LENGTH] = { 0 };
    int key_length = EVP_BytesToKey(EVP_aes_256_cbc(), EVP_sha256(), nullptr, (const unsigned char*)m_secret_key.c_str(), m_secret_key.length(), 1, key,nullptr);

    // Создание заголовка токена
    std::string header = R"({"alg":"HS256","typ":"JWT"})";
    std::string encoded_header = base64_encode((const unsigned char*)header.c_str(), header.length());

    // Создание полезной нагрузки токена
    std::string encoded_payload = base64_encode((unsigned char*)data_payload, size_payload);

    // Создание заголовка и полезной нагрузки токена
    std::string token = encoded_header + "." + encoded_payload;

    // Создание подписи токена
    unsigned int signature_length = 0;
    unsigned char signature[EVP_MAX_MD_SIZE] = { 0 };

    HMAC(EVP_sha256(), key, key_length, (const unsigned char*)token.c_str(), token.length(), signature, &signature_length);

    // Кодирование подписи токена
    std::string encoded_signature = base64_encode(signature, signature_length);

    // Создание окончательного токена
    std::string jwt_token = token + "." + encoded_signature;

    // Отправка токена клиенту
    return jwt_token;
}

std::string CCrypto::base64_decode(const std::string& input)
{
    // Создание объекта BIO для декодирования
    BIO *bio, *b64;
    b64 = BIO_new(BIO_f_base64());
    BIO_set_flags(b64, BIO_FLAGS_BASE64_NO_NL);
    bio = BIO_new_mem_buf((void*)input.c_str(), input.length());
    bio = BIO_push(b64, bio);

    // Выделение памяти под декодированные данные
    std::string result(input.length(), '\0');

    // Декодирование данных
    int length = BIO_read(bio, (void*)result.data(), result.length());

    // Усечение лишних символов
    result.resize(length);

    // Освобождение памяти
    BIO_free_all(b64);

    return result;
}

std::string CCrypto::getPayload(const std::string& jwt_token)
{
    // Получение токена от клиента

    /*if(jwt_token != m_jwt_token) {
        jwt_token = m_jwt_token;
        printf("");
    }*/

    // Разбиение токена на заголовок, полезную нагрузку и подпись
    std::string::size_type first_dot = jwt_token.find('.');
    std::string::size_type second_dot = jwt_token.find('.', first_dot + 1);
    std::string header = jwt_token.substr(0, first_dot);
    std::string payload = jwt_token.substr(first_dot + 1, second_dot - first_dot - 1);
    std::string signature = jwt_token.substr(second_dot + 1);

    // Декодирование заголовка и полезной нагрузки
    std::string decoded_header = base64_decode(header);
    std::string decoded_payload = base64_decode(payload);

    // Проверка типа токена и используемого алгоритма подписи
    if (decoded_header.find(R"("typ":"JWT")") == std::string::npos || decoded_header.find(R"("alg":"HS256")") == std::string::npos) {
        std::cout << "Invalid token type or signature algorithm" << std::endl;
        return "";
    }

    // Создание пары ключей
    // std::string secret_key = "my_secret_key";
    unsigned char key[EVP_MAX_KEY_LENGTH];
    int key_length = EVP_BytesToKey(EVP_aes_256_cbc(), EVP_sha256(), nullptr, (const unsigned char*)m_secret_key.c_str(), m_secret_key.length(), 1, key,nullptr);

    // Создание заголовка и полезной нагрузки токена
    std::string token = header + "." + payload;

    // Создание подписи токена
    unsigned int signature_length;
    unsigned char signature_calculated[EVP_MAX_MD_SIZE];
    HMAC(EVP_sha256(), key, key_length,(const unsigned char*)token.c_str(), token.length(), signature_calculated, &signature_length);

    // Кодирование подписи токена
    std::string encoded_signature_calculated = base64_encode(signature_calculated, signature_length);

    // Проверка соответствия подписей
    if (encoded_signature_calculated != signature) {
        std::cout << "Invalid token signature" << std::endl;
        return "";
    }

    return decoded_payload;

    // Проверка срока действия токена (если он задан в полезной нагрузке)
    /*std::string::size_type iat_position = decoded_payload.find(R"("time" :)");
    if (iat_position != std::string::npos) {
        int iat = std::stoi(decoded_payload.substr(iat_position + 6));
        int now = time(nullptr);
        if (now < iat) {
            std::cout << "Token has not become valid yet" << std::endl;
            return 1;
        }
    }

    // Проверка успешно завершена
    std::cout << "Token is valid" << std::endl;

    return 0;*/
}

int CCrypto::validTime(const char* value)
{
    try {
        long long iat = std::stoull(value);
        time_t now = time(nullptr) * 1000;
        if(now > iat)
            return 1;

    } catch (...) {
        return 1;
    }

    return 0;
}