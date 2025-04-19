#include "knuckle.h"

#include <ctime>
#include <stdio.h>
#include <string>

#define BYTE2 2

#define MARK_SIZE 5
#define MARK_NUMBERS 8

#define MARK_SSEPARATE "_NWQ_"

#define MARK_SBEGIN "_FAB_"
#define MARK_SSIZE_PACK "_HGD_"
#define MARK_SID_MESSAGE "_EAM_"
#define MARK_SCOUNT_POSITION "_BEC_"
#define MARK_SNUMBERS_POSITION "_DCP_"
#define MARK_SSIZE_MESSAGE "_CBS_"
#define MARK_SSRC "_RTS_"
#define MARK_SEND "_AFE_"

#define MARK_SEPARATE 0x95, 0x78, 0x87, 0x81, 0x95
#define MARK_BEGIN 0x95, 0x70, 0x65, 0x66, 0x95
#define MARK_SIZE_PACK 0x95, 0x72, 0x71, 0x68, 0x95
#define MARK_ID_MESSAGE 0x95, 0x69, 0x65, 0x77, 0x95
#define MARK_COUNT_POSITION 0x95, 0x66, 0x69, 0x67, 0x95
#define MARK_NUMBERS_POSITION 0x95, 0x68, 0x67, 0x80, 0x95
#define MARK_SIZE_MESSAGE 0x95, 0x67, 0x66, 0x83, 0x95
#define MARK_SRC 0x95, 0x82, 0x84, 0x83, 0x95
#define MARK_END 0x95, 0x65, 0x70, 0x69, 0x95

#define MARK_CSEPARATE (uint8_t)'_', (uint8_t)'N', (uint8_t)'W', (uint8_t)'Q', (uint8_t)'_'
#define MARK_CBEGIN (uint8_t)'_', (uint8_t)'F', (uint8_t)'A', (uint8_t)'B', (uint8_t)'_'
#define MARK_CSIZE_PACK (uint8_t)'_', (uint8_t)'H', (uint8_t)'G', (uint8_t)'D', (uint8_t)'_'
#define MARK_CID_MESSAGE (uint8_t)'_', (uint8_t)'E', (uint8_t)'A', (uint8_t)'M', (uint8_t)'_'
#define MARK_CCOUNT_POSITION (uint8_t)'_', (uint8_t)'B', (uint8_t)'E', (uint8_t)'C', (uint8_t)'_'
#define MARK_CNUMBERS_POSITION (uint8_t)'_', (uint8_t)'D', (uint8_t)'C', (uint8_t)'P', (uint8_t)'_'
#define MARK_CSIZE_MESSAGE (uint8_t)'_', (uint8_t)'C', (uint8_t)'B', (uint8_t)'S', (uint8_t)'_'
#define MARK_CSRC (uint8_t)'_', (uint8_t)'R', (uint8_t)'T', (uint8_t)'S', (uint8_t)'_'
#define MARK_CEND (uint8_t)'_', (uint8_t)'A', (uint8_t)'F', (uint8_t)'E', (uint8_t)'_'

CKnuckle::CKnuckle()
	: m_countParse(0)
	, m_countCheck(0)
	, m_sizeBuffer(0)
{
	m_buffer = new uint8_t[SIZE_BUFFER] { 0 };

	m_markParse = new CMarkPair* [5] { nullptr };
	m_markCheck = new CMarkPair* [3] { nullptr };

	int size = MARK_SIZE + 1;

	m_markParse[m_countParse++] = new CMarkPair(new uint8_t[size] { MARK_CID_MESSAGE, 0 }, CReform::SetPackId);
	m_markParse[m_countParse++] = new CMarkPair(new uint8_t[size] { MARK_CCOUNT_POSITION, 0 }, CReform::SetPackCount);
	m_markParse[m_countParse++] = new CMarkPair(new uint8_t[size] { MARK_CNUMBERS_POSITION, 0 }, CReform::SetPackNumber);
	m_markParse[m_countParse++] = new CMarkPair(new uint8_t[size] { MARK_CSIZE_MESSAGE, 0 }, CReform::SetPackSize);
	m_markParse[m_countParse++] = new CMarkPair(new uint8_t[size] { MARK_CEND, 0 }, CReform::SetPackMessage);

	m_markCheck[m_countCheck++] = new CMarkPair(new uint8_t[size] { MARK_CBEGIN, 0 }, nullptr);
	m_markCheck[m_countCheck++] = new CMarkPair(new uint8_t[size] { MARK_CSIZE_PACK, 0 }, nullptr);
	m_markCheck[m_countCheck++] = new CMarkPair(new uint8_t[size] { MARK_CEND, 0 }, nullptr);

	/*uint8_t mark_1[MARK_SIZE + 1] = { MARK_CSEPARATE, 0 };
	uint8_t mark_2[MARK_SIZE + 1] = { MARK_CBEGIN, 0 };
	uint8_t mark_3[MARK_SIZE + 1] = { MARK_CSIZE_PACK, 0 };
	uint8_t mark_4[MARK_SIZE + 1] = { MARK_CID_MESSAGE, 0 };
	uint8_t mark_5[MARK_SIZE + 1] = { MARK_CCOUNT_POSITION, 0 };
	uint8_t mark_6[MARK_SIZE + 1] = { MARK_CNUMBERS_POSITION, 0 };
	uint8_t mark_7[MARK_SIZE + 1] = { MARK_CSIZE_MESSAGE, 0 };
	uint8_t mark_8[MARK_SIZE + 1] = { MARK_CSRC, 0 };
	uint8_t mark_9[MARK_SIZE + 1] = { MARK_CEND, 0 };*/

	// std::srand(std::time(nullptr));
}

