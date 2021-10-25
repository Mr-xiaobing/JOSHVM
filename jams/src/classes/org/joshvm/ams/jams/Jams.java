package org.joshvm.ams.jams;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.file.FileConnection;

import org.json.me.JSONArray;
import org.json.me.JSONObject;

import com.joshvm.ams.file.FileManager;
import com.joshvm.ams.http.HttpManager;
import com.joshvm.ams.http.adapter.CheckAppListAdapter;
import com.joshvm.ams.http.callback.AppCheckCallback;
import com.joshvm.ams.network.ConfigerWifiCallBack;
import com.joshvm.ams.network.NetManager;

import com.sun.cldc.isolate.*;

public class Jams implements AppManagerCommandListener {
	private static final String APPMANAGER_COMM_PORT = "COM0";
	private static final int APPMANAGER_TYPE_COMM = 1;
	private static final int APPMANAGER_TYPE_NETWORK = 2;
	private static final boolean ifConnectAtStart = false;
	private static Isolate runningIsolate;
	private static String stoppingUniqueID;

	private static AppManager appman = null;

	// &&
	public static final String BLUFI_CMD_DIVISION = "&&";
	// wifi配置
	public static final String WIFI_INFO = "wifiInfo.txt";
	// 蓝牙名
	public static final String BLU_NAME = "bluName.txt";
	// 蓝牙mac地址
	public static String DEVICE_MAC_ADDRESS = "";

	private Jams(int type) {
		if (type == APPMANAGER_TYPE_COMM) {
			appman = new CommAppManager(APPMANAGER_COMM_PORT);
		}
		if (type == APPMANAGER_TYPE_NETWORK) {
			appman = new NetworkAppManager();
		}

		stoppingUniqueID = null;
	}

	private Installer getInstaller(String installSourceURL) {
		if (installSourceURL.startsWith("file://")) {
			return new FileInstaller(installSourceURL);
		} else if (installSourceURL.startsWith("comm:")) {
			return new CommInstaller(installSourceURL);
		} else if (installSourceURL.startsWith("socket://")) {
			return new NetworkInstaller(installSourceURL.substring(9));
		} else {
			return null;
		}
	}

	public static void main(String argv[]) {

		// Try Comm App Manager
		Jams ams = new Jams(APPMANAGER_TYPE_COMM);
		AppManager appman = ams.appman;
		appman.setCommandListener(ams);
		try {
			appman.connect();
		} catch (IOException e) {
			System.out.println("App Manager cannot connect to COMM port");
		}

		if (!appman.isConnected()) {
			appman.setCommandListener(null);
			ams = new Jams(APPMANAGER_TYPE_NETWORK);
			appman = ams.appman;

			NetManager.blufiConfiger(new ConfigerWifiCallBack() {

				public void success() {
					new Thread(new Runnable() {

						public void run() {
							/* 检查应用请求 */
							HttpManager.executeRequest(new CheckAppListAdapter(new AppCheckCallback() {

								public void response(String resp) {

									try {
										JSONObject jsonObject = new JSONObject(resp);

										int status = jsonObject.getInt("status");

										if (status == 200) {

											JSONObject jsonData = jsonObject.getJSONObject("data");
											JSONArray installArray = jsonData.getJSONArray("installList");
											JSONArray updateArray = jsonData.getJSONArray("updateList");
											JSONArray uninstallArray = jsonData.getJSONArray("uninstallList");

											/**
											 * 安装应用
											 */
											if (installArray != null && installArray.length() > 0) {

												for (int i = 0; i < installArray.length(); i++) {

													JSONObject jsonObjectInstall = installArray.getJSONObject(i);

													String url = jsonObjectInstall.getString("url");
													String md5 = jsonObjectInstall.getString("md5");
													String mainClass = jsonObjectInstall.getString("mainName");
													int size = jsonObjectInstall.getInt("size");

													Installer inst = new NetworkInstaller(url);

													try {
														inst.install(md5, mainClass, size, true);

													} catch (Exception e) {
														e.printStackTrace();
													}
												}

											}

											/**
											 * 更新应用
											 */
											if (updateArray != null && updateArray.length() > 0) {

												for (int i = 0; i < updateArray.length(); i++) {
													JSONObject jsonObjectUpdate = updateArray.getJSONObject(i);

													String url = jsonObjectUpdate.getString("url");
													String appNameCurrent = jsonObjectUpdate.getString("currentMd5");
													String appNameUpdate = jsonObjectUpdate.getString("updateMd5");
													String mainClass = jsonObjectUpdate.getString("mainName");
													int size = jsonObjectUpdate.getInt("size");
													Installer inst = new NetworkInstaller(url);

													try {
														inst.install(appNameUpdate, mainClass, size, true);
													} catch (Exception e) {
														e.printStackTrace();
													}

													FileManager.removeApp(appNameCurrent);
												}
											}

											/**
											 * 卸载应用
											 */
											if (uninstallArray != null && uninstallArray.length() > 0) {

												for (int i = 0; i < uninstallArray.length(); i++) {

													FileManager.removeApp(uninstallArray.getString(i));
												}
											}

										}

									} catch (Exception e) {
										e.printStackTrace();
									}

									autoStartAll();

									FileManager.getFilesName();
								}

								public void failure(int httpCode) {
									autoStartAll();

								}

								public void timeout() {
									autoStartAll();

								}
							}));

						}
					}).start();

				}

				public void fail(int code) {
					autoStartAll();

				}

			});

		}

	}

