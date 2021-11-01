package com.joshvm.ams.network;

import org.joshvm.ams.jams.Jams;
import org.joshvm.esp32.blufi.BluetoothUUID;
import org.joshvm.esp32.blufi.BlufiEventListener;
import org.joshvm.esp32.blufi.BlufiServer;
import org.joshvm.esp32.wifi.WifiEventListener;
import org.joshvm.esp32.wifi.WifiManager;
import org.joshvm.esp32.wifi.WifiStationConfig;

import com.joshvm.ams.file.FileManager;
import com.joshvm.ams.timeout.TimeOutCallback;
import com.joshvm.ams.timeout.Timeouts;
import com.joshvm.ams.util.Utils;
/**
 * 配网配蓝牙管理类
 * @author 86188
 *
 */
public class NetManager {

	private static final int CONFIGET_FAIL = 1;
	private static final int CONFIGET_TIMEOUT = 2;
	private static final int CONFIGET_WIFIINFO_ERROR = 3;
	private static Timeouts timeouts;

	/**
	 * 配网
	 * 
	 * @param ssid
	 * @param password
	 */
	public static void blufiConfiger(final ConfigerWifiCallBack configerWifiCallBack) {

		System.out.println("==jams====setWifiEventListener====");

		timeouts = new Timeouts();
		timeouts.setOutTime(10);
		timeouts.setCallback(new TimeOutCallback() {
			public void timeOut() {
				configerWifiCallBack.fail(CONFIGET_TIMEOUT);

			}
		});
		timeouts.startTimer();

		BlufiServer.setWifiEventListener(new WifiEventListener() {

			public void onWifiConnect() {
				System.out.println("<info>: onWifiConnect");

			}

			public void onWifiDisconnect(int reason) {
				System.out.println("<info>: onWifiDisconnect" + reason);

				if (timeouts.isTiming()) {
					configerWifiCallBack.fail(CONFIGET_FAIL);

					timeouts.dismiss();
				}

			}

			public void onWifiGotIP() {
				System.out.println("<info>: onWifiGotIP");
				if (timeouts.isTiming()) {
					configerWifiCallBack.success();
					timeouts.dismiss();

				}
			}

			public void onWifiStart() {
				System.out.println("<info>: onWifiStart");

			}

		});
		try {
			StringBuffer stringBuffer = new StringBuffer();
			byte[] bt_addr = new byte[6];
			BlufiServer.getBluetoothAddress(bt_addr);
			for (int i = 0; i < 6; i++) {
				stringBuffer.append(Integer.toString(bt_addr[i] & 0xff, 16));
				if (i == 5) {
					stringBuffer.append("");
				} else {
					stringBuffer.append(":");
				}
			}

			Jams.DEVICE_MAC_ADDRESS = stringBuffer.toString();

			System.out.println("=====MAC_ADDRESS======" + Jams.DEVICE_MAC_ADDRESS);

			String bluMac = (Integer.toString(bt_addr[4] & 0xff, 16).charAt(1)
					+ Integer.toString(bt_addr[5] & 0xff, 16)).toUpperCase();

			String deviceInfo = FileManager.getFileData(Jams.DEVICE_INFO);

			if (deviceInfo == null && deviceInfo.equals("")) {

				throw new Exception("UNKNOW DEVICE TYPE");

			}

			String[] deviceInfoArray = Utils.slipString(deviceInfo, Jams.BLUFI_CMD_DIVISION, 2);

			if (deviceInfoArray.length < 2) {

				throw new Exception("UNKNOW DEVICE TYPE");
			}

			String bluName = deviceInfoArray[1];
			BlufiServer.setDeviceName(bluName + " " + bluMac);
			
			// 设置设备蓝牙名称
			if (bluName.equals("MiaoBPM")) {
				BlufiServer.setServiceUuid(new BluetoothUUID(0x1810));
			} else if (bluName.equals("MiaoBSM")) {
				BlufiServer.setServiceUuid(new BluetoothUUID(0x1808));
			}
			BlufiServer.setManufacturerData(bt_addr);

			if (BlufiServer.getPduSize() <= 31) {

				BlufiServer.start();
			} else {
				throw new Exception("PDU size too large");
			}

			String wifiInfo = FileManager.checkFile(Jams.WIFI_INFO);

			if (wifiInfo != null && !wifiInfo.equals("")) {

				String[] wifiInfoArray = Utils.slipString(wifiInfo, Jams.BLUFI_CMD_DIVISION, 2);
				if (wifiInfoArray.length > 1) {

					System.out.println("==blufiConfiger====ssid:::" + wifiInfoArray[0] + "=========password:::"
							+ wifiInfoArray[1]);

					WifiManager wifiMgr = WifiManager.getInstance();
					WifiStationConfig blufiSetWifiConfig = new WifiStationConfig();
					blufiSetWifiConfig.setSsid(wifiInfoArray[0].getBytes());
					blufiSetWifiConfig.setPassword(wifiInfoArray[1].getBytes());
					wifiMgr.setStationConfig(blufiSetWifiConfig);
					wifiMgr.connect();

				} else {

					if (timeouts.isTiming()) {
						configerWifiCallBack.fail(CONFIGET_WIFIINFO_ERROR);

						timeouts.dismiss();
					}

				}
			} else {
				if (timeouts.isTiming()) {
					configerWifiCallBack.fail(CONFIGET_WIFIINFO_ERROR);

					timeouts.dismiss();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
