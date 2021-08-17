#include <sni.h>
#include <kni.h>
#include <sni_event.h>

#if ENABLE_PCSL
#include <javacall_logging.h>
#include <pcsl_memory.h>
#include <javacall_blufi.h>
#endif
	/**
		native org.joshvm.blufi.BlufiServer start()
	*/
	KNIEXPORT KNI_RETURNTYPE_VOID Java_org_joshvm_esp32_blufi_BlufiServer_start0(){
    #if ENABLE_PCSL
		javacall_blufi_start();
	#endif
	}
	
	/**
	  native org.joshvm.blufi.BlufiServer setBleName();
	*/
	KNIEXPORT KNI_RETURNTYPE_VOID Java_org_joshvm_esp32_blufi_BlufiServer_setDeviceName0(){
    #if ENABLE_PCSL
	   int nameLen;
       char bleName[64];
	   int i;
       jchar* temp;

	   KNI_StartHandles(1);
	   KNI_DeclareHandle(nameObject);
	   KNI_GetParameterAsObject(1,nameObject);

	   if (!KNI_IsNullHandle(nameObject)) {
			nameLen= KNI_GetStringLength(nameObject);
	   		if(nameLen < 64) {
				temp = (jchar*)bleName;
				KNI_GetStringRegion(nameObject,0,nameLen,temp);
				for(i =0;i<nameLen;i++){
					bleName[i] =(char)temp[i];
				}
				bleName[nameLen] = 0;
				javacall_blufi_set_blutooth_name(bleName);
			}
	   }
	   KNI_EndHandles();
	#endif
	}
	KNIEXPORT KNI_RETURNTYPE_INT Java_org_joshvm_esp32_blufi_BlufiThread_getCustomData(){
		int len = 0;
		void* buffer;
	#if ENABLE_PCSL
		KNI_StartHandles(1);
		KNI_DeclareHandle(buffer_byte);
		KNI_GetParameterAsObject(1,buffer_byte);
		if (!KNI_IsNullHandle(buffer_byte)) {
			buffer = SNI_GetRawArrayPointer(buffer_byte);
			len= KNI_GetArrayLength(buffer_byte);
			len = javacall_blufi_get_custom_data((char*)buffer, len);
		}
		KNI_EndHandles();
	#endif	
	KNI_ReturnInt(len);
	}
	
	KNIEXPORT KNI_RETURNTYPE_VOID Java_org_joshvm_esp32_blufi_BlufiServer_sendCustomData0(){
	#if ENABLE_PCSL
		int len;
		void* buffer;
		KNI_StartHandles(1);
		KNI_DeclareHandle(buffer_byte);
		KNI_GetParameterAsObject(1,buffer_byte);
		if (!KNI_IsNullHandle(buffer_byte)) {
			buffer = SNI_GetRawArrayPointer(buffer_byte);
			len= KNI_GetArrayLength(buffer_byte);
			javacall_blufi_send_custom_data((char*)buffer, len);
		}
		
		KNI_EndHandles();
	 #endif
	}

	KNIEXPORT KNI_RETURNTYPE_INT Java_org_joshvm_esp32_blufi_BlufiServer_getBluetoothAddress() {
		int len = -1;
		void* buffer;
	#if ENABLE_PCSL
		KNI_StartHandles(1);
		KNI_DeclareHandle(buffer_byte);
		KNI_GetParameterAsObject(1,buffer_byte);
		if (!KNI_IsNullHandle(buffer_byte)) {
	    	buffer = SNI_GetRawArrayPointer(buffer_byte);
			len= KNI_GetArrayLength(buffer_byte);
			len = javacall_blufi_get_bluetooth_addrss((char*)buffer, len);
		}
		KNI_EndHandles();
	#endif	
		KNI_ReturnInt(len);
	}

	KNIEXPORT KNI_RETURNTYPE_BOOLEAN Java_org_joshvm_esp32_blufi_BlufiServer_isBleConnected0() {
		jboolean connected = KNI_FALSE;
	#if ENABLE_PCSL
		if (javacall_blufi_is_connected()) {
			connected = KNI_TRUE;
		}
	#endif
		return connected;
	}
	
	KNIEXPORT KNI_RETURNTYPE_INT Java_org_joshvm_esp32_blufi_BlufiThread_waitForBlufiEvent(){
	int handle = -1;
	int result = -1;
	#if ENABLE_PCSL
		SNIReentryData* info =(SNIReentryData*)SNI_GetReentryData((int*)NULL);
		if(info == NULL){
		  SNIEVT_wait(BLUFI_SIGNAL,(int)handle,(void*)NULL);
		}else{
		 result = info->status;
		}
		 //SNIEVT_wait(BLUFI_EVT_CUSTOMDATA_EVENT,(int)handle,(void*)NULL);
	#endif
	KNI_ReturnInt(result); 	
	}

	KNIEXPORT KNI_RETURNTYPE_INT Java_org_joshvm_esp32_wifi_WifiManager_getConnectedApName0() {
		int len = -1;
		void* buffer;
	#if ENABLE_PCSL
		KNI_StartHandles(1);
		KNI_DeclareHandle(buffer_byte);
		KNI_GetParameterAsObject(1,buffer_byte);
		if (!KNI_IsNullHandle(buffer_byte)) {
	    	buffer = SNI_GetRawArrayPointer(buffer_byte);		
			len= KNI_GetArrayLength(buffer_byte);
			len = javacall_wifi_get_connected_ssid((char*)buffer, len);
		}		
		KNI_EndHandles();
	#endif	
		KNI_ReturnInt(len);
	}

	KNIEXPORT KNI_RETURNTYPE_VOID Java_org_joshvm_esp32_wifi_WifiManager_setStationConfig0() {
		int len_ssid = 0;
		int len_pwd = 0;
		void* buffer_ssid = (void*)0;
		void* buffer_pwd = (void*)0;
	#if ENABLE_PCSL
		KNI_StartHandles(2);
		KNI_DeclareHandle(ssid_buffer_byte);
		KNI_DeclareHandle(pwd_buffer_byte);
		KNI_GetParameterAsObject(1,ssid_buffer_byte);
		KNI_GetParameterAsObject(2,pwd_buffer_byte);	    
		
		if (!KNI_IsNullHandle(ssid_buffer_byte)) {
			buffer_ssid = SNI_GetRawArrayPointer(ssid_buffer_byte);
			len_ssid= KNI_GetArrayLength(ssid_buffer_byte);
		}
		if (!KNI_IsNullHandle(pwd_buffer_byte)) {
			buffer_pwd = SNI_GetRawArrayPointer(pwd_buffer_byte);		
			len_pwd = KNI_GetArrayLength(pwd_buffer_byte);			
		}

		javacall_wifi_set_config((char*)buffer_ssid, len_ssid, (char*)buffer_pwd, len_pwd);
		KNI_EndHandles();
	#endif	
		KNI_ReturnVoid();
	}

	KNIEXPORT KNI_RETURNTYPE_INT Java_org_joshvm_esp32_blufi_BlufiServer_getWifiStationConfigSsid0() {
		int len_ssid = 0;
		void* buffer_ssid = (void*)0;
	#if ENABLE_PCSL		
		KNI_StartHandles(1);
		KNI_DeclareHandle(ssid_buffer_byte);
		KNI_GetParameterAsObject(1,ssid_buffer_byte);	    
		
		if (!KNI_IsNullHandle(ssid_buffer_byte)) {
			buffer_ssid = SNI_GetRawArrayPointer(ssid_buffer_byte);
			len_ssid= KNI_GetArrayLength(ssid_buffer_byte);
		}

		if (buffer_ssid) {
			len_ssid = javacall_blufi_get_wifi_station_config_ssid((char*)buffer_ssid, len_ssid);
		}

		KNI_EndHandles();
	#endif	
		KNI_ReturnInt(len_ssid);
	}

	KNIEXPORT KNI_RETURNTYPE_INT Java_org_joshvm_esp32_blufi_BlufiServer_getWifiStationConfigPassword0() {
		int len_pwd = 0;		
		void* buffer_pwd = (void*)0;
	#if ENABLE_PCSL		
		KNI_StartHandles(1);
		KNI_DeclareHandle(pwd_buffer_byte);
		KNI_GetParameterAsObject(1,pwd_buffer_byte);	    
		
		if (!KNI_IsNullHandle(pwd_buffer_byte)) {
			buffer_pwd = SNI_GetRawArrayPointer(pwd_buffer_byte);		
			len_pwd = KNI_GetArrayLength(pwd_buffer_byte);			
		}
		
		if (buffer_pwd) {
			len_pwd = javacall_blufi_get_wifi_station_config_password((char*)buffer_pwd, len_pwd);
		}

		KNI_EndHandles();
	#endif	
		KNI_ReturnInt(len_pwd);
	}

	KNIEXPORT KNI_RETURNTYPE_VOID Java_org_joshvm_esp32_wifi_WifiManager_connect0() {
	#if ENABLE_PCSL		
		javacall_wifi_connect_to_AP();
	#endif
	}

	KNIEXPORT KNI_RETURNTYPE_VOID Java_org_joshvm_esp32_wifi_WifiManager_disconnect0() {
	#if ENABLE_PCSL
		javacall_wifi_disconnect_from_AP();
	#endif
	}

	KNIEXPORT KNI_RETURNTYPE_VOID Java_org_joshvm_esp32_blufi_BlufiServer_close0() {
	#if ENABLE_PCSL
		javacall_blufi_close();
	#endif
	}
