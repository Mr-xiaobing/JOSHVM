package org.joshvm.esp32.blufi;

public interface BlufiEventListener{
	void  onCustomDataReceiveEventListener(byte[] data);
}
