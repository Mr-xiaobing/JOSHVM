import java.io.UnsupportedEncodingException;

import org.joshvm.esp32.blufi.BlufiEventListener;
import org.joshvm.esp32.blufi.BlufiServer;
import org.joshvm.esp32.wifi.WifiEventListener;
import org.joshvm.esp32.wifi.WifiManager;
import org.joshvm.esp32.wifi.WifiStationConfig;

public class TestBlufi {
	
	private static byte[] receivedBlufiPassword = null;
	private static byte[] receivedBlufiSsid = null;
	
	private static Object blufiStatusLock = new Object();
	private static Object wifiStatusLock = new Object();

	public static void main(String[] args) {
		try {
			
			BlufiServer.setBlufiEventListener(new BlufiEventListener() {				
		

				public void onCustomDataReceive(byte[] data) {
					for (int i = 0; i < data.length; i++) {
						System.out.println("<info>: data[" + i + "]:" + Integer.toHexString(data[i]));
					}
					try {
						System.out.println("<info>: "+new String(data, "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					
					BlufiServer.sendCustomData("[echo]".getBytes());
					BlufiServer.sendCustomData(data);
				}

				
				public void onGotWifiPassword(byte[] password) {
					System.out.println("<info>: Received Wifi setting via blufi [password]: "+
							new String(password));
					synchronized (blufiStatusLock) {
						receivedBlufiPassword = password;
						blufiStatusLock.notifyAll();
					}
					
				}

				public void onGotWifiSsid(byte[] ssid) {
					System.out.println("<info>: Received Wifi setting via blufi [ssid]: "+
							new String(ssid));
					
					synchronized (blufiStatusLock) {
						receivedBlufiSsid = ssid;
						blufiStatusLock.notifyAll();
					}
				}

				
				public void onReqWifiConnect() {
					System.out.println("<info>: Received blufi request: connect to wifi");
				}

				public void onReqWifiDisconnect() {
					System.out.println("<info>: Received blufi request: disconnect to wifi");
				}


				public void onBleConnect() {
					System.out.println("<info>: onBleConnect");
				}


				public void onBleDisconnect() {
					System.out.println("<info>: onBleDisconnect");
				}
			});
			
			BlufiServer.setWifiEventListener(new WifiEventListener() {

				public void onWifiConnect() {
					System.out.println("<info>: onWifiConnect");
					
				}

				public void onWifiDisconnect(int reason) {
					System.out.print("<info>: onWifiDisconnect reason "+reason+": ");	
					String strReason;
					switch (reason) {
					case WifiManager.WIFI_DISCONNECT_REASON_4WAY_HANDSHAKE_TIMEOUT:
						strReason = "DISCONNECT REASON 4WAY HANDSHAKE TIMEOUT";
						break;
					case WifiManager.WIFI_DISCONNECT_REASON_ASSOC_EXPIRE:
						strReason = "DISCONNECT REASON ASSOC EXPIRE";
						break;
					case WifiManager.WIFI_DISCONNECT_REASON_AUTH_EXPIRE:
						strReason = "DISCONNECT REASON AUTH EXPIRE";
						break;
					case WifiManager.WIFI_DISCONNECT_REASON_AUTH_FAIL:
						strReason = "DISCONNECT REASON AUTH FAIL";
						break;
					case WifiManager.WIFI_DISCONNECT_REASON_HANDSHAKE_TIMEOUT:
						strReason = "DISCONNECT REASON HANDSHAKE TIMEOUT";
						break;
					case WifiManager.WIFI_DISCONNECT_REASON_NO_AP_FOUND:
						strReason = "DISCONNECT REASON NO AP FOUND";
						break;
					default:
						strReason = "DISCONNECT REASON DEFAULT";
					}
					System.out.println(strReason);
				}

				public void onWifiGotIP() {
					System.out.println("<info>: onWifiGotIP");
					synchronized(wifiStatusLock) {
						wifiStatusLock.notifyAll();
					}
				}

				public void onWifiStart() {
					System.out.println("<info>: onWifiStart");
					
				}
			
			});

			

			BlufiServer.setDeviceName("BLUFI_A1234");
			System.out.println(">>>>>>start<<<<<<");
			BlufiServer.start();
			byte[] bt_addr = new byte[6];
			BlufiServer.getBluetoothAddress(bt_addr);
			for (int i = 0 ; i < 6; i ++) {
				System.out.print(Integer.toString(bt_addr[i]&0xff, 16));
				if (i == 5) {
					System.out.println("");
				} else {
					System.out.print(":");
				}
			}
			
			System.out.println(">>>>>>>>>Now, set wifi ssid and password by your phone<<<<<<<<<");
			synchronized (blufiStatusLock) {
				while (receivedBlufiPassword == null || receivedBlufiSsid == null) {
					blufiStatusLock.wait();
				}
			}		
			
			waitUntilWifiConnected();
						
			System.out.println(">>>>>>>>>Now, disconnect BLE from your phone<<<<<<<<<");
			
			while (BlufiServer.isBleConnected()) {
				Thread.sleep(1000);
			}
			
			System.out.println("<info>: BLE disconnected. Disconnecting Wifi...");
			WifiManager wifiMgr = WifiManager.getInstance();			
			wifiMgr.disconnect();
			
			System.out.println("<info>: Wifi disconnected. Reconnecting Wifi...");
			
			WifiStationConfig blufiSetWifiConfig = new WifiStationConfig();		
			blufiSetWifiConfig.setPassword(receivedBlufiPassword);
			blufiSetWifiConfig.setSsid(receivedBlufiSsid);
			
			wifiMgr.setStationConfig(blufiSetWifiConfig);
			wifiMgr.connect();
			
			waitUntilWifiConnected();
			
			System.out.println("<info>: Connected AP:"+wifiMgr.getConnectedApName());
			
			System.out.println(">>>>>>stop<<<<<<");
			wifiMgr.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			BlufiServer.close();
		}
	}
	
	private static void waitUntilWifiConnected() {
		while (true) {
			synchronized(wifiStatusLock) {
				String state = System.getProperty("wifi.state");
				if (state.equals("3")) {
					System.out.println("<info>: wifi has connected.");
					break;
				} else {
					System.out.println("<info>: wifi is disconnected.");
				}

				try {
					wifiStatusLock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
}

