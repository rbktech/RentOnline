#ifndef CONSTS_H
#define CONSTS_H

#define OWN_PATH '.'

#include <stdint.h>

// Const value

const uint8_t g_ferms[] = { OWN_PATH, '/', 'f', 'e', 'r', 'm', 's', '/' };
const uint8_t g_sizeFerms = sizeof(g_ferms) / sizeof(g_ferms[0]);

const uint8_t g_users[] = { OWN_PATH, '/', 'u', 's', 'e', 'r', 's', '/' };
const uint8_t g_sizeUsers = sizeof(g_users) / sizeof(g_users[0]);

const uint8_t g_file[] = { '/', 'f', 'i', 'l', 'e', '.', 't', 'x', 't' };
const uint8_t g_sizeFile = sizeof(g_file) / sizeof(g_file[0]);

const uint8_t g_tag[] = { 't', 'a', 'g' };
const uint8_t g_sizeTag = sizeof(g_tag) / sizeof(g_tag[0]);

const uint8_t g_foto[] = { 'f', 'o', 't', 'o', '_' };
const uint8_t g_sizeFoto = sizeof(g_foto) / sizeof(g_foto[0]);

const uint8_t g_png[] = { '.', 'p', 'n', 'g' };
const uint8_t g_sizePng = sizeof(g_png) / sizeof(g_png[0]);

// Towns

const uint8_t g_townMoscow[] = { 'm', 'o', 's', 'c', 'o', 'w', '/' };
const uint8_t g_sizeMoscow = sizeof(g_townMoscow) / sizeof(g_townMoscow[0]);

#endif // CONSTS_H
