package org.joshvm.ams.jams;

import java.io.IOException;

public interface AppManager {
	/*REQUEST CODE*/
	static final public int APPMAN_REQUEST_SYNC = 0;
	static final public int APPMAN_REQUEST_WAIT_COMMAND = 1;
	static final public int APPMAN_REQUEST_DOWNLOAD_APP_FILENAME = 2;
	static final public int APPMAN_REQUEST_DOWNLOAD_APP_FILELENGTH = 3;
	static final public int APPMAN_REQUEST_RUN_APP_FILENAME = 4;
	static final public int APPMAN_REQUEST_RUN_APP_MAINCLASS = 5;
	static final public int APPMAN_REQUEST_START_DOWNLOAD = 6;
	/*COMMAND CODE*/
	static final public int APPMAN_COMMAND_DOWNLOAD_JAR = 30;
	static final public int APPMAN_COMMAND_RUN_APP = 31;
	static final public int APPMAN_COMMAND_LIST_APP = 32;
	static final public int APPMAN_COMMAND_STOP_APP = 33;
	static final public int APPMAN_COMMAND_HEARTBEAT = 34;
	static final public int APPMAN_COMMAND_DELETE_APP = 35;
	static final public int APPMAN_COMMAND_RUNNING_APP = 36;
	static final public int APPMAN_COMMAND_RESET_JVM = 37;
	/*REPORT CODE*/
	static final public int APPMAN_RESPCODE_DOWNLOAD = 60;
	static final public int APPMAN_RESPCODE_APPEXIST = 61;
	static final public int APPMAN_RESPCODE_INSTALLOK = 62;	
	static final public int APPMAN_RESPCODE_APPSTARTOK = 63;
	static final public int APPMAN_RESPCODE_HEARTBEAT = 64;
	static final public int APPMAN_RESPCODE_REGISTER = 65;
	static final public int APPMAN_RESPCODE_APPLIST = 66;
	static final public int APPMAN_RESPCODE_APPFINISH = 67;
	static final public int APPMAN_RESPCODE_DELETEOK = 68;
	static final public int APPMAN_RESPCODE_RUNNINGAPPLIST = 69;
	
	/*ERROR REPORT CODE*/
	static final public int APPMAN_RESPCODE_FAIL_DOWNLOAD = 100;
	static final public int APPMAN_RESPCODE_INSTALLFAIL = 101;
	static final public int APPMAN_RESPCODE_APPSTARTERROR = 102;	
	static final public int APPMAN_RESPCODE_APPNOTEXIST = 103;
	static final public int APPMAN_RESPCODE_DELETEFAIL = 104;
	static final public int APPMAN_RESPCODE_APPNOTFINISH = 105;

	/*App Manger Event*/
	static final public int APPMAN_EVENT_APPINSTALLSTART = 10001;
	static final public int APPMAN_EVENT_APPINSTALLOK = 10002;
	static final public int APPMAN_EVENT_APPINSTALLFAIL = 10003;
	static final public int APPMAN_EVENT_APPSTART = 10004;
	static final public int APPMAN_EVENT_APPSTOP = 10005;
	static final public int APPMAN_EVENT_SERVER_ERROR = 10006;
	
	public void connect() throws IOException;
	public boolean isConnected();
	public boolean isStopped();
	public boolean isForceInstall();
	public void disconnect() throws IOException;
	public void response(String uniqueid, int code) throws IOException;
	public void response(String uniqueid, int code, String body) throws IOException;
	public AMSMessage receiveMessage() throws IOException, WrongMessageFormatException;
	public String getAppdbRoot();
	public String getAppdbNativeRoot();	
	public void setCommandListener(AppManagerCommandListener listener);
	public AppManagerCommandListener getCommandListener();
	public void notifyConnected();
}

