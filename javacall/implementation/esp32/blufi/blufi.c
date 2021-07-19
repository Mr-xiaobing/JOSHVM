#include <javacall_logging.h>
#include "javacall_blufi.h"

extern void blufi_start_joshvm();
void javacall_blufi_start() {
	//javacall_printf("finsh blufi %s ",bluetoothName);
	javacall_printf("start_ble \n");
	blufi_start_joshvm();
	javacall_printf("start_ble end \n");
}
extern void blufi_getWifiState_joshvm();
void javacall_blufi_getWifiState(){
	javacall_printf("getWifi  start\n");
	blufi_getWifiState_joshvm();
	javacall_printf("getWifi end\n");
}
extern void blufi_setBleName_joshvm();
void javacall_blufi_setBleName(char *bleName){
     	javacall_printf("setBleName start \n");
	blufi_setBleName_joshvm(bleName);
	javacall_printf("setBleName end\n");
}
extern int  blufi_getCustomData_joshvm();
int javacall_blufi_getCustomData(unsigned char* buffer,int len){
	javacall_printf("test_GetCustomData start\n");
	int custom_data_len = 0;
	custom_data_len=blufi_getCustomData_joshvm(buffer,len);
	javacall_printf("test_getCustomdata end %d\n", custom_data_len);
	return custom_data_len;
}
extern void blufi_sendMessageToPhone_joshvm();
void javacall_blufi_sendMessageToPhone(char * message){
	javacall_printf("sendMessage_javacall");
	blufi_sendMessageToPhone_joshvm(message);
}
