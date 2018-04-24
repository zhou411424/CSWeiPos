package com.chuanshi.weipos.utils;

import android.util.Log;

import com.chuanshi.weipos.BuildConfig;


/**
 * log工具类
 *
 */
public class Logger {

	/***
	 * 是否处于调试模式
	 * @return
	 */
	public static boolean isDebug(){
		return BuildConfig.DEBUG;
	}
	
	public static void d(String tag,String msg) {
		if (BuildConfig.DEBUG) {
			Log.d(tag, msg);
		}
	}
	
	public static void d(String tag,String msg,Throwable e) {
		if (BuildConfig.DEBUG) {
			Log.d(tag, msg,e);
		}
	}
	public static void i(String tag,String msg) {
		if (BuildConfig.DEBUG) {
			Log.i(tag, msg);
		}
	}
	public static void v(String tag,String msg) {
		if (BuildConfig.DEBUG) {
			Log.v(tag, msg);
		}
	}
	public static void e(String tag,String msg) {
		if (BuildConfig.DEBUG) {
			Log.e(tag, msg);
		}
	}
	public static void e(String tag,String msg,Throwable e) {
		if (BuildConfig.DEBUG) {
			Log.e(tag, msg,e);
		}
	}
	
	public static void e(String tag,Throwable e){
		if(BuildConfig.DEBUG){
			Log.e(tag,"",e);
		}
	}
	
	public static void w(String tag,String msg) {
		if (BuildConfig.DEBUG) {
			Log.w(tag, msg);
		}
	}
	public static void w(String tag,String msg,Throwable e) {
		if (BuildConfig.DEBUG) {
			Log.w(tag, msg,e);
		}
	}
	
}
