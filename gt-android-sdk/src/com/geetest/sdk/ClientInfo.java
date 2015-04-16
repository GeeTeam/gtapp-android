package com.geetest.sdk;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

import com.geetest.sdk.DimenTool;
import com.geetest.sdk.SIMCardInfo;

/**
 * 客户端信息
 * 
 * @author JiangLiangJun
 * 
 */
public class ClientInfo {

	/**
	 * 手机类型
	 */
	public String m_type;

	public String imei;

	/**
	 * 系统
	 */
	public String os_type = "android";

	/**
	 * 系统版本 android 版本
	 */
	public String os_ver_release = Build.VERSION.RELEASE;
	/**
	 * 系统版本 android 版本号
	 */
	public int os_ver_int = Build.VERSION.SDK_INT;

	/**
	 * 系统版本 android 版本号
	 */
	public int happ_ver_code;
	/**
	 * 宿主应用程序
	 */
	public String happ_ver_name;
	/**
	 * 插件版本
	 */
	public String gsdk_version = "android_2.15.5.16.1";

	/**
	 * 手机号
	 */

	public String phone;

	/**
	 * 屏幕分辨率 480*800
	 */

	public String m_screen;
	//
	// /**
	// * 品牌 型号
	// */
	// public String model = Build.MODEL;

	// public long latitude;
	//
	// public long longitude;
	//
	// public String city;

	public static ClientInfo clientInfo;

	/**
	 * 单列
	 */
	public static ClientInfo build(Context context) {
		if (clientInfo == null) {
			clientInfo = new ClientInfo(context);
		}

		return clientInfo;
	}

	private ClientInfo(Context context) {

		m_type = Build.BRAND + " " + Build.MODEL + " " + Build.TYPE;
		m_screen = DimenTool.getWidthPx(context) + "x"
				+ DimenTool.getHeightPx(context);

		SIMCardInfo simCardInfo = new SIMCardInfo(context);
		phone = simCardInfo.getNativePhoneNumber();

		imei = simCardInfo.getIMSI();

		PackageManager packageManager = context.getPackageManager();
		PackageInfo packInfo = null;
		try {
			packInfo = packageManager.getPackageInfo(context.getPackageName(),
					0);
			happ_ver_code = packInfo.versionCode;
			happ_ver_name = packInfo.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	public String toJsonString() {

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("mType", m_type);
			// jsonObject.put("mType", "M353");
			jsonObject.put("imei", imei);
			jsonObject.put("osType", os_type);
			jsonObject.put("osVerRelease", os_ver_release);
			jsonObject.put("osVerInt", os_ver_int);
			jsonObject.put("hAppVerCode", happ_ver_code);
			jsonObject.put("hAppVerName", happ_ver_name);
			jsonObject.put("gsdkVerCode", gsdk_version);
			jsonObject.put("phone", phone);
			jsonObject.put("mScreen", m_screen);

			return jsonObject.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// jsonObject .put("m_type", m_type);

		return "";

	}

}
