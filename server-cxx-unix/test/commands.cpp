#include "gtest/gtest.h"

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>

#include <assembly.h>
#include <linkLinux.hpp>

#define SIZE_BUFF 1024

class CCommand
{
public:
	void CreateNewUser(uint8_t* message, uint8_t* answer, const size_t& size) {}
	void CreateNewFerm(uint8_t* message, uint8_t* answer, const size_t& size) {}
	void ReadListOwnerFerm(uint8_t* message, uint8_t* answer, const size_t& size) {}
	void ReadListKey(uint8_t* message, uint8_t* answer, const size_t& size) {}
	void ReadFerm(uint8_t* message, uint8_t* answer, const size_t& size) {}
	void PayFerm(uint8_t* message, uint8_t* answer, const size_t& size) {}
	void ReadListFerm(uint8_t* message, uint8_t* answer, const size_t& size) {}
};

class GTCommand : public ::testing::Test
{
protected:
	CCommand commands;

    void SetUp() override {};
    void TearDown() override {};
};

TEST_F(GTCommand, FillingMessage)
{
	const uint8_t* array0 = (uint8_t*)"_F_FAB_13_AFE_\0" ;
	const uint8_t* array1 = (uint8_t*)"_FAB_3asd_AFE_\0" ;
	const uint8_t* array2 = (uint8_t*)"_FAB_41ff9_AFE_\0" ;
	const uint8_t* array3 = (uint8_t*)"_FAB_5eeikl_AFE_\0" ;

	TPart part;

	FillingMessage(array0, strlen((char*)array0), part);
	FillingMessage(array1, strlen((char*)array1), part);
	FillingMessage(array2, strlen((char*)array2), part);
	FillingMessage(array3, strlen((char*)array3), part);
}

TEST_F(GTCommand, CreateNewUser)
{
	uint8_t answer[SIZE_BUFF] = { 0 };
	uint8_t message[SIZE_BUFF] = { 0 };

	std::string str = "|mail|password|\n";
	memcpy(message, &str[0], str.size());

	commands.CreateNewUser(message, answer, str.size());
	for(int i = 0; i < SIZE_BUFF; i++)
		printf("%c", answer[i]);
	printf("\n");
}

TEST_F(GTCommand, CreateNewFerm)
{
	uint8_t answer[SIZE_BUFF] = { 0 };
	uint8_t message[SIZE_BUFF] = { 0 };

	//std::string str = "|U|0123456789|T|111|P|NUMBER_PERSON|L|STREET|HOUSE|RATING|DISTANCE|NUMBER_ROOMS|PRICE|R|OWNER|GEO|DESCRIPTION|I|FERM_IP|F|0|";

	std::string str = "|U|0123456789|T|111|P|3|L|2-ая Краснопресненская|3|3.5|4.5|4|1000|R|Петров Игорь Витальевич|55.7534 66.2375|Великолепное жилье|I|145.99.22.33|F|2|rrrrrrrr2|sssssssssssss3|";
	//std::string str = "|0123456789|222.168.77.25|Степанов Владимир Ильич|Московский проезд|44|55.7534 66.2375|D72A83B48723|ОТличное жилье";
	//std::string str = "|0123456789|217.45.188.96|Петров Петр Сергеевич|Пажарная|12|18.887 135.9962|D72A83B48723|Благоприятное жилье";

	memcpy(message, &str[0], str.size());

	commands.CreateNewFerm(message, answer, str.size());
	for(int i = 0; i < SIZE_BUFF; i++)
		printf("%c", answer[i]);
	printf("\n");
}

TEST_F(GTCommand, ReadListOwnerFerm)
{
	uint8_t answer[SIZE_BUFF] = { 0 };
	uint8_t message[SIZE_BUFF] = { 0 };

	std::string str = "|0123456789";
	memcpy(message, &str[0], str.size());

	commands.ReadListOwnerFerm(message, answer, str.size());
	for(int i = 0; i < SIZE_BUFF; i++)
		printf("%c", answer[i]);
	printf("\n");
}

TEST_F(GTCommand, ReadListKey)
{
		uint8_t answer[SIZE_BUFF] = { 0 };
		uint8_t message[SIZE_BUFF] = { 0 };

		std::string str = "|0123456789";
		memcpy(message, &str[0], str.size());

		commands.ReadListKey(message, answer, str.size());
		for(int i = 0; i < SIZE_BUFF; i++)
			printf("%c", answer[i]);
		printf("\n");
	}

TEST_F(GTCommand, ReadFerm)
{
		uint8_t answer[SIZE_BUFF] = { 0 };
		uint8_t message[SIZE_BUFF] = { 0 };

		std::string str = "|moscow|1603192163|";
		memcpy(message, &str[0], str.size());

		commands.ReadFerm(message, answer, str.size());
		for(int i = 0; i < SIZE_BUFF; i++)
			printf("%c", answer[i]);
		printf("\n");
	}

TEST_F(GTCommand, PayFerm)
{
		uint8_t answer[SIZE_BUFF] = { 0 };
		uint8_t message[SIZE_BUFF] = { 0 };

		std::string str = "|moscow|#|1603192163|160314653|1603195587|\n";
		memcpy(message, &str[0], str.size());

		commands.PayFerm(message, answer, str.size());
		for(int i = 0; i < SIZE_BUFF; i++)
			printf("%c", answer[i]);
		printf("\n");
	}

TEST_F(GTCommand, ReadListFerm)
{
		uint8_t answer[SIZE_BUFF] = { 0 };
		uint8_t message[SIZE_BUFF] = { 0 };

		std::string str = "|T|111|P|3|D|3-12-2019 12:00:00|3-12-2019 12:00:01|\n";
		memcpy(message, &str[0], str.size());

		commands.ReadListFerm(message, answer, str.size());
		for(int i = 0; i < SIZE_BUFF; i++)
			printf("%c", answer[i]);
		printf("\n");
	}

TEST_F(GTCommand, InitClientAndSendGuid)
{
	 uint8_t guid[] = { "2354364560123" };
	 char ip[] = { "192.168.1.72" };
	 CLinkLinux::InitClientAndSendGuid(ip, guid, 10);
}