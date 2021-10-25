package org.joshvm.ams.jams;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;


class FileInstaller extends Installer {
	public FileInstaller(String installSource) {
		super(installSource);
	}
	
	protected  InputStream getSourceStream() throws IOException {
		FileConnection fconn = (FileConnection)Connector.open(installSource);
		if (!fconn.exists()) {
			throw new IOException(installSource + "not found");
		}

		return fconn.openInputStream();
	}
}

