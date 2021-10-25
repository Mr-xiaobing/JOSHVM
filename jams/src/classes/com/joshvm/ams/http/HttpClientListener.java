package com.joshvm.ams.http;

/**
 * Http连接监听
 * 
 * @author lihaotian
 *
 */
public interface HttpClientListener {

	/**
	 * 开始
	 */
	void onStart();

	/**
	 * 连接成功
	 */
	void onConnected();

	/**
	 * 成功通信
	 * 
	 * @param resp
	 *            响应信息
	 */
	void onResponse(String resp);

	/**
	 * 通信失败
	 * 
	 * @param httpCode
	 *            Http错误代码（非200），-1为异常导致失败
	 */
	void onFailure(int httpCode);

	/**
	 * 超时
	 */
	void onTimeout();

	/**
	 * 结束
	 */
	void onFinish();
}
