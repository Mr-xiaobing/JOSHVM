package org.joshvm.esp32.blufi;

import org.joshvm.esp32.wifi.WifiStationConfig;
import org.joshvm.esp32.wifi.WifiEventListener;

public class BlufiServer{
/**
	start ble Distribution network
*/

private static BlufiThread blufiThread = new BlufiThread();

private static boolean started = false;

private native static void start0();
private native static void close0();

/**
 * Start blufi server
 */
public synchronized static void start(){
	started = true;
	blufiThread.start();
	start0();
}

public synchronized static void close(){
	close0();
	try {
		blufiThread.join();
	} catch (InterruptedException e) {}
	started = false;
}

/**
 * Set blufi event listener
 * @param listener the listener to be set
 */
public static void  setBlufiEventListener(BlufiEventListener listener){
	blufiThread.setBlufiEventListener(listener);
}

/**
 * Set Wifi event listener
 * @param listener the listener to be set
 */
public static void  setWifiEventListener(WifiEventListener listener){
	blufiThread.setWifiEventListener(listener);
}


/**
 * Send blufi custom data
 * @param Message the message buffer to be sent
 */
public static void sendCustomData(byte[] Message) {
	sendCustomData0(Message);
}
/**
 * Set BLE name to be discovered
 * @param name the name to be set
 */
public static void setDeviceName(String name) {
	if (name.length() >= 64) {
		throw new IllegalArgumentException("Name too long");
	}
	setDeviceName0(name);
}

/**
 * Get BLE connection status
 * @return true if connected; false if not connected
 */
public static boolean isBleConnected() {
	return isBleConnected0();
}

/**
 * Get local bluetooth address
 * @param addr the byte buffer for returned address. Usually the size of array should be 6
 * @return <0 if error, otherwise the actual length of addr
 */
public native static int getBluetoothAddress(byte[] addr);

protected static WifiStationConfig getWifiStationConfig() {
	byte[] ssid_buf = new byte[32];
	byte[] pwd_buf = new byte[64];
	int len;

	len = getWifiStationConfigSsid0(ssid_buf);
	if (len < 0) {
		len = 0;
	}
	byte[] ssid = new byte[len];
	System.arraycopy(ssid_buf, 0, ssid, 0, len);

	len = getWifiStationConfigPassword0(pwd_buf);
	if (len < 0) {
		len = 0;
	}
	byte[] pwd = new byte[len];
	System.arraycopy(pwd_buf, 0, pwd, 0, len);
	return new WifiStationConfig(ssid, pwd);
}

private static native int getWifiStationConfigSsid0(byte[] ssid);
private static native int getWifiStationConfigPassword0(byte[] pwd);
private native static void sendCustomData0(byte[] Message);
private native static void setDeviceName0(String name);
private native static boolean isBleConnected0();

}