CKnuckle::~CKnuckle()
{
	for(int i = 0; i < m_countParse; i++) {
		delete[] m_markParse[i]->first;
		delete m_markParse[i];
	}
	delete[] m_markParse;

	for(int i = 0; i < m_countCheck; i++) {
		delete[] m_markCheck[i]->first;
		delete m_markCheck[i];
	}
	delete[] m_markCheck;

	delete[] m_buffer;
}

// ------------------------------------------------------------------------

void CKnuckle::FillingBuffer(uint8_t* recvArray, const int& sizeRecv, uint8_t*& commandArray, int& sizeCommand)
{
	// uint8_t buffer_debug[SIZE_BUFFFER] = { 0 };

	int index(0), size(0); // For check message
	int ps(0), sz(0);      // For clear buffer: position and size

	if(m_sizeBuffer + sizeRecv >= SIZE_BUFFER) {
		m_sizeBuffer = 0;
		printf("error: buffer overflow");
	}

	memcpy(&m_buffer[m_sizeBuffer], recvArray, sizeRecv);
	m_sizeBuffer += sizeRecv;

	while(CheckMessage(m_buffer, SIZE_BUFFER, index, size) == true) {

		CreateMessage(&m_buffer[index], size, commandArray, sizeCommand);

		ps = index + size;
		sz = SIZE_BUFFER - ps;

		memcpy(m_buffer, &m_buffer[ps], sz);
		memset(&m_buffer[sz], 0, SIZE_BUFFER - sz);
		m_sizeBuffer -= ps;
	}
}

void CKnuckle::CreateMessage(uint8_t* in, const int& sizeIn, uint8_t*& commandMessage, int& sizeCommand)
{
	TParsePack parsePack;

	int result = ParseMessage(in, sizeIn, parsePack);
	if(result == 0) {

		CMessage* message = nullptr;

		try {
			message = &m_commands.at(parsePack.m_id);

			if(message->number != parsePack.m_number || message->count != parsePack.m_count) {
				message->Clear();
				m_commands.erase(parsePack.m_id);
				message = &m_commands.insert({ parsePack.m_id, CMessage(parsePack) }).first->second;
			} else {
				message->size += parsePack.m_message.size;
				message->list.push_back(parsePack.m_message);
			}

		} catch(const std::out_of_range& oor) {
			message = &m_commands.insert({ parsePack.m_id, CMessage(parsePack) }).first->second;
		}

		message->count++;

		if(message->count == message->number) {

			uint8_t* array = new uint8_t[message->size] { 0 };

			commandMessage = array;
			sizeCommand = message->size;

			for(auto& p : message->list) {
				memcpy(array, p.value, p.size);
				array += p.size;
			}

			message->Clear();
			m_commands.erase(parsePack.m_id);
		}
	}
}

// ------------------------------------------------------------------------

bool CKnuckle::CheckMessage(uint8_t* in, const int& sizeIn, int& index, int& size)
{
	// uint8_t buffer_debug[100] = { 0 };
	// memcpy(buffer_debug, in, 100);

	int count(0), count_marks(0);

	int sizeItem = 0;
	uint8_t *pItem(nullptr), *ch(nullptr);

	while(count + MARK_SIZE <= sizeIn) {

		try {

			ch = &in[count];
			for(int i = 0; i < MARK_SIZE; i++)
				if(*ch++ != m_markCheck[count_marks]->first[i])
					throw '1';

			if(count_marks == 1) {
				CReform::ConvertToSize(pItem, sizeItem, size);
				if(count + size < sizeIn) {
					index = count + MARK_SIZE;
					count += size;
					size += MARK_SIZE;
				} else
					throw false;
			}

			if(count_marks == 2)
				throw true;

			sizeItem = 0;
			count_marks++;
			pItem = &in[count += MARK_SIZE];
		} catch(char next) {
			sizeItem++;
			count++;
		} catch(bool res) {
			return res;
		}
	}

	return false;
}

