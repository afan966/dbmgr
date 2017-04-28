package com.afan.dbmgr.util;

import java.util.regex.Pattern;

public class StringUtil {

	public static final String EMPTY = "";
	public static final String QUESTION = "?";

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static boolean isBlank(String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if ((Character.isWhitespace(str.charAt(i)) == false)) {
				return false;
			}
		}
		return true;
	}

	// source 查找 target出现的次数
	public static int find(String source, String target) {
		int number = 0;
		int i = 0;
		while ((i = source.indexOf(target, i)) != -1) {
			number++;
			i++;
		}
		return number;
	}

	public static boolean isNumber(String s) {
		if (isBlank(s)) {
			return false;
		}
		return Pattern.compile("[0-9]*([.]([0-9]+))?").matcher(s).matches();
	}
}
