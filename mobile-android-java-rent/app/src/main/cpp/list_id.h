#ifndef LIST_ID_H
#define LIST_ID_H

enum {
	SIZE_SERVICE = 50,
	SIZE_TRAFFIC = 1000, // 1000 byte
	SIZE_BUFFER =  SIZE_TRAFFIC * 3, // min 3 message

	FILE_SIZE = 512,
	FILE_SIZE_PHOTO = 1024 * 1024, // 1 mb,
	SIZE_ANSWER_DATA = FILE_SIZE_PHOTO * 5, // 5 mb

	// --------------------------------------------------------------------

	SERVER_SUCCESS = 11,
	SERVER_ERROR,

	// --------------------------------------------------------------------

	COMMAND_HEADER_SIZE = 3,

	COMMAND_SUCCESS = 21,
	COMMAND_ERROR,

	COMMAND_NOT_FOUND,

	COMMAND_READ_FARM_BEGIN,
	COMMAND_READ_FARM_NEXT,
	COMMAND_READ_FARM_END,

	// --------------------------------------------------------------------

	ERROR_MIN_ITEM = 51,
};

#endif // LIST_ID_H
