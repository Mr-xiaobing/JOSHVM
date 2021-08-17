package org.joshvm.esp32.wifi;

import java.io.UnsupportedEncodingException;

public class WifiManager {
    public final static int WIFI_DISCONNECT_REASON_DEFAULT = 0;
    public final static int WIFI_DISCONNECT_REASON_AUTH_EXPIRE = 1;
    public final static int WIFI_DISCONNECT_REASON_4WAY_HANDSHAKE_TIMEOUT = 2;
    public final static int WIFI_DISCONNECT_REASON_AUTH_FAIL = 3;
    public final static int WIFI_DISCONNECT_REASON_ASSOC_EXPIRE = 4;
    public final static int WIFI_DISCONNECT_REASON_HANDSHAKE_TIMEOUT = 5;
    public final static int WIFI_DISCONNECT_REASON_NO_AP_FOUND = 6;

    private static WifiManager manager = null;

    private WifiManager() {};

    /**
     * Get device-wide unique WifiManager instance
     * @return
     */
    public static synchronized WifiManager getInstance() {
        if (manager == null) {
            manager = new WifiManager();
        }

        return manager;
    }

    /**
     * Return connected AP name as String
     * @return the AP name to which is currently connected
     */
    public String getConnectedApName() {
        int len = -1;
        byte[] name = new byte[64];
        if ((len = getConnectedApName0(name)) > 0) {
            try {
                return new String(name, 0, len, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return new String(name, 0, len);
            }
        } else {
            return null;
        }
    }

    /**
     * Set Wifi ssid and password that are used to connect to AP
     * @param config the configuation contains SSID and password to set for Wifi Station mode
     */
    public void setStationConfig(WifiStationConfig config) {
        setStationConfig0(config.getSsid(), config.getPassword());
    }

    public void connect() {
        connect0();
    }

    public void disconnect() {
        disconnect0();
    }

    private native void connect0();
    private native void disconnect0();

    /**
     * Return connected AP name as String
     * @param name byte buffer to contain the connected AP name in UTF-8 encoding
     * @return >0 if currently connected to AP and the AP's name in @name
     *         otherwise if not currently connected to any AP
     */
    private native int getConnectedApName0(byte[] name);
    
    /**
     * Set Wifi ssid and password that are used to connect to AP
     * @param ssid ssid to connect
     * @param password password to connect
     */
    private native void setStationConfig0(byte[] ssid, byte[] password);
}
