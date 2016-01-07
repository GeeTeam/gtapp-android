package com.geetest.gt_demo_android;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

import com.geetest.gt_sdk.GeetestLib;
import com.geetest.gt_sdk.GtDialog;
import com.geetest.gt_sdk.GtDialog.GtListener;

public class MainActivity extends Activity {

    private Context context = MainActivity.this;
    private GeetestLib gt = new GeetestLib();

    // 设置获取id，challenge，success的URL，需替换成自己的服务器URL
    private String captchaURL = "http://webapi.geetest.com/apis/start-mobile-captcha/";

    // 设置二次验证的URL，需替换成自己的服务器URL
    private String validateURL = "http://webapi.geetest.com/apis/mobile-server-validate/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        gt.setCaptchaURL(captchaURL);
        gt.setValidateURL(validateURL);

        findViewById(R.id.btn_gtapp_sdk_demo_dlg).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        new GtAppDlgTask().execute();
                    }
                });
    }

    class GtAppDlgTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {

            return gt.startCaptcha();
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (result) {

                openGtTest(context, gt.getCaptcha(), gt.getChallenge(), true);

            } else {
                // 极验服务器暂时性宕机：
                Toast.makeText(
                        getBaseContext(),
                        "Geetest Server is Down,Please Use your own system or disable the geetest",
                        Toast.LENGTH_LONG).show();

                // 1. 可以选择继续使用极验，去掉下行注释
                // openGtTest(context, gt.getCaptcha(), gt.getChallenge(), false);

                // 2. 使用自己的验证
            }
        }
    }

    public void openGtTest(Context ctx, String captcha, String challenge, boolean success) {

        GtDialog dialog = new GtDialog(ctx, captcha, challenge, success);

        // 启用debug可以在webview上看到验证过程的一些数据
        // dialog.setDebug(true);


        dialog.setGtListener(new GtListener() {

            @Override
            public void gtResult(boolean success, String result) {

                if (success) {

                    toastMsg("client captcha succeed:" + result);

                    try {
                        JSONObject res_json = new JSONObject(result);

                        Map<String, String> params = new HashMap<String, String>();

                        params.put("geetest_challenge", res_json.getString("geetest_challenge"));

                        params.put("geetest_validate", res_json.getString("geetest_validate"));

                        params.put("geetest_seccode", res_json.getString("geetest_seccode"));

                        String response = gt.submitPostData(params, "utf-8");

                        toastMsg("server captcha :" + response);

                    } catch (Exception e) {

                        e.printStackTrace();
                    }


                } else {

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
