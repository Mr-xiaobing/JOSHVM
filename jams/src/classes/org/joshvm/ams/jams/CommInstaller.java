package org.joshvm.ams.jams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.joshvm.ams.consoleams.ConsoleConnection;
import org.joshvm.security.internal.*;

class CommInstaller extends Installer {
	protected InputStream server_is;

	public CommInstaller(SecurityToken securityToken,String installSource) {
		super(securityToken,installSource);
		server_is = null;
	}

	protected  InputStream getSourceStream() {
		return server_is;
	}

	public void install(String appName, String mainClass, int length, boolean autoStart) throws IOException, InstallVerifyErrorException {
		StreamConnection sc = null;
		ConsoleConnection console = null;
		
		try {
			sc = (StreamConnection)Connector.open(installSource+";blocking=off");
			OutputStream os = sc.openOutputStream();
			InputStream is = sc.openInputStream();
			
			console = new ConsoleConnection();
			console.open(os, is);
			console.sendRequest(org.joshvm.ams.consoleams.ams.REQUEST_START_DOWNLOAD);

			server_is = is;
			
			super.install(appName, mainClass, length, autoStart);
		} finally {
			if (console != null) {
				console.close();
			}
			
			if (server_is != null) {
				server_is.close();
			}
			if (sc != null) { 
				sc.close();
			}
		}
	}
}

