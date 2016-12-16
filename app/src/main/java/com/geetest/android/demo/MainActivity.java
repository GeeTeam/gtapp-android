package com.geetest.android.demo;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.app.Activity;
import android.content.DialogInterface;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Toast;

import org.json.JSONObject;

import com.geetest.android.sdk.Geetest;
import com.geetest.android.sdk.GtDialog;
import com.geetest.android.sdk.GtDialog.GtListener;

public class MainActivity extends Activity {

    private Context context = MainActivity.this;
    //考虑用户当时可能所处在弱网络环境，所以异步请求可能在后台用时很久才获取到验证的数据。xxdemo仅作演示。
    private ProgressDialog progressDialog;
    private GtAppDlgTask mGtAppDlgTask;
    private GtAppValidateTask mGtAppValidateTask;

    // 创建验证码网络管理器实例
    private Geetest captcha = new Geetest(

            // 设置获取id，challenge，success的URL，需替换成自己的服务器URL
            "http://api.apiapp.cc/gtcap/start-mobile-captcha/",

            // 设置二次验证的URL，需替换成自己的服务器URL
            "http://api.apiapp.cc/gtcap/gt-server-validate/"
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

                        GtAppDlgTask gtAppDlgTask = new GtAppDlgTask();
                        mGtAppDlgTask = gtAppDlgTask;
                        mGtAppDlgTask.execute();

                        if (!((Activity) context).isFinishing()) {
                            progressDialog = ProgressDialog.show(context, null, "Loading", true, true);
                            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    toastMsg("user cancel progress dialog");
                                    if (mGtAppDlgTask.getStatus() == AsyncTask.Status.RUNNING) {
                                        Log.i("async task", "status running");
                                        captcha.cancelReadConnection();
                                        mGtAppDlgTask.cancel(true);
                                    } else {
                                        Log.i("async task", "No thing happen");
                                    }
                                }
                            });
                        }
                    }
                });

        captcha.setTimeout(5000);

        captcha.setGeetestListener(new Geetest.GeetestListener() {
            @Override
            public void readContentTimeout() {
                mGtAppDlgTask.cancel(true);
                //TODO 获取验证参数超时
                progressDialog.dismiss();
                //Looper.prepare() & Looper.loop(): 在当前线程并没有绑定Looper时返回为null, 可以与toastMsg()一同在正式版本移除
                Looper.prepare();
                toastMsg("read content time out");
                Looper.loop();
            }

            @Override
            public void submitPostDataTimeout() {
                mGtAppValidateTask.cancel(true);
                //TODO 提交二次验证超时
                toastMsg("submit error");
            }

            @Override
            public void receiveInvalidParameters() {
                //TODO 从API接收到无效的JSON参数
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                toastMsg("Did recieve invalid parameters.");
            }
        });
    }

    class GtAppDlgTask extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... params) {

            return captcha.checkServer();
        }

        @Override
        protected void onPostExecute(JSONObject parmas) {

            if (parmas != null) {

                // 根据captcha.getSuccess()的返回值 自动推送正常或者离线验证
                if (captcha.getSuccess()) {
                    openGtTest(context, parmas);
                } else {
                    // TODO 从API_1获得极验服务宕机或不可用通知, 使用备用验证或静态验证
                    // 静态验证依旧调用上面的openGtTest(_, _, _), 服务器会根据getSuccess()的返回值, 自动切换
                    // openGtTest(context, params);
                    toastLongTimeMsg("Geetest Server is Down.");
                    // 执行此处网站主的备用验证码方案
                }

            } else {
                toastLongTimeMsg("Can't Get Data from API_1");
            }
        }
    }

    class GtAppValidateTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                JSONObject res_json = new JSONObject(params[0]);

                Map<String, String> validateParams = new HashMap<String, String>();

                validateParams.put("geetest_challenge", res_json.getString("geetest_challenge"));

                validateParams.put("geetest_validate", res_json.getString("geetest_validate"));

                validateParams.put("geetest_seccode", res_json.getString("geetest_seccode"));

                String response = captcha.submitPostData(validateParams, "utf-8");

                //TODO 验证通过, 获取二次验证响应, 根据响应判断验证是否通过完整验证

                return response;

            } catch (Exception e) {

                e.printStackTrace();
            }

            return "invalid result";
        }

        @Override
        protected void onPostExecute(String params) {
            toastMsg("server captcha :" + params);
        }
    }

    public void openGtTest(Context ctx, JSONObject params) {

        GtDialog dialog = new GtDialog(ctx, params);

        // 启用debug可以在webview上看到验证过程的一些数据
//        dialog.setDebug(true);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                //TODO 取消验证
                toastMsg("user close the geetest.");
            }
        });

        dialog.setGtListener(new GtListener() {

            @Override
            public void gtResult(boolean success, String result) {

                if (success) {

                    toastMsg("client captcha succeed:" + result);

                    GtAppValidateTask gtAppValidateTask = new GtAppValidateTask();
                    mGtAppValidateTask = gtAppValidateTask;
                    mGtAppValidateTask.execute(result);

                } else {
                    //TODO 验证失败
                    toastMsg("client captcha failed:" + result);
                }
            }

            @Override
            public void gtCallClose() {

                toastMsg("close geetest windows");
            }

            @Override
            public void gtCallReady(Boolean status) {

                progressDialog.dismiss();

                if (status) {
                    //TODO 验证加载完成
                    toastMsg("geetest finish load");
                } else {
                    //TODO 验证加载超时,未准备完成
                    toastMsg("there's a network jam");
                }
            }

            @Override
            public void gtError() {
                progressDialog.dismiss();
                toastMsg("Fatal Error Did Occur.");
            }

        });

    }

    private void toastMsg(final String msg) {

        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void toastLongTimeMsg(final String msg) {

        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
            }
        });

    }

}
