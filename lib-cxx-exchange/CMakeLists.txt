cmake_minimum_required(VERSION 3.10)

file(STRINGS "version" VERSION)

project(ExchangeLib VERSION 1.0.0)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_VERBOSE_MAKEFILE OFF)
set(CMAKE_PREFIX_PATH $ENV{LIBRARY}/RBKLib)

#set(TEST test)

find_package(rbk 1.0.0 REQUIRED)

if(CMAKE_BUILD_TYPE MATCHES "Release")
    add_compile_options(-O2)
    add_compile_options(-Wall)
endif()

if(CMAKE_BUILD_TYPE MATCHES "Debug")
    add_compile_options(-W)
endif()

include_directories(include)

if(CMAKE_BUILD_TYPE MATCHES "Release")
    add_link_options(-s)
endif()

# add_link_options(-fPIC)
# add_link_options(-static)

if (CMAKE_BUILD_TYPE MATCHES "Debug")
    set(POSTFIX d)
endif()

add_library(${PROJECT_NAME}

        src/assembly.cpp
        src/baseserver.cpp
        # src/fileLinux.cpp
        src/linkLinux.cpp
        src/listlink.cpp

        src/reform.cpp
        src/knuckle.cpp
)

set_target_properties(${PROJECT_NAME}
        PROPERTIES
        PREFIX "lib"
        OUTPUT_NAME ${PROJECT_NAME}
        DEBUG_POSTFIX "d"
)

### Test ###

if(${TEST} MATCHES test)

    add_executable(${TEST})

    set_target_properties(${TEST}
            PROPERTIES OUTPUT_NAME ${PROJECT_NAME}-${TEST}-${CMAKE_PROJECT_VERSION}
    )

    target_include_directories(${TEST} PRIVATE $ENV{GOOGLETEST}/googletest/include)

    target_link_directories(${TEST} PRIVATE $ENV{GOOGLETEST}/lib/gcc_libx64)

    target_link_libraries(${TEST} PRIVATE gtest)
    target_link_libraries(${TEST} PRIVATE gtest_main)
    #target_link_libraries(${TEST} PRIVATE Ws2_32)

    target_sources(${TEST} PRIVATE test/test.cpp)
endif()

### Install ###

set(INSTALL_HEADER_FILES
        include/assembly.h
        include/baseserver.h
        include/consts.hpp
        # include/fileLinux.hpp
        include/linkLinux.hpp
        include/listlink.h

        include/reform.h
        include/list_id.h
        include/knuckle.h
)

set(INSTALL_TARGET_FILES
        ${PROJECT_NAME}
)

include(config/install.cmake)