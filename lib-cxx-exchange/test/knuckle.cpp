#include "gtest/gtest.h"

#include "knuckle.h"

class GTKnuckle : public ::testing::Test
{
protected:
    void SetUp() override {};
    void TearDown() override {};
};

TEST_F(GTKnuckle, DataExchange)
{
    CKnuckle knuckle;
    std::srand(std::time(nullptr));

    int sizeSend(0);
    uint8_t sendArray[SIZE_TRAFFIC] = { 0 }, *pSend(sendArray);

    int sizeRecv(SIZE_TRAFFIC);
    uint8_t recvArray[SIZE_TRAFFIC] = { 0 }, *pRecv(recvArray);

    TParsePack pack;
    std::string message;

    {
        sizeSend = 0;
        memset(sendArray, 0, SIZE_TRAFFIC);

        sizeRecv = SIZE_TRAFFIC;
        memset(recvArray, 0, SIZE_TRAFFIC);

        if(pack.m_count == 3)
            pack.m_count = 0;

        if(pack.m_count == 0) {
            pack.m_id = 20 + rand() % 100;
            pack.m_number = 3;
        }

        message = "this is test message of id: " + std::to_string(pack.m_count);
        pack.m_message.size = message.size();
        pack.m_message.value = (uint8_t*)message.data();

        // knuckle.GetMessage(recvArray, sizeRecv);

        knuckle.CollectMessage(pack, sendArray, sizeSend);
        // knuckle.FillingBuffer(sendArray, sizeSend);

        pack.m_count++;

        /*for(int i = 0; i < sizeSend; i++)
            printf("%c", sendArray[i]);
        printf("\n");

        for(int i = 0; i < sizeRecv; i++)
            printf("%c", recvArray[i]);
        printf("\n");*/

        /*
        knuckle.FillingBuffer((uint8_t*)"recvArray", 9);
        knuckle.FillingBuffer(recvArray, sizeRecv);
        knuckle.FillingBuffer(sendArray, sizeSend);
        knuckle.FillingBuffer((uint8_t*)"dsfdsf", 6);
        */
    }
}