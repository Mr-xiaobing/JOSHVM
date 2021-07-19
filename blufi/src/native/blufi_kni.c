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
	   native org.joshvm.blufi.BlufiServer getWifiState()
	*/
	KNIEXPORT KNI_RETURNTYPE_VOID Java_org_joshvm_esp32_blufi_BlufiServer_getWifiState(){
	#if ENABLE_PCSL
		javacall_blufi_getWifiState();
	#endif
	}
	/**
	  native org.joshvm.blufi.BlufiServer setBleName();
	*/
	KNIEXPORT KNI_RETURNTYPE_VOID Java_org_joshvm_esp32_blufi_BlufiServer_setBleName(){
    	#if ENABLE_PCSL
	   int nameLen;
           char bleName[64];
	   int i;
           jchar* temp;
	   KNI_StartHandles(1);
	   KNI_DeclareHandle(nameObject);
	   KNI_GetParameterAsObject(1,nameObject);
	   nameLen= KNI_GetStringLength(nameObject);
	   if(nameLen>64){
		javacall_printf("ble name has word length: %d \n",nameLen);
	//	KNI_ThrowNew(KNIIllegalArgumentException,(char*)gKNIBuffer);
	   }else{
		temp = (jchar*)bleName;
		KNI_GetStringRegion(nameObject,0,nameLen,temp);
		for(i =0;i<nameLen;i++){
			bleName[i] =(char)temp[i];
	   	}
        	bleName[nameLen] = 0;
	    javacall_blufi_setBleName(bleName);
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
	        buffer = SNI_GetRawArrayPointer(buffer_byte);
		if (buffer) {
			len= KNI_GetArrayLength(buffer_byte);
			len = javacall_blufi_getCustomData((unsigned char*)buffer, len);
		}
		KNI_EndHandles();
	#endif	
	KNI_ReturnInt(len);
	}
	
	KNIEXPORT KNI_RETURNTYPE_VOID Java_org_joshvm_esp32_blufi_BlufiServer_sendMessageToPhone(){
	#if ENABLE_PCSL
		int  messageLen;
		char message[64];
		int  i;
		jchar* temp;
		KNI_StartHandles(1);
		KNI_DeclareHandle(nameObject);
		KNI_GetParameterAsObject(1,nameObject);
		messageLen =  KNI_GetStringLength(nameObject);
		if(messageLen>=64){
			//char tempMessage[17]={'m','e','s','s','a','g','e',' ','t','o','o',' ','l','o','n','g','\0'};
			//for(i=0;i<17;i++){
			 //message[i]=tempMessage[i];
			//}	
		}else{
		      temp =  (jchar*)message;
		      KNI_GetStringRegion(nameObject,0,messageLen,temp);
		      for(i = 0;i<messageLen;i++){
			message[i]=(char)temp[i];
		      }
		      message[messageLen]='\0';
		}
		
		javacall_blufi_sendMessageToPhone(message);
		KNI_EndHandles();
	 #endif
	}
	
	KNIEXPORT KNI_RETURNTYPE_INT Java_org_joshvm_esp32_blufi_BlufiThread_watiForBlufiEvent(){
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
