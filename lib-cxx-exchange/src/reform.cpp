#include "reform.h"

CReform::CReform()
{
}

CReform::~CReform()
{
}

void CReform::ConvertToSize(const uint8_t* in, const int& sizeIn, int& sizeOut)
{
	if(sizeIn == 2) {
		sizeOut = in[0] << 8;
		sizeOut |= in[1];
	}
}

void CReform::ConvertOfSize(uint8_t* out, const int& sizeOut, const int& sizeIn)
{
	if(sizeOut == 2) {
		out[0] = sizeIn >> 8; // 2 byte
		out[1] = sizeIn;
	}
}

void CReform::SetPackId(TParsePack& pack, const uint8_t* in, const int& size)
{
	ConvertToSize(in, size, pack.m_id);
}

void CReform::SetPackCount(TParsePack& pack, const uint8_t* in, const int& size)
{
	if(size == 1)
		pack.m_count = in[0];
	else
		throw 2;
}

void CReform::SetPackNumber(TParsePack& pack, const uint8_t* in, const int& size)
{
	if(size == 1)
		pack.m_number = in[0];
	else
		throw 2;
}

void CReform::SetPackSize(TParsePack& pack, const uint8_t* in, const int& size)
{
	if(size == 2) {
		pack.m_message.size = in[0] << 8;
		pack.m_message.size |= in[1];
	} else
		throw 2;
}

void CReform::SetPackMessage(TParsePack& pack, const uint8_t* in, const int& size)
{
	if(pack.m_message.size == size) {
		pack.m_message.value = new uint8_t[size + 1] { 0 };
		memcpy(pack.m_message.value, in, size);
	} else
		throw 2;
}
