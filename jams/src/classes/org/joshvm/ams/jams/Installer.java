package org.joshvm.ams.jams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

public abstract class Installer {
	protected String installSource;
	protected String installDest;
	
	public Installer (String installSource) {
		this.installSource = installSource;
	}
	
	public void install(String appName, String mainClass, int length, boolean autoStart) throws IOException, InstallVerifyErrorException {
		installDest = "file:///"+Jams.getAppdbRoot()+appName+".jar";
		FileConnection fconn = null;
		InputStream is;
		OutputStream file_os = null;
		try {
			System.out.println("Open file for write:"+installDest);
			fconn = (FileConnection)Connector.open(installDest);
			if (fconn.exists()) {
				fconn.delete();
			}
			fconn.create();
			
			is = getSourceStream();
			int left;
			/*
			left = (is.read()&0xff)<<24;
			left |= (is.read()&0xff)<<16;
			left |= (is.read()&0xff)<<8;
			left |= (is.read()&0xff);
			*/
			left = length;
			System.out.println("Expected download size:"+left);
			file_os = fconn.openOutputStream();
	        final int MAX_LENGTH = 512;
	        byte[] buf = new byte[MAX_LENGTH];
			boolean eof = false;
			System.out.println("Start reading Jar from website...");
			while (!eof) {
	        	int total = 0;
				int size;
				if (left < MAX_LENGTH) {
					size = left;
				} else {
					size = MAX_LENGTH;
				}
		        while ((total < size) && !eof) {
					
	                int count = is.read(buf, total, size - total);
	                if (count < 0) {
						System.out.println("Read EOF!!!");
						eof = true;	                    
	                } else if (count > 0) {
	                	System.out.println("Read bytes number = "+count);
	                	total += count;
						left -= count;
						if (left <= 0) {
							eof = true;
						}
	                }
		        }
				file_os.write(buf, 0, total);
				System.out.println("Total left bytes to write:"+left);
			}
			
			setAutoStart(appName, mainClass, autoStart);
		}finally {			
			if (file_os != null) {
				file_os.close();
			}
			if (fconn != null) {
				fconn.close();
			}
		}
	}

	protected abstract InputStream getSourceStream() throws IOException;

	private void setAutoStart(String appName, String mainClass, boolean autoStart) throws IOException {
		FileConnection fconn = (FileConnection)Connector.open("file:///"+Jams.getAppdbRoot()+appName+".aut");
		boolean exist = fconn.exists();
		if (autoStart) {
			if (!exist) {
				fconn.create();
			}
			OutputStream os = fconn.openOutputStream();
			os.write(mainClass.getBytes());
			os.close();
		} else {
			if (exist) {
				fconn.delete();
			}
		}
		fconn.close();
	}
}

