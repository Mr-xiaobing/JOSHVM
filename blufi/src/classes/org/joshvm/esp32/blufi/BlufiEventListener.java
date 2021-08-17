package org.joshvm.esp32.blufi;

public interface BlufiEventListener{
	/**
	 * Invoked when BLE connection established
	 */
	void onBleConnect();

	/**
	 * Invoked when BLE disconnected
	 */
	void onBleDisconnect();

	/**
	 * Invoked when blufi custom data received
	 * @param data the received data buffer. Received data length equals to data.length
	 */
	void onCustomDataReceive(byte[] data);

	/**
	 * Invoked when blufi connection create
	 */
	void onReqWifiConnect();

	/**
	 * Invoked when blufi connection disconnected
	 */
	void onReqWifiDisconnect();

	/**
	 * Invoked when Wifi SSID received via blufi
	 * @param ssid buffer contains the received SSID. Length of the SSID equals to ssid.length
	 */
	void onGotWifiSsid(byte[] ssid);

	/**
	 * Invoked when Wifi password received via blufi
	 * @param password buffer contains the received password. Length of the password equals to password.length
	 */
	void onGotWifiPassword(byte[] password);

}
