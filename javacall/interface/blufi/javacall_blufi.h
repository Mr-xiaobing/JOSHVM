#ifndef __JAVACALL_BLUFI_H_
#define __JAVACALL_BLUFI_H_

#ifdef __cplusplus
extern "C"{
#endif 

#include "javacall_defs.h"

void javacall_blufi_start();

void javacall_blufi_close();

void javacall_blufi_set_blutooth_name(char*);

int  javacall_blufi_get_custom_data(char*,int);

void javacall_blufi_send_custom_data(char*,int);

int javacall_blufi_get_bluetooth_addrss(char*, int);

int javacall_blufi_get_wifi_station_config_ssid(char* ssid, int len_ssid);

int javacall_blufi_get_wifi_station_config_password(char* password, int len_pwd);

int javacall_wifi_get_connected_ssid(char* buffer, int len);

void javacall_wifi_set_config(char* buffer_ssid, int len_ssid, char* buffer_pwd, int len_pwd);

void javacall_wifi_connect_to_AP();

void javacall_wifi_disconnect_from_AP();

javacall_bool javacall_blufi_is_connected();
#ifdef __cplusplus
}
#endif

#endif
