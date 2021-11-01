package com.joshvm.ams.http.callback;
/**
 * 网络请求回调
 * @author 86188
 *
 */
public interface AppCheckCallback {
	
	void response(String resp);
	void failure(int httpCode);
	void timeout();

}
