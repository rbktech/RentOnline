if(CMAKE_CXX_COMPILER_ID MATCHES "GNU")
    set(CONFIG_NAME "gcc")
    set(OUTPUT_DIRECTORY "gcc")
elseif (CMAKE_CXX_COMPILER_ID MATCHES "MSVC")
    set(CONFIG_NAME "msvc")
    set(OUTPUT_DIRECTORY "msvc")
endif()

if(BUILD_SHARED_LIBS)
    # set(STATIC_POSTFIX "")
    set(CONFIG_NAME "${CONFIG_NAME}-dll")
    set(OUTPUT_DIRECTORY "${OUTPUT_DIRECTORY}/dll")
else()
    # set(STATIC_POSTFIX "s")
    set(CONFIG_NAME "${CONFIG_NAME}-lib")
    set(OUTPUT_DIRECTORY "${OUTPUT_DIRECTORY}/lib")
endif()

if(BUILD_X32_LIBS)
    set(CMAKE_C_FLAGS -m32)
    set(CMAKE_CXX_FLAGS -m32)
    set(CMAKE_RC_FLAGS --target=pe-i386)

    # set(BITNESS_POSTFIX "32")
    set(CONFIG_NAME "${CONFIG_NAME}-x32")
    set(OUTPUT_DIRECTORY "${OUTPUT_DIRECTORY}/x32")
else()
    # set(BITNESS_POSTFIX "")
    set(CONFIG_NAME "${CONFIG_NAME}-x64")
    set(OUTPUT_DIRECTORY "${OUTPUT_DIRECTORY}/x64")
endif()

if(CMAKE_BUILD_TYPE MATCHES "Debug")
    set(CONFIG_NAME "${CONFIG_NAME}-debug")
else()
    set(CONFIG_NAME "${CONFIG_NAME}-release")
endif()

# ------------------------------------------------------------------------

# Путь для include файлов
foreach(ITEM ${INSTALL_HEADER_FILES})

    get_filename_component(ITEM_PATH ${ITEM} DIRECTORY)

	install(
        FILES ${ITEM}
        DESTINATION ${PROJECT_NAME}-${PROJECT_VERSION}/${ITEM_PATH}
    )
endforeach()

# Путь для библиотек
install(
        TARGETS ${INSTALL_TARGET_FILES}
        DESTINATION ${PROJECT_NAME}-${PROJECT_VERSION}/lib/${OUTPUT_DIRECTORY}
        EXPORT ${PROJECT_NAME}-targets
)

# Путь для автоматически генерируемых cmake файлов
install(
        EXPORT ${PROJECT_NAME}-targets
        DESTINATION ${PROJECT_NAME}-${PROJECT_VERSION}/lib/${OUTPUT_DIRECTORY}/target
)

# Создание файлов конфигурации и версии пакета.
configure_file(
        ${CMAKE_CURRENT_SOURCE_DIR}/config/template-config.cmake.in
        ${CMAKE_CURRENT_BINARY_DIR}/config/${CONFIG_NAME}-config.cmake @ONLY
)

configure_file(
        ${CMAKE_CURRENT_SOURCE_DIR}/config/template-config-version.cmake.in
        ${CMAKE_CURRENT_BINARY_DIR}/config/${CONFIG_NAME}-config-version.cmake @ONLY
)

configure_file(
        ${CMAKE_CURRENT_SOURCE_DIR}/config/template-base-config.cmake.in
        ${CMAKE_CURRENT_BINARY_DIR}/config/${PROJECT_NAME}-config.cmake @ONLY
)

configure_file(
        ${CMAKE_CURRENT_SOURCE_DIR}/config/template-config-version.cmake.in
        ${CMAKE_CURRENT_BINARY_DIR}/config/${PROJECT_NAME}-config-version.cmake @ONLY
)

# Установка файлов конфигурации и версии пакета.
install(FILES
        ${CMAKE_CURRENT_BINARY_DIR}/config/${CONFIG_NAME}-config.cmake
        ${CMAKE_CURRENT_BINARY_DIR}/config/${CONFIG_NAME}-config-version.cmake

        ${CMAKE_CURRENT_BINARY_DIR}/config/${PROJECT_NAME}-config.cmake
        ${CMAKE_CURRENT_BINARY_DIR}/config/${PROJECT_NAME}-config-version.cmake

        DESTINATION ${PROJECT_NAME}-${PROJECT_VERSION}/lib/cmake/${PROJECT_NAME}-${PROJECT_VERSION}
)