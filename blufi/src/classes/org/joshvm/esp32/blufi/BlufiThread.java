package org.joshvm.esp32.blufi;

import org.joshvm.esp32.wifi.WifiEventListener;

class BlufiThread extends Thread{

private static byte[] buffer = new byte[64];

private static final int BLUFI_EVT_CUSTOMDATA_EVENT = 1;
private static final int BLUFI_EVT_SSID_EVENT = 2;
private static final int BLUFI_EVT_PASSWORD_EVENT = 3;
private static final int BLUFI_EVT_ACTIVE_CLOSE_EVENT = 4;
private static final int BLUFI_EVT_CONNECT_EVENT = 5;
private static final int BLUFI_EVT_DISCONNECT_EVENT = 6;
private static final int BLUFI_EVT_GOT_REQ_WIFI_CONNECT_EVENT = 7;
private static final int BLUFI_EVT_GOT_REQ_WIFI_DISCONNECT_EVENT = 8;

private static final int WIFI_EVT_START_EVENT = 100;
private static final int WIFI_EVT_CONNECTED_EVENT = 101;
private static final int WIFI_EVT_DISCONNECTED_EVENT = 102;
private static final int WIFI_EVT_GOT_IP_EVENT = 103;

private native int waitForBlufiEvent();

//public native int getCustomDataLength();

private native int  getCustomData(byte[] buffer);

private BlufiEventListener blufiEventListener;
private WifiEventListener wifiEventListener;
private boolean looping = false;

private void invcokeCustomDataReceiveEventListener(byte[] data, int length){
	if (blufiEventListener != null) {
		System.out.print("nead to set Listener");
		byte[] buf = new byte[length];
		System.arraycopy(data,0,buf,0,length);
		blufiEventListener.onCustomDataReceive(buf);
	}
}

private void invokeSsidEventListener(byte[] ssid) {
	if (blufiEventListener != null) {
		blufiEventListener.onGotWifiSsid(ssid);
	}
}

private void invokePasswordEventListener(byte[] password) {
	if (blufiEventListener != null) {
		blufiEventListener.onGotWifiPassword(password);
	}
}

protected  void setBlufiEventListener(BlufiEventListener listener) {
	blufiEventListener = listener;
}

protected void setWifiEventListener(WifiEventListener listener) {
	wifiEventListener = listener;
}

public void run() {
	looping = true;
	while(looping){
		int ret =  waitForBlufiEvent();
		switch (ret & 0xFFFF) {
			case BLUFI_EVT_CUSTOMDATA_EVENT:
				int receiveDataLength = getCustomData(buffer);
				if(receiveDataLength>0){
					invcokeCustomDataReceiveEventListener(buffer,receiveDataLength);
				}
				break;
			case BLUFI_EVT_SSID_EVENT:
				invokeSsidEventListener(BlufiServer.getWifiStationConfig().getSsid());
				break;
			case BLUFI_EVT_PASSWORD_EVENT:
				invokePasswordEventListener(BlufiServer.getWifiStationConfig().getPassword());
				break;

			case BLUFI_EVT_CONNECT_EVENT:
				if (blufiEventListener != null) {
					blufiEventListener.onBleConnect();
				}
				break;

			case BLUFI_EVT_DISCONNECT_EVENT:
				if (blufiEventListener != null) {
					blufiEventListener.onBleDisconnect();
				}
				break;

			case BLUFI_EVT_GOT_REQ_WIFI_CONNECT_EVENT:
				if (blufiEventListener != null) {
					blufiEventListener.onReqWifiConnect();
				}
				break;
			
			case BLUFI_EVT_GOT_REQ_WIFI_DISCONNECT_EVENT:
				if (blufiEventListener != null) {
					blufiEventListener.onReqWifiDisconnect();
				}
				break;

			case WIFI_EVT_START_EVENT:
				if (wifiEventListener != null) {
					wifiEventListener.onWifiStart();
				}
				break;
			case WIFI_EVT_CONNECTED_EVENT:
				if (wifiEventListener != null) {
					wifiEventListener.onWifiConnect();
				}
				break;
			case WIFI_EVT_DISCONNECTED_EVENT:
				if (wifiEventListener != null) {
					wifiEventListener.onWifiDisconnect(ret >>> 16);
				}
				break;
			case WIFI_EVT_GOT_IP_EVENT:
				if (wifiEventListener != null) {
					wifiEventListener.onWifiGotIP();
				}
				break;
			case BLUFI_EVT_ACTIVE_CLOSE_EVENT:
				looping = false;
				break;
		}
	}
}
}
