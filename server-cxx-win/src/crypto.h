#pragma once

#include <iostream>
#include <string>
#include <openssl/evp.h>
#include <openssl/hmac.h>
#include <openssl/bio.h>
#include <openssl/buffer.h>


#include <iostream>
#include <string>
#include <openssl/evp.h>
#include <openssl/hmac.h>
#include <openssl/bio.h>
#include <openssl/buffer.h>

class CCrypto
{
private:
    std::string m_secret_key;

public:
    CCrypto();
    ~CCrypto();

    std::string base64_encode(const unsigned char* input, int length);
    std::string create(const char* data_payload, const int& size_payload);
    std::string base64_decode(const std::string& input);
    std::string getPayload(const std::string& jwt_token);

    int validTime(const char* value);
};