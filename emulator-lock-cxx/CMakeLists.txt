cmake_minimum_required(VERSION 3.10)

file(STRINGS "version" VERSION)

project(emulator-lock-cxx VERSION ${VERSION})

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_VERBOSE_MAKEFILE OFF)

list(APPEND CMAKE_PREFIX_PATH $ENV{LIBRARY}/RBKLib)
list(APPEND CMAKE_PREFIX_PATH $ENV{LIBRARY}/ExchangeLib)

find_package(rbk 1.0.0 REQUIRED)
find_package(ExchangeLib 1.0.0 REQUIRED)

if (${CMAKE_BUILD_TYPE} MATCHES Debug)
    add_compile_options(-O0)
    add_compile_options(-Wall)
    add_compile_options(-Wextra)
endif()

if (${CMAKE_BUILD_TYPE} MATCHES Release)
    add_link_options(-s)
endif()

add_link_options(-static)
add_link_options(-static-libgcc)
add_link_options(-static-libstdc++)

add_executable(${PROJECT_NAME})

set_target_properties(${PROJECT_NAME}
        PROPERTIES
        OUTPUT_NAME ${PROJECT_NAME}-${CMAKE_BUILD_TYPE}-${PROJECT_VERSION}
)

target_sources(${PROJECT_NAME} PRIVATE main.cpp)

target_link_libraries(${PROJECT_NAME} ExchangeLib)
target_link_libraries(${PROJECT_NAME} rbk)
target_link_libraries(${PROJECT_NAME} ws2_32)