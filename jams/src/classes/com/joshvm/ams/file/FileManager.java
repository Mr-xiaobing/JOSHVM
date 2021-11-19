package com.joshvm.ams.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import org.joshvm.ams.jams.InstallVerifyErrorException;
import org.joshvm.ams.jams.Jams;
import com.sun.cldc.io.j2me.file.Protocol;
import org.joshvm.security.internal.*;

/**
 * 文件读写Manager
 * 
 * @author zhangzhuang 2021-04-22
 *
 */
public class FileManager {

	public static String filePath = "/Phone/";

	public static InputStream inputStream = null;
	public static OutputStream outputStream = null;
	public static FileConnection fileConnection = null;

	/**
	 * 保存文件
	 */
	public static void saveFile(String fileName, String data) {
		System.out.println("<Jams>:  Save File...");
		try {
			fileConnection = (FileConnection) Connector.open("file://" + filePath + fileName);

			if (fileConnection.exists()) {

				fileConnection.delete();

			}

			// 创建File
			fileConnection.create();

			// 写入File
			outputStream = fileConnection.openOutputStream();
			outputStream.write(data.getBytes());

			outputStream.close();
			outputStream = null;
			fileConnection.close();
			fileConnection = null;

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 删除对应文件
	 * 
	 * @param fileName
	 */
	public static void deleteFile(String fileName) {

		System.out.println("<Jams>:  Delete File...");
		// 删除文件
		try {
			fileConnection = (FileConnection) Connector.open("file://" + filePath + fileName);

			if (fileConnection.exists()) {

				fileConnection.delete();

			}

			fileConnection.close();
			fileConnection = null;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 读取文件
	 */
	public static String checkFile(String fileName) {
		System.out.println("<Jams>:  Check File...");
		try {
			// 读File
			fileConnection = (FileConnection) Connector.open("file://" + filePath + fileName);

			if (!fileConnection.exists()) {

				fileConnection.close();

				System.out.println("<Jams>:  File No Exist...");
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
	public static String getFileData(String fileName) {

		String filepathName = "//" + Jams.getAppdbRoot() + fileName;

		try {

			Protocol fconn = new Protocol();
			fconn.openPrim(Jams.getSecurityToken(), filepathName, Connector.READ_WRITE, false);

			if (!fconn.exists()) {

				fconn.close();

				System.out.println("<Jams>:  File No Exist...");
				return "";
			}

			inputStream = fconn.openInputStream();
			byte[] buffer = new byte[256];
			int readLen = 0;
			StringBuffer stringBuffer = new StringBuffer();

			while ((readLen = inputStream.read(buffer)) != -1) {

				stringBuffer.append(new String(buffer, 0, readLen));

			}

			inputStream.close();
			inputStream = null;
			fconn.close();
			fconn = null;

			return stringBuffer.toString();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";

	}

	/**
	 * 查看是否安装
	 * 
	 * @param appname
	 * @return
	 */
	public static boolean isInstalled(String appname) {
		boolean result;
		String filepath = "//" + Jams.getAppdbRoot() + appname + ".jar";
		try {
			Protocol fconn = new Protocol();
			fconn.openPrim(Jams.getSecurityToken(), filepath, Connector.READ_WRITE, false);

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

		String filepath = "//" + Jams.getAppdbRoot();

		StringBuffer stringBuffer = new StringBuffer();

		try {
			Protocol fconn = new Protocol();
			fconn.openPrim(Jams.getSecurityToken(), filepath, Connector.READ_WRITE, false);
			java.util.Enumeration em = fconn.list();

			while (em.hasMoreElements()) {
				String filename = (String) em.nextElement();
				if (filename.endsWith(".jar")) {

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

		String filepath = "//" + Jams.getAppdbRoot();

		try {
			Protocol fconn = new Protocol();
			fconn.openPrim(Jams.getSecurityToken(), filepath, Connector.READ_WRITE, false);
			java.util.Enumeration em = fconn.list();

			while (em.hasMoreElements()) {
				String filename = (String) em.nextElement();

				System.out.println("<Jams>: getFilesName ====" + filename);
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
		Protocol fconn = null;
		String filename = "//" + Jams.getAppdbRoot() + appName;
		try {
			fconn = new Protocol();
			fconn.openPrim(Jams.getSecurityToken(), filename + ".jar", Connector.READ_WRITE, false);

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

			fconn = new Protocol();
			fconn.openPrim(Jams.getSecurityToken(), filename + ".aut", Connector.READ_WRITE, false);
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

	/**
	 * 校验应用
	 * @param appName
	 * @param length
	 * @return
	 * @throws IOException
	 * @throws InstallVerifyErrorException
	 */
	public static boolean verify(String appName, int length) {
		String filename = "//" + Jams.getAppdbRoot() + appName;
		try {
			// 读File
			Protocol fileConnection = new Protocol();
			fileConnection.openPrim(Jams.getSecurityToken(), filename + ".jar", Connector.READ_WRITE,
					false);

			if (!fileConnection.exists()) {
				FileManager.removeApp(appName);
				return false;

			}

			if (length != fileConnection.fileSize()) {
				FileManager.removeApp(appName);

				return false;
			}

			fileConnection = new Protocol();
			fileConnection.openPrim(Jams.getSecurityToken(), filename+ ".aut", Connector.READ_WRITE,
					false);

			if (!fileConnection.exists()) {
				FileManager.removeApp(appName);
				return false;
			}

			fileConnection.close();
			fileConnection = null;

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}

}
