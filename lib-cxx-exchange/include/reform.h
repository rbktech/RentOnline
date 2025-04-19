#ifndef CREFORM_HPP
#define CREFORM_HPP

#include <cstring>
#include <stdint.h>

struct CMessagePair {
public:
	uint8_t* value = nullptr;
	uint16_t size = 0;
};

struct TParsePack {
	int m_id = 0;
	uint8_t m_count = 0;
	uint8_t m_number = 0;
	CMessagePair m_message;

	void Clear()
	{
		if(m_message.value != nullptr) {
			delete[] m_message.value;
			m_message.value = nullptr;
		}
	}

	void Set(int id, uint8_t number)
	{
		m_id = id;
		m_number = number;
	}
};

class CReform
{
public:
	CReform();
	~CReform();

	static void ConvertToSize(const uint8_t* in, const int& sizeIn, int& sizeOut);
	static void ConvertOfSize(uint8_t* out, const int& sizeOut, const int& sizeIn);
	static void SetPackId(TParsePack& pack, const uint8_t* in, const int& size);
	static void SetPackCount(TParsePack& pack, const uint8_t* in, const int& size);
	static void SetPackNumber(TParsePack& pack, const uint8_t* in, const int& size);
	static void SetPackSize(TParsePack& pack, const uint8_t* in, const int& size);
	static void SetPackMessage(TParsePack& pack, const uint8_t* in, const int& size);
};

#endif // CREFORM_HPP
