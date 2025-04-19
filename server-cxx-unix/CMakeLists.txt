cmake_minimum_required(VERSION 3.10)

project(server-cxx-unix)
set(CMAKE_CXX_STANDARD 17)

set(TEST test)

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

### TEST ###

if(${TEST} MATCHES test)

    add_executable(${TEST})

    set_target_properties(${TEST}
            PROPERTIES OUTPUT_NAME ${PROJECT_NAME}-${TEST}-${CMAKE_PROJECT_VERSION}
    )

    target_include_directories(${TEST} PRIVATE src)
    target_include_directories(${TEST} PRIVATE $ENV{GOOGLETEST}/googletest/include)

    target_link_directories(${TEST} PRIVATE $ENV{GOOGLETEST}/lib/gcc_libx64)

    target_link_libraries(${TEST} PRIVATE gtest)
    target_link_libraries(${TEST} PRIVATE gtest_main)
    target_link_libraries(${TEST} PRIVATE ExchangeLib)
    target_link_libraries(${TEST} PRIVATE rbk)
    target_link_libraries(${TEST} PRIVATE ws2_32)

    target_sources(${TEST} PRIVATE src/alock.cpp)
    target_sources(${TEST} PRIVATE src/core/basecommand.cpp)

    target_sources(${TEST} PRIVATE test/commands.cpp)

endif()