package com.geetest.sdk;

import android.content.Context;
/**
 * 单位转换工具
 * @author 刘挺
 *
 */
public class DimenTool {
	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
	 
	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
	
	
	  //将pixel转换成sp  
    public static int pixelToSp(Context context, float pixelValue) {  
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;  
        int sp=(int) (pixelValue / scaledDensity + 0.5f);  
        return sp;  
    }  
  
    //将sp转换成pixel  
    public static int spToPixel(Context context, float spValue) {  
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;  
        int pixelValue=(int) (spValue * scaledDensity + 0.5f);  
        return pixelValue;  
    }  

	/**
	 * 获取手机的宽(像素)
	 */
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
