#include "javacall_blufi.h"

void javacall_blufi_start() {
}

void javacall_blufi_set_blutooth_name(char *bleName){
}

int javacall_blufi_get_custom_data(char* buffer,int len){
	return -1;
}

void javacall_blufi_send_custom_data(char * message, int len){
}

int javacall_blufi_get_bluetooth_addrss(char* addr_buffer, int buffer_len) {
	return -1;
}

int javacall_blufi_get_wifi_station_config_ssid(char* ssid, int len_ssid) {
	return -1;
}

int javacall_blufi_get_wifi_station_config_password(char* password, int len_pwd) {
	return -1;
}

int javacall_wifi_get_connected_ssid(char* buffer, int len) {
	return -1;
}

void javacall_wifi_set_config(char* buffer_ssid, int len_ssid, char* buffer_pwd, int len_pwd) {	
}

void javacall_wifi_connect_to_AP() {
}

void javacall_wifi_disconnect_from_AP() {	
}

void javacall_blufi_close() {	
}

javacall_bool javacall_blufi_is_connected() {
	return JAVACALL_FALSE;
}