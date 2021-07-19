package org.joshvm.esp32.blufi;

class BlufiThread extends Thread{

public static byte[] buffer = new byte[64];

public final int BLUFI_EVT_CUSTOMDATA_EVENT =1;

public native int watiForBlufiEvent();

//public native int getCustomDataLength();

public native int  getCustomData(byte[] buffer);

private BlufiEventListener customDataEventListener;

public void invcokeCustomDataReceiveEventListener(byte[] data, int length){
	if(customDataEventListener !=null){
		System.out.print("nead to set Listener");
		byte[] buf = new byte[length];
		System.arraycopy(data,0,buf,0,length);
		customDataEventListener.onCustomDataReceiveEventListener(buf);
	}
}

public  void setBlufiEventListener(BlufiEventListener listener){
	customDataEventListener = listener;
}

public void run(){
	while(true){
		int ret =  watiForBlufiEvent();
		if (ret == BLUFI_EVT_CUSTOMDATA_EVENT){
		int receiveDataLength = getCustomData(buffer);
		if(receiveDataLength>0){
			 invcokeCustomDataReceiveEventListener(buffer,receiveDataLength);
		}
	}
}
}
}
