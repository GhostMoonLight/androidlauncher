package com.android.launcher3.utils;

import java.security.MessageDigest;

/**
 * @author jiang
 * MD5加密
 */
public class MD5Util {

	public static String getMD5(String val) {
		if (val != null) {
			return getMD5(val.getBytes());
		}
		return "";
	}

	public static String getMD5(byte[] val) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] md5Bytes = md5.digest(val);
			StringBuffer hexValue = new StringBuffer();
			for (int i = 0; i < md5Bytes.length; i++) {
				int temp = ((int) md5Bytes[i]) & 0xff;
				if (temp < 16) {
					hexValue.append("0");
				}
				hexValue.append(Integer.toHexString(temp));
			}
			return hexValue.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}
