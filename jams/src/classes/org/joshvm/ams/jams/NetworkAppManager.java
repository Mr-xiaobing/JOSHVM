package org.joshvm.ams.jams;

import java.io.IOException;

public class NetworkAppManager implements AppManager {

	public String getAppdbNativeRoot() {
		String path = System.getProperty("org.joshvm.ams.appdbpath.native_root.secure");

		if (path == null) {
			path = new String("C:\\java\\internal\\appdb\\secure\\");
		}
		return path;
	}

	public String getAppdbRoot() {
		String path = System.getProperty("org.joshvm.ams.appdbpath.root.secure");
		if (path == null) {
			path = new String("/internal/appdb/secure/");
		}
		return path;
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
