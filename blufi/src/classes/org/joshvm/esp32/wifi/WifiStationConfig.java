package org.joshvm.esp32.wifi;

public class WifiStationConfig {
    private byte[] ssid;
    private byte[] password;

    /**
     * Default constructor
     */
    public WifiStationConfig() {
        ssid = null;
        password = null;
    }

    /**
     * Constructor. The length of SSID and password equal to the corresponding buffer's length
     * @param ssid byte buffer contains SSID of the AP
     * @param password byte buffer contains password of the AP
     */
    public WifiStationConfig(byte[] ssid, byte[] password) {
        this.ssid = ssid;
        this.password = password;
    }

    /**
     * Set SSID of the configuration
     * @param ssid byte buffer of ssid. The length of the buffer equals to ssid's
     */
    public void setSsid(byte[] ssid) { this.ssid = ssid;}

    /**
     * Set password of the configuration
     * @param password byte buffer of ssid. The length of the buffer equals to password's
     */
    public void setPassword(byte[] password) {this.password = password;}

    /**
     * Getter of configuation's SSID
     * @return SSID byte buffer. The length of the buffer equals to ssid's
     */
    public byte[] getSsid() {return ssid;}

    /**
     * Getter of configuration's password
     * @return password byte buffer. The length of the buffer equals to password's
     */
    public byte[] getPassword() {return password;}
}
