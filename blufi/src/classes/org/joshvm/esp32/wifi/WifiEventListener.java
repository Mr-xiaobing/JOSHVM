package org.joshvm.esp32.wifi;

public interface WifiEventListener{
    /**
	 * Invoked when Wifi start
	 */
	void onWifiStart();

	/**
	 * Invoked when Wifi connected to AP
	 */
	void onWifiConnect();

	/**
	 * Invoked when Wifi disconnected from the AP
	 */
	void onWifiDisconnect(int reason);

	/**
	 * Invoked when Wifi connection established and got IP
	 */
	void onWifiGotIP();
}

