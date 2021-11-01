package org.joshvm.ams.jams;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;
import org.joshvm.security.internal.*;
import com.sun.cldc.io.j2me.file.Protocol;

import com.joshvm.ams.file.FileManager;

public class NetworkInstaller extends Installer {
	private InputStream server_is;
	private SecurityToken securityToken;

	public NetworkInstaller(SecurityToken securityToken,String installSource) {
		super(securityToken,installSource);

		this.installSource = installSource;
		this.securityToken = securityToken;

	}

	protected InputStream getSourceStream() {
		return server_is;

	}

	public void install(String appName, String mainClass, int length, boolean autoStart)
			throws IOException, InstallVerifyErrorException {
		HttpConnection connection = null;
		try {
			connection = (HttpConnection) Connector.open(installSource);
			// 设置请求方式
			connection.setRequestMethod(HttpConnection.GET);
			System.out.println("Connecting to " + installSource);
			// 获取响应HttpCode 200为成功
			int httpCode = connection.getResponseCode();
			System.out.println("ResponseHttpCode ==== : " + httpCode);
			if (httpCode == HttpConnection.HTTP_OK) { // 请求失败
				server_is = connection.openInputStream();
			}

			super.install(appName, mainClass, length, autoStart);
		} finally {
			if (server_is != null) {
				server_is.close();
			}
			if (connection != null) {
				connection.close();
			}
		}

		verify(appName, length);
	}

	private void verify(String appName, int length) throws IOException, InstallVerifyErrorException {

		try {
			// 读File
			Protocol fileConnection = new Protocol();
			fileConnection.openPrim(securityToken, "//" + Jams.getAppdbRoot() + appName + ".jar", Connector.READ_WRITE, false);

			if (!fileConnection.exists()) {
				FileManager.removeApp(appName);

				throw new InstallVerifyErrorException("JarFile not found");

			}

			if (length != fileConnection.fileSize()) {
				FileManager.removeApp(appName);

				throw new InstallVerifyErrorException("file size does not match");
			}
			
			fileConnection = new Protocol();
			fileConnection.openPrim(securityToken, "//" + Jams.getAppdbRoot() + appName + ".aut", Connector.READ_WRITE, false);

			if (!fileConnection.exists()) {
				FileManager.removeApp(appName);

				throw new InstallVerifyErrorException("AutFile not found");
			}

			fileConnection.close();
			fileConnection = null;

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
