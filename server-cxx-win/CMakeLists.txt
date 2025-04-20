cmake_minimum_required(VERSION 3.10)

project(server-cxx-win)

set(CMAKE_CXX_STANDARD 17)

# set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/bin)
# set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/bin)
# set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/bin)

set(CMAKE_PREFIX_PATH $ENV{LIBRARY}/RBKLib)

find_package(rbk 1.0.0 REQUIRED)

add_definitions(-DCURL_STATICLIB)

if (${CMAKE_BUILD_TYPE} MATCHES Release)
    add_link_options(-O2)
    add_link_options(-Wl,--gc-sections)
    add_link_options(-Wl,-s)
endif()

include_directories($ENV{CURLLib}/include)
include_directories($ENV{OPENSSL}/include)
include_directories($ENV{JSONCPP}/include)
include_directories(E:/library/sqllite)

link_directories($ENV{OPENSSL}/lib/gcc_libx64)
link_directories($ENV{CURLLib}/lib/gcc_libx64)
link_directories($ENV{JSONCPP}/lib/gcc_libx64)
link_directories(E:/library/sqllite)

add_executable(${PROJECT_NAME}
        src/main.cpp
        src/core.cpp
        src/tcpserverssl.cpp
        src/crypto.cpp
        src/query.cpp
)


target_link_libraries(${PROJECT_NAME} rbk)
target_link_libraries(${PROJECT_NAME} curl)
target_link_libraries(${PROJECT_NAME} ssl)
target_link_libraries(${PROJECT_NAME} crypto)
target_link_libraries(${PROJECT_NAME} ws2_32)
target_link_libraries(${PROJECT_NAME} crypt32)
target_link_libraries(${PROJECT_NAME} RPCRT4)
target_link_libraries(${PROJECT_NAME} jsoncpp)
target_link_libraries(${PROJECT_NAME} sqlite3)

#target_link_libraries(${PROJECT_NAME} gdi32)
target_link_libraries(${PROJECT_NAME} Wldap32)
#target_link_libraries(${PROJECT_NAME} wsock32)
