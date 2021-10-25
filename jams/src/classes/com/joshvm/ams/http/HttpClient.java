package com.joshvm.ams.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import com.joshvm.ams.timeout.TimeOutCallback;
import com.joshvm.ams.timeout.Timeouts;

/**
 * Http连接类
 * 
 * @author lihaotian
 *
 */
public class HttpClient {

	private HttpConnection connection;
	private InputStream inputStream;
	private OutputStream outputStream;

	private String url;
	private String requestMethod;
	private String contentType;
	private String requestBody;
	
	private static Timeouts timeouts;

	/**
	 * 构造器
	 * 
	 * @param builder
	 */
	private HttpClient(Builder builder) {

		System.out.println("===HttpClient==" + builder.requestMethod);

		url = builder.url;
		requestMethod = builder.requestMethod;
		contentType = builder.contentType;
		requestBody = builder.requestBody;
	}

	/**
	 * 执行请求
	 * 
	 * @param httpClientListener
	 */
	public void execute(final HttpClientListener httpClientListener) {

		timeouts = new Timeouts();
		timeouts.setOutTime(10);
		timeouts.setCallback(new TimeOutCallback() {
			public void timeOut() {
				httpClientListener.onTimeout();

			}
		});
		timeouts.startTimer();

		try {

			httpClientListener.onStart();
			// 连接
			connection = (HttpConnection) Connector.open(url);
			System.out.println(url);
			httpClientListener.onConnected();
			// 设置请求方式
			connection.setRequestMethod(requestMethod);
			connection.setRequestProperty("Content-Type", contentType);
			// 获取输出流
			outputStream = connection.openOutputStream();
			// 根据请求方式执行请求
			// TODO 完善请求方式的处理
			if (requestMethod.equals(HttpConnection.POST)) {
				executePost();
			}
			outputStream.flush();
			// 获取响应HttpCode 200为成功
			int httpCode = connection.getResponseCode();
			System.out.println("ResponseHttpCode ==== : " + httpCode);
			if (httpCode != HttpConnection.HTTP_OK) { // 请求失败
				
				if (timeouts.isTiming()) {
					httpClientListener.onFailure(httpCode);
					timeouts.dismiss();
				}
				
			} else {
				
				// 获取响应数据
				// TODO 数据长度处理
				InputStream inputStream = connection.openInputStream();
				byte[] data = new byte[1024];
				int readLen = inputStream.read(data);
				String response = new String(data, 0, readLen);
				System.out.println(timeouts.isTiming()+"ResponseMessage ==== : " + response);
				
				if (timeouts.isTiming()) {
					timeouts.dismiss();
					httpClientListener.onResponse(response);
					
				}
				
			}

		} catch (Exception e) {

			e.printStackTrace();
			
			if (timeouts.isTiming()) {
				httpClientListener.onFailure(-1);
				timeouts.dismiss();
			}
			

		} finally {

			try {
				if (inputStream != null) {
					inputStream.close();

				}
				if (outputStream != null) {
					outputStream.close();
				}

				if (connection != null) {
					connection.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			httpClientListener.onFinish();
		}
	}

	/**
	 * 执行post请求
	 * 
	 * @throws IOException
	 */
	private void executePost() throws IOException {
	
		outputStream.write(requestBody.getBytes());
		
		System.out.println(requestBody);
		
	}

	/**
	 * 构造者
	 * 
	 * @author lihaotian
	 *
	 */
	public static class Builder {

		private String url;
		private String requestMethod;
		private String contentType;
		private String requestBody;

		/**
		 * 设置url
		 * 
		 * @param url
		 * @return
		 */
		public Builder url(String url) {
			this.url = url;
			return this;
		}

		/**
		 * PostJson请求
		 * 
		 * @param requestBody
		 * @return
		 */
		public Builder postJson(String requestBody) {
			requestMethod = HttpConnection.POST;
			this.requestBody = requestBody;
			this.contentType = "application/json";
			return this;
		}

		/**
		 * Get请求
		 * 
		 * @return
		 */
		public Builder get() {
			requestMethod = HttpConnection.GET;
			return this;
		}

		/**
		 * 构建
		 * 
		 * @return
		 */
		public HttpClient build() {
			return new HttpClient(this);
		}

	}

}
