package ru.rbkdev.lease.main;

public class CLISTID {

	public static final int SIZE_SERVICE = 50; // 50 byte
	public static final int SIZE_TRAFFIC = 1000; // 1000 byte
	public static final int SIZE_BUFFER = SIZE_TRAFFIC * 3; // min 3 message
	public static final int MAX_SIZE_SEND = 1024 * 1024;

	// -------------------------------------------------------------------

	public static final int SERVER_SUCCESS = 11;
	public static final int SERVER_ERROR = SERVER_SUCCESS + 1;

	// -------------------------------------------------------------------

	public static final int COMMAND_HEADER_SIZE = 3;

	public static final int COMMAND_SUCCESS = 21;
	public static final int COMMAND_ERROR = COMMAND_SUCCESS + 1;
	public static final int COMMAND_NOT_FOUND = COMMAND_SUCCESS + 2;

	public static final int COMMAND_READ_FARM_BEGIN = COMMAND_SUCCESS + 3;
	public static final int COMMAND_READ_FARM_NEXT = COMMAND_SUCCESS + 4;
	public static final int COMMAND_READ_FARM_END = COMMAND_SUCCESS + 5;

	// -------------------------------------------------------------------

	public static final int ERROR_MIN_ITEM = 50;

	// -------------------------------------------------------------------

	public static final int MARK_SIZE = 5;
}

