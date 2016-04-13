package com.geetest.android.sdk;

import java.lang.Runnable;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LogPrinter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
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
    private Boolean debug = false;

    private Dialog mDialog = this;
    private Context context;
    private int mWidth;
    private GtWebview webView;

    public void setBaseURL(String url) {
        this.baseURL = url;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    @Override
    public void onDetachedFromWindow() {

        super.onDetachedFromWindow();
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
                + "&success=" + (!this.success ? 0 : 1)
                + "&product=" + this.product
                + "&debug=" + this.debug
                + "&width=" + (int)(width / scale + 0.5f);

        webView.loadUrl(gt_mobile_req_url);

        webView.buildLayer();
    }


    public interface GtListener {
        //通知native验证已准备完毕
        void gtCallReady();
        //通知native关闭验证
        void closeGt();
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
                Log.i(ACTIVITY_TAG, "start loading");

                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // TODO Auto-generated method stub
                Log.i(ACTIVITY_TAG, "webview did start");
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // TODO Auto-generated method stub
                Log.i(ACTIVITY_TAG, "webview did finish");
                super.onPageFinished(view, url);

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
                gtListener.closeGt();
            }
        }

        @JavascriptInterface
        public void gtReady() {
            if (gtListener != null) {
                gtListener.gtCallReady();
            }
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDialog.show();
                }
            });
        }

    }

}
