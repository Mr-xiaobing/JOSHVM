package com.joshvm.ams.util;
/**
 * 通用工具类
 * @author 86188
 *
 */
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

}
