package com.joshvm.ams.http;

/**
 * 网络请求适配器
 * 
 * @author lihaotian
 *
 */
public abstract class HttpAdapter {

	/**
	 * 创建请求
	 * 
	 * @return
	 */
	public abstract HttpClient createHttpClient();

	/**
	 * 响应
	 * 
	 * @param resp
	 */
	public abstract void response(String resp);

	/**
	 * 错误
	 * 
	 * @param httpCode
	 */
	public abstract void failure(int httpCode);
	/**
	 * 超时
	 * 
	 */
	public abstract void timeout();

}
