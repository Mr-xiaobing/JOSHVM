package org.joshvm.ams.jams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.StreamConnection;

public class NetworkInstaller extends Installer {
	protected String hostname;
	protected String filePathToGet;
	private InputStream server_is;

	public NetworkInstaller(String installSource) {
		super(installSource);

		this.installSource = installSource;

		String install = installSource.substring(7);
		int pos = install.indexOf("/");
		if (pos == -1) {
			hostname = install;
			filePathToGet = "";
		} else {

			hostname = install.substring(0, pos);
			filePathToGet = install.substring(pos);

		}
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

		// verify(appName);
	}

	private void verify(String appName) throws IOException, InstallVerifyErrorException {
		StreamConnection sc = null;
		System.out.println("verify...");
		try {
			String host = "socket://" + hostname;
			System.out.println("Connecting to " + host);
			sc = (StreamConnection) Connector.open(host);
			System.out.println("Connected.");
			String request = "MD5 " + filePathToGet + "\n";
			OutputStream os = sc.openOutputStream();
			os.write(request.getBytes());
			os.close();

			InputStream is = sc.openInputStream();
			String reply = readline(is);
			System.out.println("verify: reply=" + reply);
			is.close();
			if (reply.startsWith("MD5=")) {
				reply = reply.substring(4);
				int md5 = Integer.parseInt(reply);
				// int digest = MD5.calc(installDest);
				// if (digest != md5) {
				// throw new InstallVerifyErrorException("Wrong MD5: "+digest+"
				// does not match expected MD5 value "+md5);
				// }
			}
		} catch (NumberFormatException nfe) {
			throw new InstallVerifyErrorException("Invalid server response");
		} finally {
			if (sc != null) {
				sc.close();
			}
		}
	}

	private String readline(InputStream in) throws IOException {
		int b;
		int off = 0;
		int maxlen = 600;
		StringBuffer line = new StringBuffer();
		while (off < maxlen) {
			b = in.read();
			if ((b == -1) || (b == 0x0a)) {
				// '\n' or eof
				break;
			}
			line.append((char) (b & 0xff));
		}
		return line.toString();
	}
}
