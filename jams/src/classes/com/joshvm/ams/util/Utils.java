package com.joshvm.ams.util;

public class Utils {


	/**
	 * slip String
	 * 
	 * @param source
	 * @param slip
	 * @return
	 */
	public static String[] slipString(String source, String slip, int slipLen) {

		int stringArrayLen = 1;
		int i = 0;

		while ((i = source.indexOf(slip, i)) != -1) {

			stringArrayLen++;
			i = i + slipLen;
		}

		String[] stringArray = new String[stringArrayLen];

		i = 0;
		int poi = 0;
		int y = 0;

		while ((i = source.indexOf(slip, i)) != -1) {

			stringArray[poi] = source.substring(y, i);
			i = i + slipLen;
			y = i;
			poi++;
		}

		stringArray[poi] = source.substring(y, source.length());

		return stringArray;
	}

	/**
	 * 
	 * @param res
	 * @param start
	 * @param len
	 * @return
	 */
	public static byte sumByteArray(byte[] res, int start, int len) {

		byte sum = res[start];

		for (int i = start + 1; i < start + len; i++) {

			sum += res[i];
		}
		return sum;
	}

	/**
	 * byte[2] 转 int 高位在前地位在后
	 * 
	 * @param src
	 * @return
	 */
	public static double bytesToDouble(byte[] src) {

		double value;

		value = (double) (((src[0] & 0xFF) << 8) | (src[1] & 0xFF));

		return value;
	}

	/**
	 * byte[]转int
	 * 
	 * @param bytes
	 *            需要转换成int的数组
	 * @return int值
	 */
	public static int byteArrayToInt(byte[] bytes) {
		int value = 0;
		for (int i = 0; i < 4; i++) {
			int shift = (3 - i) * 8;
			value += (bytes[i] & 0xFF) << shift;
		}
		return value;
	}

}
