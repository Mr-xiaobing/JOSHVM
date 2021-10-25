package com.joshvm.ams.util;

import java.io.IOException;

import com.joshvm.ams.encoder.Base64;
import com.joshvm.ams.encoder.Hex;
import com.joshvm.ams.encoder.MD5Digest;

public class MD5Utils {

	/**
	 * md5Base64签名
	 * 
	 * @param content
	 * @return
	 * @throws IOException
	 */
	public static String md5Base64(String content, String key, long time) throws IOException {

		byte[] bs = Base64.encode(content.getBytes("utf-8"));

		String md5Content = new String(bs) + key + time;

		System.out.println(md5Content);

		MD5Digest digest = new MD5Digest();
		digest.update(md5Content.getBytes("utf-8"), 0, md5Content.getBytes("utf-8").length);

		byte[] md5Byte = new byte[16];
		digest.doFinal(md5Byte, 0);
 
		return Hex.encodeHex(md5Byte);

	}

}
