package com.joshvm.ams.http;

/**
 * Http功能管理类
 * 
 * @author lihaotian
 *
 */
public class HttpManager {

	/**
	 * 执行请求
	 * 
	 * @param httpAdapter
	 */
	public static void executeRequest(final HttpAdapter httpAdapter) {
		
		HttpClient httpClient = httpAdapter.createHttpClient();
		httpClient.execute(new HttpClientListener() {

			public void onTimeout() {
				
				httpAdapter.timeout();
			}

			public void onStart() {
			}

			public void onResponse(String resp) {

				httpAdapter.response(resp);
			}

			public void onFinish() {
			}

			public void onFailure(int httpCode) {

				httpAdapter.failure(httpCode);
			}

			public void onConnected() {
			}
		});

	}

}