	synchronized public void commandStartApp(String uniqueID, String appName, String mainClass) {
		startApp(uniqueID, appName, mainClass);
	}

	synchronized public void commandRemoveApp(String uniqueID, String appName) {
		if (runningIsolate != null) {
			try {
				appman.response(uniqueID, AppManager.APPMAN_RESPCODE_APPNOTFINISH);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}

		if (!isInstalled(appName)) {
			try {
				appman.response(uniqueID, AppManager.APPMAN_RESPCODE_APPNOTEXIST);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}

		FileConnection fconn = null;
		String filename = "file:///" + Jams.getAppdbRoot() + appName;
		try {
			fconn = (FileConnection) Connector.open(filename + ".jar");
			fconn.delete();
			appman.response(uniqueID, AppManager.APPMAN_RESPCODE_DELETEOK);
		} catch (IOException ioe) {
			try {
				appman.response(uniqueID, AppManager.APPMAN_RESPCODE_DELETEFAIL);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} finally {
			if (fconn != null) {
				try {
					fconn.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}

		fconn = null;

		try {
			fconn = (FileConnection) Connector.open(filename + ".aut");
			if (fconn.exists()) {
				fconn.delete();
			}
		} catch (IOException ex) {
		} finally {
			if (fconn != null) {
				try {
					fconn.close();
				} catch (IOException ex) {
				}
			}
		}
	}

	synchronized public void commandStopApp(String uniqueID) {
		if (runningIsolate != null) {
			stoppingUniqueID = uniqueID;
			runningIsolate.exit(0);
		} else {
			try {
				appman.response(uniqueID, AppManager.APPMAN_RESPCODE_APPFINISH, String.valueOf(0));
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	synchronized public void commandRestartApp(String uniqueID) {
	}

	synchronized public void commandHeartbeat(String uniqueID) {
		try {
			appman.notifyConnected();
			appman.response(uniqueID, AppManager.APPMAN_RESPCODE_HEARTBEAT);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	synchronized public void commandInstallApp(boolean forceInstall, String uniqueID, String appName, String mainClass,
			String installSource, int length, boolean autoStart, boolean startAfterInstall) {
		try {
			if (!forceInstall && isInstalled(appName)) {
				appman.response(uniqueID, AppManager.APPMAN_RESPCODE_APPEXIST);
				if (startAfterInstall) {
					startApp(uniqueID, appName, mainClass);
				}
			} else {
				Installer inst = getInstaller(installSource);

				try {
					inst.install(appName, mainClass, length, autoStart);
					appman.response(uniqueID, AppManager.APPMAN_RESPCODE_INSTALLOK);

					if (startAfterInstall) {
						startApp(uniqueID, appName, mainClass);
					}
				} catch (Exception e) {
					e.printStackTrace();
					appman.response(uniqueID, AppManager.APPMAN_RESPCODE_INSTALLFAIL);
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	synchronized public void commandListInstalledApp(String uniqueID) {
		String filepath = "file:///" + Jams.getAppdbRoot();
		StringBuffer applist = new StringBuffer();
		try {
			FileConnection fconn = (FileConnection) Connector.open(filepath);
			java.util.Enumeration em = fconn.list();
			System.out.println("File open path:" + filepath);

			while (em.hasMoreElements()) {
				String filename = (String) em.nextElement();
				appendAppNameToList(applist, filename);
			}

			int length = applist.length();
			if (length > 0) {
				applist.setLength(length - 1); // Remove the last ";"
			} else {
				System.out.println("EMPTY_LIST");
				applist.append("EMPTY_LIST");
			}

			appman.response(uniqueID, AppManager.APPMAN_RESPCODE_APPLIST, applist.toString());

			fconn.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	synchronized public void commandListRunningApp(String uniqueID) {
		String applist;

		if (runningIsolate != null) {
			String cp = runningIsolate.getClassPath()[0];

			int sep = cp.lastIndexOf('/');
			if (sep == -1) {
				sep = cp.lastIndexOf('\\');
			}
			sep++; // if sep == -1, then sep = 0

			if (cp.endsWith(".jar")) {
				applist = cp.substring(sep, cp.length() - 4);
			} else {
				applist = cp.substring(sep);
			}
		} else {
			applist = new String("EMPTY_LIST");
		}

		try {
			appman.response(uniqueID, AppManager.APPMAN_RESPCODE_RUNNINGAPPLIST, applist.toString());
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	synchronized public void commandGetAppInfo(String uniqueID, String appName) {
	}

	synchronized public void commandResetJVM(String uniqueID) {
		System.out.println("=============Reset now by server command=============");
		System.getProperty("system.hint.powerreset");
	}

	private static void startApp(final String uniqueID, final String appName, final String mainClass) {
		if (runningIsolate != null) {
			System.out.println("[startApp]There's another application running. Failed to start: " + appName);
			try {
				appman.response(uniqueID, AppManager.APPMAN_RESPCODE_APPNOTFINISH);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}

		new Thread(new Runnable() {
			public void run() {
				try {
					System.out.println("New Isolate: " + mainClass + "," + appName + ".jar");
					if (!isInstalled(appName)) {
						throw new ClassNotFoundException();
					}
					Isolate iso = new Isolate(mainClass, new String[0],
							new String[] { getAppdbNativeRoot() + appName + ".jar" });
					System.out.println("New Isolate: " + appName + " about to start");
					iso.start();
					System.out.println("New Isolate: " + appName + " started, waiting for exit...");
					try {
						appman.response(uniqueID, AppManager.APPMAN_RESPCODE_APPSTARTOK);
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
					runningIsolate = iso;
					stoppingUniqueID = null;
					iso.waitForExit();
					int exitcode = iso.exitCode();
					System.out.println("Isolate: " + appName + " exit successfully with code:" + exitcode);
					try {
						appman.response(stoppingUniqueID != null ? stoppingUniqueID : uniqueID,
								AppManager.APPMAN_RESPCODE_APPFINISH, String.valueOf(exitcode));
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				} catch (IsolateStartupException ise) {
					ise.printStackTrace();
					try {
						appman.response(uniqueID, AppManager.APPMAN_RESPCODE_APPSTARTERROR);
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				} catch (ClassNotFoundException cnfe) {

					cnfe.printStackTrace();
					try {
						appman.response(uniqueID, AppManager.APPMAN_RESPCODE_APPNOTEXIST);
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				} finally {
					runningIsolate = null;
				}
			}
		}).start();
	}

	public static String getAppdbNativeRoot() {
		String path = appman.getAppdbNativeRoot();
		System.out.println("appdb_native_root=" + path);
		return path;
	}

	public static String getAppdbRoot() {
		String path = appman.getAppdbRoot();
		System.out.println("appdb_root=" + path);
		return path;
	}

	private StreamConnection getCommConnection() {
		return null;
	}

	private StreamConnection getLocalHostConnection() {
		return null;
	}

	private static void autoStartAll() {
		String filepath = "file:///" + Jams.getAppdbRoot();
		System.out.println("Try to find auto-start application...");
		try {
			FileConnection fconn = (FileConnection) Connector.open(filepath);
			java.util.Enumeration em = fconn.list();
			while (em.hasMoreElements()) {
				String filename = (String) em.nextElement();
				if (filename.endsWith(".aut")) {
					FileConnection asfile = (FileConnection) Connector.open(filepath + filename);

					String appname = filename.substring(0, filename.length() - 4);
					if (!isInstalled(appname)) {
						asfile.delete();
					} else {

						InputStream in = asfile.openInputStream();
						byte[] buf = new byte[64];
						int len = in.read(buf);
						in.close();

						String mainclass = new String(buf, 0, len);
						System.out.println("Find auto start application: " + appname);
						startApp(null, appname, mainclass);
					}
					asfile.close();
				}
			}
			fconn.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private static boolean isInstalled(String appname) {
		boolean result;
		String filepath = "file:///" + Jams.getAppdbRoot() + appname + ".jar";
		try {
			FileConnection fconn = (FileConnection) Connector.open(filepath);
			if (fconn.exists()) {
				result = true;
			} else {
				result = false;
			}

			fconn.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			result = false;
		}
		return result;
	}

	private static boolean isAutostart(String appname) {
		boolean result;
		String filepath = "file:///" + Jams.getAppdbRoot() + appname + ".aut";
		try {
			FileConnection fconn = (FileConnection) Connector.open(filepath);
			if (fconn.exists()) {
				result = true;
			} else {
				result = false;
			}

			fconn.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			result = false;
		}
		return result;
	}

	private static void appendAppNameToList(StringBuffer applist, String filename) {
		if (filename.endsWith(".jar")) {
			String appname = filename.substring(0, filename.length() - 4);
			applist.append(appname);
			applist.append("#AUTOSTART=");
			if (isAutostart(appname)) {
				applist.append("1");
			} else {
				applist.append("0");
			}
			applist.append(";");
		}
	}

	public void event(int event_code, int arg) {
		return;
	}
}
