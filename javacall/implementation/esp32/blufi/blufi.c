#include "javacall_blufi.h"
#include "javacall_logging.h"

#include <esp_wifi_types.h>

static int blufi_started = 0;

extern void blufi_start_joshvm();
void javacall_blufi_start() {
	if (blufi_started == 0) {
		blufi_start_joshvm();
		blufi_started = 1;
	} else {
		javacall_logging_printf(JAVACALL_LOGGING_WARNING, JC_PROTOCOL, "Blufi already started\n");
	}
}

extern int joshvm_esp32_blufi_set_ble_name(char*);
int javacall_blufi_set_blutooth_name(char *bleName){
	return joshvm_esp32_blufi_set_ble_name(bleName);
}

extern int  joshvm_esp32_blufi_get_data(char*, int);
int javacall_blufi_get_custom_data(char* buffer,int len){
	int custom_data_len = 0;
	custom_data_len=joshvm_esp32_blufi_get_data(buffer,len);
	return custom_data_len;
}

extern void joshvm_esp32_blufi_send_data(char*, int);
void javacall_blufi_send_custom_data(char * message, int len){
	joshvm_esp32_blufi_send_data(message, len);
}

extern int joshvm_esp32_blufi_get_bt_addr(char* addr_buffer, int buffer_len);
int javacall_blufi_get_bluetooth_addrss(char* addr_buffer, int buffer_len) {
	return joshvm_esp32_blufi_get_bt_addr(addr_buffer, buffer_len);
}

extern wifi_config_t* joshvm_esp32_blufi_get_wifi_config();
int javacall_blufi_get_wifi_station_config_ssid(char* ssid, int len_ssid) {
	wifi_config_t* cfg = joshvm_esp32_blufi_get_wifi_config();
	if (cfg) {
		int len = strlen(cfg->sta.ssid);
		if (len > len_ssid) {
			return -1;
		}
		strncpy(ssid, cfg->sta.ssid, len);
		return len;
	} else {
		return -1;
	}
}

int javacall_blufi_get_wifi_station_config_password(char* password, int len_pwd) {
	wifi_config_t* cfg = joshvm_esp32_blufi_get_wifi_config();
	if (cfg) {
		int len = strlen(cfg->sta.password);
		if (len > len_pwd) {
			return -1;
		}
		strncpy(password, cfg->sta.password, len);
		return len;
	} else {
		return -1;
	}
}

extern int joshvm_esp32_wifi_get_connected_ssid(char* ssid_buffer, int buffer_len);
int javacall_wifi_get_connected_ssid(char* buffer, int len) {
	return joshvm_esp32_wifi_get_connected_ssid(buffer, len);
}

extern void joshvm_esp32_wifi_set(char*, int, char*, int, int);
void javacall_wifi_set_config(char* buffer_ssid, int len_ssid, char* buffer_pwd, int len_pwd) {
	joshvm_esp32_wifi_set((char*)buffer_ssid, len_ssid, (char*)buffer_pwd, len_pwd, 1);
}

extern void joshvm_esp32_wifi_connect();
void javacall_wifi_connect_to_AP() {
	joshvm_esp32_wifi_connect();
}

extern void joshvm_esp32_wifi_disconnect();
void javacall_wifi_disconnect_from_AP() {	
	joshvm_esp32_wifi_disconnect();
}

extern void joshvm_esp32_blufi_close();
void javacall_blufi_close() {	
	joshvm_esp32_blufi_close();
	blufi_started = 0;
}

extern int joshvm_esp32_blufi_is_connected();
javacall_bool javacall_blufi_is_connected() {
	if (joshvm_esp32_blufi_is_connected()) {
		return JAVACALL_TRUE;
	} else {
		return JAVACALL_FALSE;
	}
}