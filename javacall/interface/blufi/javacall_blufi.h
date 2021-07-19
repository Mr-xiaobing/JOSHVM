#ifndef __JAVACALL_BLUFI_H_
#define __JAVACALL_BLUFI_H_

#ifdef __cplusplus
extern "C"{
#endif 

#include "javacall_defs.h"

void javacall_blufi_start();

void javacall_blufi_getWifiState();

void javacall_blufi_setBleName(char*);

int  javacall_blufi_getCustomData(unsigned char*,int);

void javacall_blufi_sendMessageToPhone(char*);
#ifdef __cplusplus
}
#endif

#endif
