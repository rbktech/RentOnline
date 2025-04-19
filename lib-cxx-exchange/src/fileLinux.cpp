#include "fileLinux.hpp"

#include <algorithm>
#include <cstring>

#include <errno.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <unistd.h>

#include <sys/stat.h>

#include <ctime>

CFileLinux::CFileLinux()
{
}

CFileLinux::~CFileLinux()
{
}

ProtocolResult CFileLinux::CreateAndWriteFile(const char* path, const uint8_t* buffer, const int size)
{
    int result = 0;
    int header = 0;

    header = open(path, O_CREAT | O_EXCL | O_RDWR | O_CLOEXEC);
    if(header < 0) {
        // perror(path);
        return ProtocolResult::CreateFile;
    }

    result = write(header, buffer, size);
    if(result < 0) {
        perror(path);
        return ProtocolResult::WriteFile;
    }

    result = close(header);
    if(result < 0) {
        perror(path);
        return ProtocolResult::CloseFile;
    }

    return ProtocolResult::Success;
}

ProtocolResult CFileLinux::OpenAndReadFile(const char* path, uint8_t* buffer, int& size)
{
    int result = 0;
    int header = 0;

    header = open(path, O_EXCL | O_RDWR | O_CLOEXEC);
    if(header < 0) {
        // perror(path);
        return ProtocolResult::OpenFile;
    }

    size = read(header, buffer, size);
    if(size < 0) {
        perror(path);
        return ProtocolResult::ReadFile;
    }

    result = close(header);
    if(result < 0) {
        perror(path);
        return ProtocolResult::CloseFile;
    }

    return ProtocolResult::Success;
}
