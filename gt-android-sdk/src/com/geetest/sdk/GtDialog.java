package com.geetest.sdk;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
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

	SdkInit initData = new SdkInit();
	
	private static GtDialog mDialog;
	
	public static GtDialog newInstance(SdkInit initData){
		if(mDialog==null){
			mDialog = new GtDialog(initData);
		}
		return mDialog;
	}

	private GtDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	private GtDialog(SdkInit initData) {
		super(initData.getContext());
		this.initData = initData;
	}
	
	@Override
	public void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();
		mDialog = null;
	}

	private WebView webView;

	public interface GtListener {
		public void closeGt();

		public void gtResult(boolean success, String result);
	}

	private GtListener gtListener;

	public void setGtListener(GtListener listener) {
		gtListener = listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_main);
		// webView = (WebView) findViewById(R.id.webView);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		webView = new WebView(getContext());

		// LinearLayout layout = new LinearLayout(getContext());

		setContentView(webView);

		// LinearLayout.LayoutParams lp = new
		// LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		// layout.addView(webView,lp);

		// getWindow().getDecorView();

		webView.getLayoutParams().height = DimenTool.getHeightPx(getContext()) / 2;
		webView.getLayoutParams().width = LayoutParams.MATCH_PARENT;
		webView.setLayoutParams(webView.getLayoutParams());
		
		WebSettings settings = webView.getSettings();
	    settings.setLoadWithOverviewMode(true);
	    settings.setUseWideViewPort(true);
	    settings.setJavaScriptEnabled(true);
		webView.addJavascriptInterface(new Scripte(), "android");

		ClientInfo clientInfo = ClientInfo.build(getContext());

		String mobile_info = clientInfo.toJsonString();

		// webView.loadData(string, "text/html", "UTF-8");

		// webView.loadUrl("file:///android_asset/demo.html");
		// String base_urlString ="http://192.168.1.33:8000";
		// String base_urlString = "http://testcenter.geetest.com";
		// String relative_pathString = "/static/gt-mobile/android-index.html";
		String gt_mobile_req_url = "http://static.geetest.com/static/appweb/android-index.html"
				+ "?gt="
				+ initData.getCaptcha_id()// ad872a4e1a51888967bdb7cb45589605
				+ "&product=embed"
				+ "&challenge=" + initData.getChallenge_id()
				+ "&mobileInfo=" + mobile_info;

		GeetestLib.log_v(gt_mobile_req_url);

		webView.loadUrl(gt_mobile_req_url);
		GeetestLib.log_v(gt_mobile_req_url);

		webView.setWebChromeClient(new WebChromeClient() {

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				// TODO Auto-generated method stub
				super.onProgressChanged(view, newProgress);
				if (newProgress == 100) {
					webView.getLayoutParams().height = LayoutParams.MATCH_PARENT;
					webView.getLayoutParams().width = LayoutParams.MATCH_PARENT;
					webView.setLayoutParams(webView.getLayoutParams());
				}
			}
		});
		webView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
			}

		});

	}

	public class Scripte {

		/**
		 * 验证结果返回
		 * 
		 * @time 2015年4月13日 下午4:38:55
		 * @param code
		 * @param result
		 * @param message
		 */
		@JavascriptInterface
		public void gtCallBack(String code, String result, String message) {
			GeetestLib.log_v("gtCallBack");
			GeetestLib.log_v("code:" + code.toString());
			GeetestLib.log_v("result:" + result.toString());
			GeetestLib.log_v("message:client result" + message.toString());
			// Toast.makeText(getContext(), "code:" + code, Toast.LENGTH_LONG)
			// .show();
			// Toast.makeText(getContext(), "result:" + result,
			// Toast.LENGTH_LONG).show();
			// Toast.makeText(getContext(), "message:" + message,
			// Toast.LENGTH_LONG).show();
			//
			int codeInt;
			try {
				codeInt = Integer.parseInt(code);
				if (codeInt == 1) {
					//
					dismiss();

					if (gtListener != null) {
						gtListener.gtResult(true, result);
						;
					}

				} else {
					if (gtListener != null) {
						gtListener.gtResult(false, result);
						;
					}
					Toast.makeText(getContext(), "message:" + message,
							Toast.LENGTH_LONG).show();
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		/**
		 * 关闭窗口
		 * 
		 * @time 2015年4月13日 下午4:38:40
		 */
		@JavascriptInterface
		public void gtCloseWindow() {
			GeetestLib.log_v("gtCloseWindow");
			dismiss();
			if (gtListener != null) {
				gtListener.closeGt();
			}
		}

	}

}
