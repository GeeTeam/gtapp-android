package com.gt.demo;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.geetest.sdk.GeetestLib;
import com.geetest.sdk.GtDialog;
import com.geetest.sdk.GtDialog.GtListener;
import com.geetest.sdk.SdkInit;

public class MainActivity extends Activity {

	private SdkInit sdkInitData = new SdkInit();

	// TODO get your own captcha id
	private String captcha_id = "ad872a4e1a51888967bdb7cb45589605";
	private Context context = MainActivity.this;
	private GeetestLib geetestLib = new GeetestLib();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findViewById(R.id.btn_gtapp_sdk_demo_dlg).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						geetestLib.setCaptchaId(captcha_id);
						new GtAppDlgTask().execute();
					}
				});
	}

	class GtAppDlgTask extends AsyncTask<Void, Integer, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			return geetestLib.preProcess();
		}

		@Override
		protected void onPostExecute(Integer serverStatusCode) {

			if (serverStatusCode == 1) {

				sdkInitData.setCaptcha_id(captcha_id);
				sdkInitData.setChallenge_id(geetestLib.getChallengeId());
				sdkInitData.setContext(context);
				openGtTest(sdkInitData);

			} else {
				// TODO 使用自己的验证码体系来进行判断。或者不做任何处理
				Toast.makeText(
						getBaseContext(),
						"Geetest Server is Down,Please Use your own system or disable the geetest",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	public void openGtTest(SdkInit initData) {
		GtDialog dialog = GtDialog.newInstance(initData);


		dialog.setGtListener(new GtListener() {

			@Override
			public void gtResult(boolean success, String result) {
				// TODO Auto-generated method stub
				if (success) {
					// TODO captcha
					toastMsg("client captcha succeed:" + result);

					// TODO If captcha is succeed on client side ,then post the
					// data to CustomServer to setup the second validate
					try {
						JSONObject res_json = new JSONObject(result);

						//TODO Demo use of captcha
						String custom_server_validate_url = "http://testcenter.geetest.com/gtweb/android_sdk_demo_server_validate/";

						Map<String, String> params = new HashMap<String, String>();

						params.put("geetest_challenge",
								res_json.getString("geetest_challenge"));
						params.put("geetest_validate",
								res_json.getString("geetest_validate"));
						params.put("geetest_seccode",
								res_json.getString("geetest_seccode"));
						String response = geetestLib.submitPostData(
								custom_server_validate_url, params, "utf-8");
						
						toastMsg("server captcha :" + response);

					} catch (Exception e) {
						e.printStackTrace();
					}
					

				} else {
					// TODO 验证失败
					toastMsg("client captcha failed:" + result);
				}
			}

			@Override
			public void closeGt() {
				toastMsg("Close geetest windows");
			}
		});
		dialog.show();
	}

	private void toastMsg(String msg) {
		Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
	}

}
