package com.joshvm.ams.http.adapter;

import java.io.IOException;
import java.util.Date;

import org.joshvm.ams.jams.Jams;
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

	private final String HTTP_HOST_URL = "http://118.190.93.145:9002/vm/ams/vm/app/check";
	private final String HTTP_SECRET_KEY = "ff552e123wd";
	
	private AppCheckCallback appCheckCallback;

	public CheckAppListAdapter(AppCheckCallback appCheckCallback) {
		this.appCheckCallback = appCheckCallback;
	}

	public HttpClient createHttpClient() {

		System.out.println("=========createHttpClient=========");

		String string = FileManager.getFilesMD5();

		try {
			JSONObject jsonData = new JSONObject();
			jsonData.put("imei", Jams.DEVICE_MAC_ADDRESS);
			jsonData.put("appList", string);

			long timestamp = new Date().getTime();

			String sign = MD5Utils.md5Base64(jsonData.toString(), HTTP_SECRET_KEY, timestamp);

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("data", jsonData);
			jsonObject.put("timestamp", timestamp);
			jsonObject.put("sign", sign);

			return new HttpClient.Builder().url(HTTP_HOST_URL).postJson(jsonObject.toString()).build();

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public void response(String resp) {
		
		System.out.println("====response========" + resp);
		appCheckCallback.response(resp);
	}

	public void failure(int httpCode) {

		System.out.println("=======failure=====" + httpCode);
		appCheckCallback.failure(httpCode);
	}

	public void timeout() {
		appCheckCallback.timeout();
		
	}
	
}
