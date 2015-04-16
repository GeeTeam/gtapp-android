package com.geetest.sdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class SIMCardInfo {

	/**
	 * TelephonyManager提供设备上获取通讯服务信息的入口。 应用程序可以使用这个类方法确定的电信服务商和国家 以及某些类型的用户访问信息。
	 * 应用程序也可以注册一个监听器到电话收状态的变化。不需要直接实例化这个类
	 * 使用Context.getSystemService(Context.TELEPHONY_SERVICE)来获取这个类的实例。
	 */
	private TelephonyManager telephonyManager;
	
	private WifiManager wifi;
	/**
	 * 国际移动用户识别码
	 */
	private String IMSI;
	
	private Context mContext;

	public SIMCardInfo(Context context) {
		mContext = context;
		telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}

	public void stopWifi() {
		wifi.setWifiEnabled(false);
	}
	
	public String getRopDeviceCode(){
		String deviceCode="";
		SharedPreferences sd = mContext.getSharedPreferences("deciveCode", Context.MODE_PRIVATE);
		if (sd.contains("ROP_DEVICECODE")){
			deviceCode = sd.getString("ROP_DEVICECODE", "");
			return deviceCode;
		}else{
			String devicecode 	= telephonyManager.getDeviceId();
			String emulatecode 	= Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
			String netcardmac 	= wifi.getConnectionInfo().getMacAddress();
			if(!TextUtils.isEmpty(devicecode)){
				Editor editor = sd.edit();
		        editor.putString("ROP_DEVICECODE", devicecode);
		        editor.commit();
				return devicecode;
			}
			if(!TextUtils.isEmpty(emulatecode)){
				Editor editor = sd.edit();
		        editor.putString("ROP_DEVICECODE", emulatecode);
		        editor.commit();
				return emulatecode;
			}
			if(!TextUtils.isEmpty(netcardmac)){
				Editor editor = sd.edit();
		        editor.putString("ROP_DEVICECODE", netcardmac);
		        editor.commit();
				return netcardmac;
			}
		}
		return "";
	}



	/**
	 * Role:获取当前设置的电话号码 <BR>
	 * Date:2013-1-12 <BR>
	 * @author 
	 */
	public String getNativePhoneNumber() {
		String NativePhoneNumber = null;
		NativePhoneNumber = telephonyManager.getLine1Number();
		return NativePhoneNumber;
	}

	/**
	 * Role:Telecom service providers获取手机服务商信息 <BR>
	 * 需要加入权限<uses-permission
	 * android:name="android.permission.READ_PHONE_STATE"/> <BR>
	 * Date:2012-3-12 <BR>
	 * 
	 * @author CODYY
	 */
	public String getIMSI() {
		// 返回唯一的用户ID;
		IMSI = telephonyManager.getSubscriberId();

		if (IMSI == null) {
			IMSI = "";
		}

		return IMSI;
	}

	public String getProvidersName() {
		String ProvidersName = null;
		// IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
		if (getIMSI().startsWith("46000") || getIMSI().startsWith("46002")) {
			ProvidersName = "中国移动";
		} else if (getIMSI().startsWith("46001")) {
			ProvidersName = "中国联通";
		} else if (getIMSI().startsWith("46003")) {
			ProvidersName = "中国电信";
		}
		return ProvidersName;
	}


	public String macAddress = null;
	private int i = 0;

	public String getWifiMacAddress(final Context context) {
		WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		if (wifi == null) {
			((Activity) context).finish();
			AlertDialog.Builder builer = new Builder(context);
			builer.setTitle("提示");
			builer.setCancelable(false);
			builer.setMessage("获取手机唯一标识异常！");
			builer.setPositiveButton("确定", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					((Activity) context).finish();
				}
			});
			AlertDialog dialog = builer.create();
			dialog.show();

			return macAddress;
		}
		WifiInfo info = wifi.getConnectionInfo();
//		int ii=info.getRssi();
//		int i2=info.getBSSID();
//		int i3=info.getHiddenSSID();
//		int i4=
		this.macAddress = info.getMacAddress();
		if (this.macAddress == null && !wifi.isWifiEnabled()) {
			wifi.setWifiEnabled(true);
			for (int i = 0; i < 20; i++) {
				WifiInfo _info = wifi.getConnectionInfo();
				macAddress = _info.getMacAddress();
				Log.i("test", "新1mac  " + macAddress);
				if (macAddress != null){
					wifi.setWifiEnabled(false);
					break;
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					Looper.prepare();
					Toast.makeText(context, "获取手机唯一标识异常", Toast.LENGTH_SHORT)
							.show();
					Looper.loop();
					AlertDialog.Builder builer = new Builder(context);
					builer.setTitle("提示");
					builer.setMessage("获取手机唯一标识异常！");
					builer.setPositiveButton("确定", new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							((Activity) context).finish();
						}
					});
					AlertDialog dialog = builer.create();
					dialog.show();
					e.printStackTrace();
				}
			}
			
			return macAddress;

		} else if (wifi.isWifiEnabled()) {
			return macAddress;
		}
		return macAddress;
	}
	
	
	public static String getlocalIp(Context context){  
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);    
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();    
        int ipAddress = wifiInfo.getIpAddress();   
        //Log.d(Tag, "int ip "+ipAddress);  
        if(ipAddress==0)return null;  
        return ((ipAddress & 0xff)+"."+(ipAddress>>8 & 0xff)+"."  
                +(ipAddress>>16 & 0xff)+"."+(ipAddress>>24 & 0xff));  
    }  

}
