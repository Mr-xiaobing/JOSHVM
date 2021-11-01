package org.joshvm.ams.jams;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import org.joshvm.security.internal.*;


class FileInstaller extends Installer {
	public FileInstaller(SecurityToken securityToken,String installSource) {
		super(securityToken,installSource);
	}
	
	protected  InputStream getSourceStream() throws IOException {
		FileConnection fconn = (FileConnection)Connector.open(installSource);
		if (!fconn.exists()) {
			throw new IOException(installSource + "not found");
		}

		return fconn.openInputStream();
	}
}

