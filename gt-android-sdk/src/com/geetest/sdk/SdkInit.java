package com.geetest.sdk;

import android.content.Context;

/**
 * 初始化SDK的数据
 * 
 * @author Simin
 * @time 2015年4月13日 下午4:20:24
 */
public class SdkInit {
	private String captcha_id = "";
	private String challenge_id = "";

	/**
	 * Android上下文
	 */
	private Context context;

	public String getCaptcha_id() {
		return captcha_id;
	}

	public void setCaptcha_id(String captcha_id) {
		this.captcha_id = captcha_id;
	}

	public String getChallenge_id() {
		return challenge_id;
	}

	public void setChallenge_id(String challenge_id) {
		this.challenge_id = challenge_id;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

}
