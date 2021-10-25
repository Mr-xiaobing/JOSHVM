package org.joshvm.ams.jams;

public interface AppManagerCommandListener {
	public void commandInstallApp(boolean forceInstall, String uniqueID, String appName, String mainClass, String installSource, int length, boolean autoStart, boolean startAfterInstall);
	public void commandRemoveApp(String uniqueID, String appName);
	public void commandStopApp(String uniqueID);
	public void commandStartApp(String uniqueID, String appName, String mainClass);
	public void commandRestartApp(String uniqueID);
	public void commandListInstalledApp(String uniqueID);
	public void commandListRunningApp(String uniqueID);
	public void commandGetAppInfo(String uniqueID, String appName);
	public void commandHeartbeat(String uniqueID);
	public void commandResetJVM(String uniqueID);
}

