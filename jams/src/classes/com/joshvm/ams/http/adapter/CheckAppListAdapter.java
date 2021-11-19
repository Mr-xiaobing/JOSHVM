package com.joshvm.ams.http.adapter;

import java.io.IOException;
import java.util.Date;

import org.joshvm.ams.jams.Jams;
import org.joshvm.esp32.blufi.BlufiServer;
import org.json.me.JSONException;
import org.json.me.JSONObject;

import com.joshvm.ams.file.FileManager;
import com.joshvm.ams.http.HttpAdapter;
import com.joshvm.ams.http.HttpClient;
import com.joshvm.ams.http.callback.AppCheckCallback;
import com.joshvm.ams.util.MD5Utils;

/**
 * 检查更新适配器
 * 
 * @author lihaotian
 *
 */
public class CheckAppListAdapter extends HttpAdapter {

	/**
	 * 测试环境配置信息
	 */
	private String hostUrl = "http://118.190.93.145:9002/vm/ams/vm/app/check";
	private String secretKey = "ff552e123wd";

	private AppCheckCallback appCheckCallback;

	public CheckAppListAdapter(AppCheckCallback appCheckCallback) {
		this.appCheckCallback = appCheckCallback;
	}

	public HttpClient createHttpClient() {

		System.out.println("<Jams>:  createHttpClient=========");

		try {
			String deviceInfo = FileManager.getFileData(Jams.DEVICE_INFO);

			if (deviceInfo == null && deviceInfo.equals("")) {

				throw new Exception("UNKNOW DEVICE TYPE");

			}

			if (deviceInfo.equals(Jams.TEST)) {
				hostUrl = "http://118.190.93.145:9002/vm/ams/vm/app/check";
				secretKey = "ff552e123wd";
			} else if (deviceInfo.equals(Jams.PRODUCT)) {
				hostUrl = "http://118.190.93.145:9002/vm/ams/vm/app/check";
				secretKey = "ff552e123wd";
			}

			String string = FileManager.getFilesMD5();

			StringBuffer bluMac = new StringBuffer();
			byte[] bt_addr = new byte[6];
			BlufiServer.getBluetoothAddress(bt_addr);
			for (int i = 0; i < 6; i++) {
				bluMac.append(Integer.toString(bt_addr[i] & 0xff, 16));
				if (i == 5) {
					bluMac.append("");
				} else {
					bluMac.append(":");
				}
			}

			JSONObject jsonData = new JSONObject();
			jsonData.put("imei", bluMac);
			jsonData.put("appList", string);

			long timestamp = new Date().getTime();

			String sign = MD5Utils.md5Base64(jsonData.toString(), secretKey, timestamp);

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("data", jsonData);
			jsonObject.put("timestamp", timestamp);
			jsonObject.put("sign", sign);

			return new HttpClient.Builder().url(hostUrl).postJson(jsonObject.toString()).build();

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public void response(String resp) {

		System.out.println("<Jams>:  response========" + resp);
		appCheckCallback.response(resp);
	}

	public void failure(int httpCode) {

		System.out.println("<Jams>:  failure=====" + httpCode);
		appCheckCallback.failure(httpCode);
	}

	public void timeout() {
		System.out.println("<Jams>:  timeout=====");
		appCheckCallback.timeout();

	}

}
