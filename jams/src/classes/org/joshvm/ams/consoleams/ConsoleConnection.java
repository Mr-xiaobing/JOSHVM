package org.joshvm.ams.consoleams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class ConsoleConnection {
	private static final byte[] MAGIC = {(byte)0xfe, (byte)0xaa, (byte)0xbc, (byte)0xef};
	OutputStream os;
	InputStream is;

	public void open (OutputStream os, InputStream is) {
		this.os = os;
		this.is = is;
	}

	public void close() {
		os = null;
		is = null;
	}
	
	public String receiveString() throws IOException  {
		byte[] buf = receivePacket();
		if ((buf == null) || (buf.length == 0)) {
			throw new IOException();
		}
		return new String(buf);
	}

	public int receiveInt() throws IOException  {
		byte[] buf = receivePacket();
		if (buf.length != 4) {
			throw new IOException();
		}
		int n = 0;
		for (int i = 0; i < 4; i++) {
			n = n << 8;
			n |= (int)buf[i] & 0xff;			
			System.out.println("buf["+i+"]="+(buf[i]&0xff)+", n="+n);
		} 
		System.out.println("receiveInt():"+n);
		return n;
	}

	public int read(byte b[], int off, int len) throws IOException {
		for (int i = 0; i < len; i++) {
			b[off+i] = readByteBlocking();
		}
		return len;
	}

	public void sendRequest(int request_id) throws IOException {
		sendMagic();
		os.write(request_id);		
	}
	
	public void sendReport(int report_id) throws IOException {
		sendMagic();
		os.write(report_id);
	}
	
	public byte[] receivePacket() throws IOException {
		int len;
		len = readByteBlocking();
		len = len & 0xff;
		byte[] buf = new byte[len];
		for (int i = 0; i < len; i++) {
			buf[i] = readByteBlocking();
		}
		return buf;
	}



	private void sendMagic() throws IOException {
		os.write(MAGIC);
	}

	private byte readByteBlocking() throws IOException {
		int i;
		int retry = 2;
		while (retry -- > 0 && is.available() == 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		if (retry <= 0) {
			throw new IOException("Connection timeout");
		}

		i = is.read();

		return (byte)(i & 0xff);
	}
	
}

