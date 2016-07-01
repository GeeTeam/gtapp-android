package com.geetest.android.sdk;

import java.util.Timer;
import java.util.TimerTask;
import java.lang.Runnable;
import java.lang.Character.UnicodeBlock;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/**
 * 验证对话框
 * 
 * @author dreamzsm@gmail.com
 * 
 */
public class GtDialog extends Dialog {

    private String baseURL = "http://static.geetest.com/static/appweb/app-index.html";
//    private String baseURL = "http://192.168.1.195:8720";

    protected static final String ACTIVITY_TAG="GtDialog";

    private String id;
    private String challenge;
    private Boolean success;
    private String product = "embed";
    private String language = "zh-cn";
    private String mTitle = "";
    private Boolean debug = false;

    private Dialog mDialog = this;
    private Context context;
    private int mWidth;
    private int mTimeout = 10000;//默认10000ms
    private GtWebview webView;
    private Timer timer;

    public Boolean isShowing = false;

    public void setBaseURL(String url) {
        this.baseURL = url;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    //支持"zh-cn","zh-hk","zh-tw","ko-kr","ja-jp","en-us".默认"zh-cn".
    public void setLanguage(String lang) {
        this.language = lang;
    }

    //验证标题, 默认无标题, 不宜过长.
    public void setGTTitle(String title) { this.mTitle = title;}

    public void setTimeout(int timeout) {
        this.mTimeout = timeout;
    }

    @Override
    public void onDetachedFromWindow() {

        super.onDetachedFromWindow();
    }

    public void stopLoading() {
        webView.stopLoading();
    }

    public GtDialog (Context context, String id, String challenge, Boolean success) {
        super(context);

        this.context = context;

        this.id = id;
        this.challenge = challenge;
        this.success = success;

        init();
    }

    private void init() {

        int height = DimenTool.getHeightPx(getContext());
        int width = DimenTool.getWidthPx(getContext());
        float scale = getContext().getResources().getDisplayMetrics().density;

        final int WIDTH = 290;

        webView = new GtWebview(getContext());

        if (height < width) {
            width = height * 3 / 4;
        }
        width = width * 4 / 5;
        if ((int)(width / scale + 0.5f) < WIDTH) {
            width = (int)((WIDTH - 0.5f) * scale);
        }

        mWidth = width;

        webView.addJavascriptInterface(new JSInterface(), "JSInterface");

        String gt_mobile_req_url = baseURL
                + "?gt=" + this.id
                + "&challenge=" + this.challenge
                + "&success=" + (this.success ? 1 : 0)
                + "&mType=" + Build.MODEL
                + "&osType=" + "android"
                + "&osVerInt=" + Build.VERSION.RELEASE
                + "&gsdkVerCode=" + "2.16.4.21.1"
                + "&title=" + this.mTitle //验证标题，不宜过长
                + "&lang=" + this.language //支持"zh-cn","zh-hk","zh-tw","ko-kr","ja-jp","en-us".默认"zh-cn"
                + "&debug=" + this.debug
                + "&width=" + (int)(mWidth / scale + 1.5f);//1.5f: fix blank on the webview
        Log.i("GtDialog", "url: " + gt_mobile_req_url);
        webView.loadUrl(gt_mobile_req_url);

        webView.buildLayer();
    }


    public interface GtListener {
        //通知native验证已准备完毕
        void gtCallReady(Boolean status); // true准备完成/false未准备完成
        //通知native关闭验证
        void gtCallClose();
        //通知native验证结果，并准备二次验证
        void gtResult(boolean success, String result);
    }

    private GtListener gtListener;

    public void setGtListener(GtListener listener) {
        gtListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(webView);

        final LayoutParams layoutParams = webView.getLayoutParams();

        layoutParams.width = mWidth;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        webView.setLayoutParams(layoutParams);
    }

    @Override
    public void show() {
        isShowing = true;
        super.show();
    }

    @Override
    public void dismiss() {
        isShowing = false;
        super.dismiss();
    }

    private class GtWebview extends WebView {

        public GtWebview(Context context) {
            super(context);

            init(context);
        }

        private void init(Context context) {
            WebSettings webSettings = this.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setDatabaseEnabled(true);
//            webSettings.setSupportZoom(true);
            webSettings.setUseWideViewPort(true);
            this.setOverScrollMode(View.OVER_SCROLL_NEVER);
            this.setHorizontalScrollBarEnabled(false);
            this.setVerticalScrollBarEnabled(false);
            this.setWebViewClient(mWebViewClientBase);
            this.onResume();
        }

        private WebViewClientBase mWebViewClientBase = new WebViewClientBase();

        private class WebViewClientBase extends WebViewClient {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);

                return true;
            }

            @Override
            public void onPageStarted(final WebView view, String url, Bitmap favicon) {
                // TODO Auto-generated method stub
                Log.i(ACTIVITY_TAG, "webview did start");
                super.onPageStarted(view, url, favicon);

                timer = new Timer();
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        ((Activity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (webView.getProgress() < 100) {
                                    webView.stopLoading();
                                    mDialog.dismiss();
                                    if (gtListener != null) {
                                        gtListener.gtCallReady(false);
                                    }
                                }
                            }
                        });
                    }
                };
                timer.schedule(timerTask, mTimeout, 1);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                timer.cancel();
                timer.purge();
                if (debug) {
                    //当验证无法访问, 可能展示PROXY ERROR
                    if (gtListener != null) {
                        gtListener.gtCallReady(false);
                    }
                    mDialog.show();
                }
                // TODO Auto-generated method stub
                Log.i(ACTIVITY_TAG, "webview did finish");
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                //
                if (gtListener != null) {
                    gtListener.gtCallReady(false);
                }
                super.onReceivedError(view, request, error);
            }

            @Override
            public void onReceivedHttpError(
                    WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                //
                if (gtListener != null) {
                    gtListener.gtCallReady(false);
                }
                mDialog.show();
                super.onReceivedHttpError(view, request, errorResponse);
            }

        }

    }

    public class JSInterface {

        @JavascriptInterface
        public void gtCallBack(String code, String result, String message) {
            int codeInt;
            try {
                codeInt = Integer.parseInt(code);
                if (codeInt == 1) {
                    dismiss();

                    if (gtListener != null) {
                        gtListener.gtResult(true, result);
                    }

                } else {
                    if (gtListener != null) {
                        gtListener.gtResult(false, result);
                    }
                    Toast.makeText(getContext(), "message:" + message,
                            Toast.LENGTH_LONG).show();
                }
            } catch (NumberFormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        @JavascriptInterface
        public void gtCloseWindow() {
            dismiss();
            if (gtListener != null) {
                gtListener.gtCallClose();
            }
        }

        @JavascriptInterface
        public void gtReady() {

            ((Activity)context).runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mDialog.show();
                }
            });

            if (gtListener != null) {
                gtListener.gtCallReady(true);
            }
        }

    }

}
