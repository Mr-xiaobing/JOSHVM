package org.joshvm.ams.jams;

import java.io.IOException;

import org.joshvm.ams.consoleams.ConnectionResetException;

public class CommAppManager extends org.joshvm.ams.consoleams.ams implements AppManager {
	protected boolean stop;
	protected AppManagerCommandListener listener;

	public CommAppManager(String port) {
		super(port);
		listener = null;
		connected = false;
		stop = false;
	}
	
	public void connect() throws IOException {
		super.connect();
		stop = false;
		startCommandListener();
	}
	public boolean isConnected() {
		return connected;
	}
	public boolean isStopped() {
		return stop;
	}
	public boolean isForceInstall() {
		return true;
	}

	public void disconnect() throws IOException {
		stop = true; 
		super.disconnect();
	}

	public void notifyConnected() {
	}
	
//	public String getAppdbNativeRoot() {
//		String path = System.getProperty("appdb_native_root_unsecure");
//		
//		if (path == null) {
//			System.out.println("getAppdbNativeRoot use default");
//			path = new String("C:\\java\\appdb\\unsecure\\");
//		}
//		return path;
//	}
//
//	public String getAppdbRoot() {		
//		String path = System.getProperty("appdb_root_unsecure");
//		if (path == null) {
//			System.out.println("getAppdbRoot use default");
//			path = new String("windows_logical_drive_C:/java/appdb/unsecure/");
//		}
//		return path;
//	}	
	
	public String getAppdbNativeRoot() {
		return "/userdata/";
	}
	
	public String getAppdbRoot() {		
		return "root/";
	}
	public void response(String uniqueid, int code) throws IOException {
		response(uniqueid, code, null);
	}
	public void response(String uniqueid, int code, String body) throws IOException {
		switch (code) {
			case AppManager.APPMAN_RESPCODE_INSTALLOK:
				console.sendReport(REPORT_FINISH_DOWNLOAD);
				break;
			case AppManager.APPMAN_RESPCODE_INSTALLFAIL:
				console.sendReport(REPORT_FAIL_DOWNLOAD);
				break;
			case AppManager.APPMAN_RESPCODE_APPLIST:
				System.out.println(body);
				console.sendReport(REPORT_FINISH_LISTAPP);
				break;
			case AppManager.APPMAN_RESPCODE_DELETEOK:
				console.sendReport(REPORT_FINISH_ERASEAPP);
				break;
			case AppManager.APPMAN_RESPCODE_APPFINISH:
				console.sendReport(REPORT_FINISH_RUNAPP);
				break;
			case AppManager.APPMAN_RESPCODE_APPSTARTOK:
				break;
			default:
				console.sendReport(REPORT_GENERAL_FAIL);
		}
	}
	public AMSMessage receiveMessage() throws IOException, WrongMessageFormatException {
		AMSMessage msg = null;

		try {
			int command = waitForCommand();
		
			switch (command) {
				case COMMAND_DOWNLOAD_JAR:
					msg = downloadInfo();
					break;
				case COMMAND_RUN_APP:
					msg = runningInfo();
					break;
				case COMMAND_LIST_APP:
					msg = appListInfo();
					break;
				case COMMAND_ERASE_APP:
					msg = removeAppInfo();
					break;
				case COMMAND_STOP_APP:
					break;
				default:
			}
		} catch (ConnectionResetException e) {
			throw new IOException(e.toString());
		}

		return msg;
	}

	public void setCommandListener(AppManagerCommandListener listener) {
		this.listener = listener;
	}

	public AppManagerCommandListener getCommandListener() {
		return listener;
	}

	private void startCommandListener() {
		new Thread(new AMSCommandDispacher(this)).start();
	}

	private AMSMessage downloadInfo() throws IOException, ConnectionResetException, WrongMessageFormatException {
		String appJarFile = getDownloadAppName();
		int filebytes = getDownloadAppLength();

		return new AMSMessage("[DOWNLAPP]UNIQUEID=0000000000000000,APPNAME="+appJarFile+
			",APPURL=comm:COM0,AUTOSTART=0,STARTNOW=0,MAINCLASS=dummy,APPLEN="+filebytes);
	}

	private AMSMessage appListInfo() throws IOException, ConnectionResetException, WrongMessageFormatException {
		return new AMSMessage("[RLISTAPP]UNIQUEID=0000000000000000");
	}

	private AMSMessage removeAppInfo() throws IOException, ConnectionResetException, WrongMessageFormatException {
		String appJarFile = getDownloadAppName();

		return new AMSMessage("[RDELEAPP]UNIQUEID=0000000000000000,APPNAME="+appJarFile);
	}

	private AMSMessage runningInfo() throws IOException, ConnectionResetException, WrongMessageFormatException {
		String appJarFile = getRunAppJarfile();
		String appName = getRunAppMainClass();

		return new AMSMessage("[STARTAPP]UNIQUEID=0000000000000000,APPNAME="+appJarFile+
			", MAINCLASS="+appName);
	}

}

