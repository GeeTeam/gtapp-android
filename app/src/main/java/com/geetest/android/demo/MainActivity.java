package com.geetest.android.demo;

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

import com.geetest.android.sdk.Geetest;
import com.geetest.android.sdk.GtDialog;
import com.geetest.android.sdk.GtDialog.GtListener;

public class MainActivity extends Activity {

    private Context context = MainActivity.this;

    // 创建验证码实例
    private Geetest captcha = new Geetest(

            // 设置获取id，challenge，success的URL，需替换成自己的服务器URL
            "http://webapi.geetest.com/apis/start-mobile-captcha/",

            // 设置二次验证的URL，需替换成自己的服务器URL
            "http://webapi.geetest.com/apis/mobile-server-validate/"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

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

            return captcha.checkServer();
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (result) {

                openGtTest(context, captcha.getGt(), captcha.getChallenge(), true);

            } else {
                // 极验服务器暂时性宕机：
                Toast.makeText(
                        getBaseContext(),
                        "Geetest Server is Down.",
                        Toast.LENGTH_LONG).show();

                // 选择1. 继续使用极验（failback模式），去掉下行注释
                // openGtTest(context, captcha.getCaptcha(), captcha.getChallenge(), false);

                // 选择2. 使用您备用的验证码
            }
        }
    }

    public void openGtTest(Context ctx, String id, String challenge, boolean success) {

        GtDialog dialog = new GtDialog(ctx, id, challenge, success);

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

                        String response = captcha.submitPostData(params, "utf-8");

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
