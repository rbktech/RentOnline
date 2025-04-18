#ifndef CKNUCKLE_H
#define CKNUCKLE_H

#include <list>
#include <map>
#include <functional>

#include "reform.h"
#include "list_id.h"

// ------------------------------------------------------------------------

class CMessage
{

public:
	uint8_t count = 0;
	const uint8_t number = 0;
	uint16_t size = 0;
	std::list<CMessagePair> list;

	CMessage(const TParsePack& pack)
		: number(pack.m_number)
	{
		count = pack.m_count;
		size = pack.m_message.size;
		list.push_back(pack.m_message);
	}

	~CMessage() {};

	void Clear()
	{
		count = size = 999;
		for(auto& p : list) {
			p.size = 999;
			if(p.value != nullptr) {
				delete[] p.value;
				p.value = nullptr;
			}
		}
		list.clear();
	}
};

// ------------------------------------------------------------------------

typedef void (*pFunc)(TParsePack& pack, const uint8_t* in, const int& size);

typedef std::function<void(const uint8_t* message, const int& size)> SendFunc;

class CMarkPair
{
public:
	const uint8_t* first;
	pFunc second;

	CMarkPair(const uint8_t* mark, pFunc func)
		: first(mark)
		, second(func)
	{
	}

	~CMarkPair() {};
};

// ------------------------------------------------------------------------

class CKnuckle
{
private:
	CMarkPair** m_markParse;
	CMarkPair** m_markCheck;

	std::map<int, CMessage> m_commands;

	int m_countParse, m_countCheck;

	int m_sizeBuffer;
	uint8_t* m_buffer;

	bool CheckMessage(uint8_t* buffer, const int& sizeBuffer, int& index, int& size);
	int ParseMessage(uint8_t* in, const int& size, TParsePack& pack);

	void SeparateList(const std::list<CMessagePair> in, std::list<CMessagePair>& out);

	// ----------------------

	void CreateMessage(uint8_t* in, const int& sizeIn, uint8_t*& commandMessage, int& sizeCommand);

public:
	CKnuckle();
	~CKnuckle();

	void CollectMessage(const TParsePack& in, uint8_t* bufferSend, int& count);

	void FillingBuffer(uint8_t* recvArray, const int& sizeRecv, uint8_t*& commandMessage, int& sizeCommand);

	void SendData(uint8_t* sendArray, const int& sizeSend, uint8_t id, SendFunc func);
};

#endif // CKNUCKLE_H
