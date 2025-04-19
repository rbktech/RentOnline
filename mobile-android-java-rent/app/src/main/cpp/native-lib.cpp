#include <jni.h>
#include <string>

#include "knuckle.h"

TParsePack g_pack;
CKnuckle g_knuckle;

extern "C" JNIEXPORT jbyteArray JNICALL
Java_ru_rbkdev_lease_main_CClient_ReceiveData(__unused JNIEnv *env, __unused jclass clazz, jbyteArray array, jint size)
{
    int sizeOut = 0;
    uint8_t* outArray = nullptr;
    uint8_t inArray[SIZE_TRAFFIC + SIZE_SERVICE] = { 0 };

    env->GetByteArrayRegion(array, 0, size, reinterpret_cast<jbyte*>(inArray));

    g_knuckle.FillingBuffer(inArray, size, outArray, sizeOut);
    if(outArray != nullptr) {
        array = env->NewByteArray(sizeOut);
        env->SetByteArrayRegion(array, 0, sizeOut, reinterpret_cast<jbyte*>(outArray));
        return array;
    }

    return nullptr;
}

extern "C" JNIEXPORT void JNICALL
Java_ru_rbkdev_lease_main_CClient_SetPackMessage(__unused JNIEnv *env, __unused jclass clazz, jint id, jint number)
{
    g_pack.m_id = id;
    g_pack.m_number = number;
}

extern "C" JNIEXPORT void JNICALL
Java_ru_rbkdev_lease_main_CClient_ClearPackMessage(__unused JNIEnv *env, __unused jclass clazz)
{
    g_pack.m_id = 0;
    g_pack.m_count = 0;
    g_pack.m_number = 0;
    g_pack.m_message.size = 0;
    if(g_pack.m_message.value != nullptr) {
        delete[] g_pack.m_message.value;
        g_pack.m_message.value = nullptr;
    }
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_ru_rbkdev_lease_main_CClient_PackMessage(JNIEnv *env, __unused jclass clazz, jbyteArray array, jint size)
{
    int count = 0;
    uint8_t buffer[SIZE_TRAFFIC] = { 0 };

    g_pack.m_message.size = size;
    g_pack.m_message.value = new uint8_t[size]{ 0 };

    env->GetByteArrayRegion(array, 0, size, reinterpret_cast<jbyte*>(g_pack.m_message.value));

    g_knuckle.CollectMessage(g_pack, buffer, count);

    array = env->NewByteArray(count);
    env->SetByteArrayRegion(array, 0, count, reinterpret_cast<jbyte*>(buffer));

    g_pack.m_count++;
    delete[] g_pack.m_message.value;
    g_pack.m_message.value = nullptr;
    return array;
}