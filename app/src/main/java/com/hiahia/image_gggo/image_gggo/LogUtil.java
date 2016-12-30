package com.hiahia.image_gggo.image_gggo;

import android.util.Log;

public class LogUtil {

	private static String defTag = "xxx";
	private static boolean DEBUG = true;
	
	public static void LogE(String tag, String s) {
		if (DEBUG) {
			Log.e(tag, s);
		}
	}
	public static void LogE(String s){
		LogE(defTag,s);
	}
	
	/**
	 * syso 打印  主要用在 我的 eclipse 习惯
	 */
	public static void Syo(String s){
		System.out.println(s);
	}
}
