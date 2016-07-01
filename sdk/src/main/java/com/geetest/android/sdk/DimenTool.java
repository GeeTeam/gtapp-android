package com.geetest.android.sdk;

import android.content.Context;

public class DimenTool {


	public static int getWidthPx(Context context) {
		return context.getResources().getDisplayMetrics().widthPixels;
	}
	/**
	 * 获取手机的高(像素)
	 */
	public static int getHeightPx(Context context) {
		return context.getResources().getDisplayMetrics().heightPixels;
	}


}
