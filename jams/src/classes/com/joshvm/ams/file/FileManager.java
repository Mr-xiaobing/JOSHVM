package com.joshvm.ams.file;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import org.joshvm.ams.jams.Jams;

/**
 * 文件读写Manager
 * 
 * @author zhangzhuang 2021-04-22
 *
 */
public class FileManager {

	public static String filePath = "/Phone/";

	public static InputStream inputStream = null;
	public static FileConnection fileConnection = null;

	/**
	 * 读取文件
	 */
	public static String checkFile(String fileName) {
		System.out.println("==========Check File...");
		try {
			// 读File
			fileConnection = (FileConnection) Connector.open("file://" + filePath + fileName);

			if (!fileConnection.exists()) {

				fileConnection.close();

				System.out.println("==========File No Exist...");
				return "";
			}

			inputStream = fileConnection.openInputStream();
			byte[] buffer = new byte[256];
			int readLen = 0;
			StringBuffer stringBuffer = new StringBuffer();

			while ((readLen = inputStream.read(buffer)) != -1) {

				stringBuffer.append(new String(buffer, 0, readLen));

			}

			inputStream.close();
			inputStream = null;
			fileConnection.close();
			fileConnection = null;

			return stringBuffer.toString();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * 获取蓝牙名字前缀
	 * 
	 * @param appname
	 * @return
	 */
	public static String getBluName(String fileName) {
		
		String filepathName = "file:///" + Jams.getAppdbRoot() + fileName;
		
		try {
			// 读File
			fileConnection = (FileConnection) Connector.open(filepathName);

			if (!fileConnection.exists()) {

				fileConnection.close();

				System.out.println("==========File No Exist...");
				return "";
			}

			inputStream = fileConnection.openInputStream();
			byte[] buffer = new byte[256];
			int readLen = 0;
			StringBuffer stringBuffer = new StringBuffer();

			while ((readLen = inputStream.read(buffer)) != -1) {

				stringBuffer.append(new String(buffer, 0, readLen));

			}

			inputStream.close();
			inputStream = null;
			fileConnection.close();
			fileConnection = null;

			return stringBuffer.toString();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
		
	}

	/**
	 * 查看是否
	 * 
	 * @param appname
	 * @return
	 */
	public static boolean isInstalled(String appname) {
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

	/**
	 * 获取目录下的所有jar文件名
	 * 
	 * @return
	 */
	public static String getFilesMD5() {

		String filepath = "file:///" + Jams.getAppdbRoot();

		StringBuffer stringBuffer = new StringBuffer();

		try {
			FileConnection fconn = (FileConnection) Connector.open(filepath);
			java.util.Enumeration em = fconn.list();

			while (em.hasMoreElements()) {
				String filename = (String) em.nextElement();
				if (filename.endsWith(".jar")) {

					System.out.println("===========" + filename);
					stringBuffer.append(filename.substring(0, filename.length() - 4) + Jams.BLUFI_CMD_DIVISION);
				}
			}

			fconn.close();

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		if (stringBuffer.toString().length() == 0) {

			return "";
		}

		return stringBuffer.toString().substring(0, stringBuffer.toString().length() - 2);

	}

	/**
	 * 获取目录下的所有文件
	 * 
	 * @return
	 */
	public static void getFilesName() {

		String filepath = "file:///" + Jams.getAppdbRoot();

		try {
			FileConnection fconn = (FileConnection) Connector.open(filepath);
			java.util.Enumeration em = fconn.list();

			while (em.hasMoreElements()) {
				String filename = (String) em.nextElement();

				System.out.println("===========" + filename);
			}

			fconn.close();

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}

	/**
	 * 删除应用以及aut文件
	 * 
	 * @param appName
	 */
	public static void removeApp(String appName) {

		FileConnection fconn = null;
		String filename = "file:///" + Jams.getAppdbRoot() + appName;
		try {
			fconn = (FileConnection) Connector.open(filename + ".jar");
			fconn.delete();
		} catch (IOException e) {
			e.printStackTrace();
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
	
}
