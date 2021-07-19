package org.joshvm.esp32.blufi;

public class BlufiServer{
/**
	start ble Distribution network
*/

private static BlufiThread blufiThread = new BlufiThread();

public native static void start0();

public synchronized static void start(){
	blufiThread.start();
	start0();
}

public static void  setCustomDataReceiveListener(BlufiEventListener listener){
	blufiThread.setBlufiEventListener(listener);
}

public native static void sendMessageToPhone(String Message);
/**
	set ble name 
*/
public  native void setBleName(String name);


/**
	get wifi state
*/
public native void getWifiState();

}
