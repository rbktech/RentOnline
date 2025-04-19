cmake_minimum_required(VERSION 3.10)

project(server-cxx-unix)
set(CMAKE_CXX_STANDARD 17)

list(APPEND CMAKE_PREFIX_PATH $ENV{LIBRARY}/RBKLib)
list(APPEND CMAKE_PREFIX_PATH $ENV{LIBRARY}/ExchangeLib)

find_package(rbk 1.0.0 REQUIRED)
find_package(ExchangeLib 1.0.0 REQUIRED)

add_executable(${PROJECT_NAME}
        src/main.cpp
        src/alock.cpp
        src/core/basecommand.cpp
        src/core/door.cpp
        src/core/moderator.cpp
        src/core/user.cpp
)

target_link_libraries(${PROJECT_NAME} ExchangeLib)
target_link_libraries(${PROJECT_NAME} rbk)
target_link_libraries(${PROJECT_NAME} ws2_32)