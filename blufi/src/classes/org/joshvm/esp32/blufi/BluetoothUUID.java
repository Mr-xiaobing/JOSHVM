package org.joshvm.esp32.blufi;

public final class BluetoothUUID {
    private final byte[] DEFAULT = {(byte)0xfb, (byte)0x34, (byte)0x9b, (byte)0x5f, 
                                    (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x80, 
                                    (byte)0x00, (byte)0x10, (byte)0x00, (byte)0x00, 
                                    (byte)0xFF, (byte)0xFF, (byte)0x00, (byte)0x00};
    private byte[] buffer;
    private int short_uuid;

    public BluetoothUUID(int uuid) {
        short_uuid = uuid;
        buffer = null;
    }

    public BluetoothUUID(byte[] uuid) throws IllegalArgumentException {
        if (uuid == null || uuid.length != 16) {
            throw new IllegalArgumentException();
        }
        buffer = new byte[uuid.length];
        System.arraycopy(uuid, 0, buffer, 0, uuid.length);
        short_uuid = (uuid[12] & 0xff) | ((uuid[13] << 8) & 0xff00) |
                     ((uuid[14] << 16) & 0xff0000) | ((uuid[15] << 24) & 0xff000000);
    }
    
    public byte[] getUUID() {
        if (buffer == null) {
            buffer = new byte[DEFAULT.length];
            System.arraycopy(DEFAULT, 0, buffer, 0, DEFAULT.length);
            buffer[12] = (byte)(short_uuid & 0xff);
            buffer[13] = (byte)((short_uuid >>  8) & 0xff);
            buffer[14] = (byte)((short_uuid >> 16) & 0xff);
            buffer[15] = (byte)((short_uuid >> 24) & 0xff);
        }
        return buffer;
    }

    public int getShortUUID() {
        return short_uuid;
    }

    protected boolean isShortType() {
        return buffer == null;
    }

    protected int getPDUSize() {
        if (isShortType()) {
            if ((short_uuid & 0xFFFF0000) == 0) {
                //16bit UUID
                return 2;
            } else {
                if (((short_uuid & 0xFF000000) == 0) || 
                    ((short_uuid & 0x00FF0000) == 0)) {                    
                    return 16;
                } else {
                    //32bit UUID
                    return 4;
                }
            }
        } else {
            int match = 0;
            for (int i = 0 ; i < DEFAULT.length; i++) {
                if (i == 12 || i == 13) {
                    continue;
                }

                if (buffer[i] == DEFAULT[i]) {
                    match++;
                }
            }

            if (match == 12) {
                return 4;
            } else if (match == 14) {
                return 2;
            } else {
                return 16;
            }
        }
    }
}