int CKnuckle::ParseMessage(uint8_t* in, const int& size, TParsePack& pack)
{
	int count = 0;
	int count_marks = 0;

	int sizeItem = 0;
	uint8_t* ch(nullptr);

	while(count + MARK_SIZE <= size) {
		try {

			ch = &in[count];
			for(int i = 0; i < MARK_SIZE; i++)
				if(*ch++ != m_markParse[count_marks]->first[i])
					throw '1';

			pFunc func = m_markParse[count_marks]->second;
			if(func != nullptr)
				func(pack, &in[count - sizeItem], sizeItem);

			count_marks++;
			count += MARK_SIZE;
			sizeItem = 0;

		} catch(char next) {
			sizeItem++;
			count++;
		} catch(int error_parse) {
			return 1;
		}
	}

	return 0;
}

void CKnuckle::SeparateList(const std::list<CMessagePair> in, std::list<CMessagePair>& out)
{
	for(auto& p : in) {
	}
}

void CKnuckle::CollectMessage(const TParsePack& in, uint8_t* bufferSend, int& count)
{
	uint8_t tempArray[BYTE2] = { 0 };

	memcpy(&bufferSend[count], MARK_SBEGIN, MARK_SIZE);
	count += MARK_SIZE + BYTE2;

	memcpy(&bufferSend[count], MARK_SSIZE_PACK, MARK_SIZE);
	count += MARK_SIZE;

	CReform::ConvertOfSize(tempArray, BYTE2, in.m_id); // 2 byte
	memcpy(&bufferSend[count], tempArray, BYTE2);
	count += BYTE2;

	memcpy(&bufferSend[count], MARK_SID_MESSAGE, MARK_SIZE);
	count += MARK_SIZE;

	bufferSend[count++] = in.m_count;

	memcpy(&bufferSend[count], MARK_SCOUNT_POSITION, MARK_SIZE);
	count += MARK_SIZE;

	bufferSend[count++] = in.m_number;

	memcpy(&bufferSend[count], MARK_SNUMBERS_POSITION, MARK_SIZE);
	count += MARK_SIZE;

	CReform::ConvertOfSize(tempArray, BYTE2, in.m_message.size); // 2 byte
	memcpy(&bufferSend[count], tempArray, BYTE2);
	count += BYTE2;

	memcpy(&bufferSend[count], MARK_SSIZE_MESSAGE, MARK_SIZE);
	count += MARK_SIZE;

	memcpy(&bufferSend[count], in.m_message.value, in.m_message.size);
	count += in.m_message.size;

	CReform::ConvertOfSize(tempArray, BYTE2, count - MARK_SIZE * 2 - 2); // 2 byte
	memcpy(&bufferSend[MARK_SIZE], tempArray, BYTE2);

	memcpy(&bufferSend[count], MARK_SEND, MARK_SIZE);
	count += MARK_SIZE;
}

void CKnuckle::SendData(uint8_t* in, const int& size, uint8_t id, SendFunc func)
{
	const int sizeOfDeparture = SIZE_TRAFFIC + SIZE_SERVICE;

	TParsePack pack;
	int sizeSend = 0;
	uint8_t sendArray[sizeOfDeparture] = { 0 };

	int pos = 0;
	int number = size / SIZE_TRAFFIC;
	if(size % SIZE_TRAFFIC != 0)
		number++;

	pack.Set(id, number);

	do {

		pack.m_message.size = size - pos < SIZE_TRAFFIC ? size - pos : SIZE_TRAFFIC;
		pack.m_message.value = new uint8_t[pack.m_message.size];
		memcpy(pack.m_message.value, &in[pos], pack.m_message.size);

		CollectMessage(pack, sendArray, sizeSend);

		if(sizeSend > sizeOfDeparture || func == nullptr)
			return;

		func(sendArray, sizeSend);

		pos += pack.m_message.size;
		memset(sendArray, 0, sizeOfDeparture);
		sizeSend = 0;

		pack.m_count++;
		pack.Clear();

	} while(size != pos);
}
