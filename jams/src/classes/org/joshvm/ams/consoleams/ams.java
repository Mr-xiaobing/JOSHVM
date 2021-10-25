package org.joshvm.ams.consoleams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.file.FileConnection;

import com.sun.cldc.isolate.*;

public class ams {
	/*REQUEST CODE*/
	static final public int REQUEST_SYNC = 0;
	static final public int REQUEST_WAIT_COMMAND = 1;
	static final public int REQUEST_DOWNLOAD_APP_FILENAME = 2;
	static final public int REQUEST_DOWNLOAD_APP_FILELENGTH = 3;
	static final public int REQUEST_RUN_APP_FILENAME = 4;
	static final public int REQUEST_RUN_APP_MAINCLASS = 5;
	static final public int REQUEST_START_DOWNLOAD = 6;
	/*COMMAND CODE*/
	static final public int COMMAND_DOWNLOAD_JAR = 30;
	static final public int 	COMMAND_RUN_APP = 31;
	static final public int 	COMMAND_LIST_APP = 32;
	static final public int 	COMMAND_STOP_APP = 33;
	static final public int COMMAND_ERASE_APP = 35;
	/*REPORT CODE*/
	static final public int REPORT_FINISH_DOWNLOAD = 60;
	static final public int REPORT_FINISH_LISTAPP = 61;
	static final public int REPORT_FINISH_RUNAPP = 62;
	static final public int REPORT_FINISH_ERASEAPP = 63;
	/*ERROR REPORT CODE*/
	static final public int REPORT_FAIL_DOWNLOAD = 100;
	static final public int REPORT_GENERAL_FAIL = 255;

	static final public int SYNC_CODE = 0x0c0ffee0;

	static final public String APPDB_PATH_ROOT = "file:///Phone/";
	
	StreamConnection sc;
	OutputStream com_os;
	InputStream com_is;
	String commport;
	
	protected boolean connected;		
	protected ConsoleConnection console;

	protected ams(String comm_port) {
		connected = false;
		console = new ConsoleConnection();
		commport = comm_port;
	}


	protected String getRunAppJarfile() throws IOException, ConnectionResetException {
		console.sendRequest(REQUEST_RUN_APP_FILENAME);
		System.out.println("Waiting for running app jar file...");
		return console.receiveString();
	}	

	protected String getRunAppMainClass() throws IOException, ConnectionResetException {
		console.sendRequest(REQUEST_RUN_APP_MAINCLASS);
		System.out.println("Waiting for running app main class name...");
		return console.receiveString();
	}
		
	protected void runApp()  throws IOException, ConnectionResetException {
		Isolate iso;
		String appName;
		String appJarFile;
		String[] appArgs = new String[0];

		if (!connected) {
			return;
		}

		appJarFile = getRunAppJarfile();
		appName = getRunAppMainClass();

		try {
			disconnect();
			System.out.println("New Isolate: " + appName + "," + appJarFile);
			iso = new Isolate(appName, appArgs, new String[] { "D:\\Java\\fcroot\\" + appJarFile });
			System.out.println("New Isolate: " + appName + " about to start");
			iso.start();
			System.out.println("New Isolate: " + appName + " started, waiting for exit...");
			iso.waitForExit();
			System.out.println("New Isolate: " + appName + " exit successfully with code:" + iso.exitCode());
			if (iso.exitCode() < 0) {
			}
		} catch (Exception IsolateStartupException) {
			System.out.println("IsolateStartupException when starting to run " + appName);
		}
	}

	protected String getDownloadAppName() throws IOException, ConnectionResetException {
		console.sendRequest(REQUEST_DOWNLOAD_APP_FILENAME);
		System.out.println("Waiting for app jar file name to download");
		return console.receiveString();
	}

	protected int getDownloadAppLength() throws IOException, ConnectionResetException {
		console.sendRequest(REQUEST_DOWNLOAD_APP_FILELENGTH);
		System.out.println("Waiting for app lentgh to download");
		return console.receiveInt();
	}

	protected int waitForCommand() throws IOException, ConnectionResetException {
		console.sendRequest(REQUEST_WAIT_COMMAND);
		System.out.println("Waiting for command");
		return console.receiveInt();
	}
	
	private void downloadApp() throws IOException, ConnectionResetException {
		if (!connected) {
			return;
		}

		String appJarFile = getDownloadAppName();
		int filebytes = getDownloadAppLength();

		FileConnection fconn = null;
		OutputStream file_os = null;
		System.out.println("filename to download:"+appJarFile);
		System.out.println("filebytes to download:"+filebytes);
		try {
			fconn = (FileConnection)Connector.open(APPDB_PATH_ROOT+appJarFile);
			if (fconn.exists()) {
				fconn.delete();
			}
			fconn.create();
			file_os = fconn.openOutputStream();
	        byte[] buf = new byte[filebytes];
			console.sendRequest(REQUEST_START_DOWNLOAD);
			int count = console.read(buf, 0, filebytes);
			if (filebytes != count) {
				System.out.println("Report fail");
				console.sendReport(REPORT_FAIL_DOWNLOAD);
			} else {
				file_os.write(buf, 0, count);
				System.out.println("Report finish");				
				console.sendReport(REPORT_FINISH_DOWNLOAD);
			}
			
		} 
		finally {
			if (file_os != null)
				file_os.close();

			if (fconn != null)
				fconn.close();
		}
		
		
	}

	void sync() throws IOException {
		int code;
		do {
			console.sendRequest(REQUEST_SYNC);
			code = console.receiveInt();
		} while (code != SYNC_CODE);
	}

	protected void connect() throws IOException {
		if (connected) 
			return;
		
		sc = (StreamConnection)Connector.open("comm:"+commport+";blocking=off");
		com_os = sc.openOutputStream();
		com_is = sc.openInputStream();
		connected = true;
		console.open(com_os, com_is);
		System.out.println("Proxy connected, sync...");
		try {
			sync();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			disconnect();
			throw e;
		}
		System.out.println("Proxy sync ok");
	}

	protected void disconnect() throws IOException {
		if (!connected)
			return;
		
        com_is.close();
		com_os.close();
		sc.close();

		com_is = null;
		com_os = null;
		sc = null;
		connected = false;
		console.close();
	}
	
	public static void main(String argv[]) {
		ams appman = new ams("COM0");
		
		do {
			try {
				appman.connect();
				
				int command = appman.waitForCommand();
				switch (command) {
					case COMMAND_DOWNLOAD_JAR:
						appman.downloadApp();
						break;
					case COMMAND_RUN_APP:
						appman.runApp();
						break;
					case COMMAND_LIST_APP:
						break;
					case COMMAND_STOP_APP:
						break;
					default:
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} while (true);
	}
}

