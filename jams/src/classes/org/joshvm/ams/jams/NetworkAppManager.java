package org.joshvm.ams.jams;

import java.io.IOException;

public class NetworkAppManager implements AppManager {
	
//	public String getAppdbNativeRoot() {
//		String path = System.getProperty("appdb_native_root_secure");
//		
//		if (path == null) {
//			System.out.println("getAppdbNativeRoot use default");
//			path = new String("C:\\java\\appdb\\secure\\");
//		}
//		return path;
//	}
//
//	public String getAppdbRoot() {		
//		String path = System.getProperty("appdb_root_secure");
//		if (path == null) {
//			System.out.println("getAppdbRoot use default");
//			path = new String("windows_logical_drive_C:/java/appdb/secure/");
//		}		
//		return path;
//	}
	public String getAppdbNativeRoot() {
		return "/userdata/";
	}
	
	public String getAppdbRoot() {		
		return "root/";
	}

	public void connect() throws IOException {
		// TODO Auto-generated method stub
		
	}

	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isStopped() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isForceInstall() {
		// TODO Auto-generated method stub
		return false;
	}

	public void disconnect() throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void response(String uniqueid, int code) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void response(String uniqueid, int code, String body) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public AMSMessage receiveMessage() throws IOException, WrongMessageFormatException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setCommandListener(AppManagerCommandListener listener) {
		// TODO Auto-generated method stub
		
	}

	public AppManagerCommandListener getCommandListener() {
		// TODO Auto-generated method stub
		return null;
	}

	public void notifyConnected() {
		// TODO Auto-generated method stub
		
	}

}

