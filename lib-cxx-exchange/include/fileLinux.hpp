#ifndef CFILELINUX_H
#define CFILELINUX_H

#include "consts.hpp"

#include <cstring>
#include <vector>

#include <dirent.h>

class CFileLinux
{
public:
    CFileLinux();
    ~CFileLinux();

    static const char* GetNameDir(DIR* dir);
    static DIR* OpenDir(const char* path);
    static ProtocolResult CloseDir(DIR* dir);

    static ProtocolResult CreateAndWriteFile(const char* path, const uint8_t* buffer, const int size);
    static ProtocolResult OpenAndReadFile(const char* path, uint8_t* buffer, int& size);
    static ProtocolResult CreateDir(const char* path);
    static ProtocolResult RenameFile(const char* old_path, const char* new_path);
    static ProtocolResult RemoveFile(const char* path);
};

#endif // CFILELINUX_H
