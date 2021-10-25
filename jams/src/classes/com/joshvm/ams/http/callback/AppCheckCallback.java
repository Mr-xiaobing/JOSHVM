package com.joshvm.ams.http.callback;

public interface AppCheckCallback {
	
	void response(String resp);
	void failure(int httpCode);
	void timeout();

}
