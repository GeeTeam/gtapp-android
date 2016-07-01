package com.geetest.android.demo;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.os.Looper;
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
    //因为可能用户当时所处在低速高延迟网络，所以异步请求可能在后台用时很久才获取到验证的数据。可以自己设计状态指示器, demo仅作演示。
    private ProgressDialog progressDialog;
    private GtAppDlgTask mGtAppDlgTask;

    // 创建验证码网络管理器实例
    private Geetest captcha = new Geetest(

            // 设置获取id，challenge，success的URL，需替换成自己的服务器URL
            "http://webapi.geelao.ren:8011/gtcap/start-mobile-captcha/",

            // 设置二次验证的URL，需替换成自己的服务器URL
            "http://webapi.geelao.ren:8011/gtcap/gt-server-validate/"
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

                        progressDialog = ProgressDialog.show(context, null, "Loading", true, true);
                        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                toastMsg("user cancel progress dialog");
                                mGtAppDlgTask.cancel(true);
                                captcha.cancelReadConnection();
                            }
                        });
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
                //TODO 提交二次验证超时
                toastMsg("submit error");
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

                // 根据captcha.getSuccess()的返回值 自动推送正常或者离线验证
                openGtTest(context, captcha.getGt(), captcha.getChallenge(), captcha.getSuccess());

            } else {

                // TODO 从API_1获得极验服务宕机或不可用通知, 使用备用验证或静态验证
                // 静态验证依旧调用上面的openGtTest(_, _, _), 服务器会根据getSuccess()的返回值, 自动切换
                Toast.makeText(
                        getBaseContext(),
                        "Geetest Server is Down.",
                        Toast.LENGTH_LONG).show();

                // 执行此处网站主的备用验证码方案

            }
        }
    }

    public void openGtTest(Context ctx, String id, String challenge, boolean success) {

        GtDialog dialog = new GtDialog(ctx, id, challenge, success);

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

                    try {
                        JSONObject res_json = new JSONObject(result);

                        Map<String, String> params = new HashMap<String, String>();

                        params.put("geetest_challenge", res_json.getString("geetest_challenge"));

                        params.put("geetest_validate", res_json.getString("geetest_validate"));

                        params.put("geetest_seccode", res_json.getString("geetest_seccode"));

                        String response = captcha.submitPostData(params, "utf-8");

                        //TODO 验证通过, 获取二次验证响应, 根据响应判断验证是否通过完整验证
                        toastMsg("server captcha :" + response);

                    } catch (Exception e) {

                        e.printStackTrace();
                    }

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
                }else {
                    //TODO 验证加载超时,未准备完成
                    toastMsg("there's a network jam");
                }
            }

        });

    }

    private void toastMsg(String msg) {

        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();

    }

}
