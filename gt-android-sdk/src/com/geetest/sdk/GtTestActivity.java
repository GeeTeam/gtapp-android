package com.geetest.sdk;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class GtTestActivity extends Activity {

	private WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_main);
		// webView = (WebView) findViewById(R.id.webView);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		webView = new WebView(this);

		setContentView(webView);
		
//		getWindow().getDecorView();
		
		webView.getLayoutParams().height = LayoutParams.MATCH_PARENT ;
		webView.getLayoutParams().width = LayoutParams.MATCH_PARENT ;
		webView.setLayoutParams(webView.getLayoutParams());

		webView.getSettings().setJavaScriptEnabled(true);
		webView.addJavascriptInterface(new Scripte(), "android");

		ClientInfo clientInfo = ClientInfo.build(getBaseContext());

		String string = clientInfo.toJsonString();

		// webView.loadData(string, "text/html", "UTF-8");

		// webView.loadUrl("file:///android_asset/demo.html");
		//String base_urlString ="http://192.168.1.33:8000";
		String base_urlString ="http://testcenter.geetest.com";
		String gt_mobile_req_url = base_urlString+"/static/gt-mobile/index.html?gt=ad872a4e1a51888967bdb7cb45589605&mobile_info="
				+ string;
		webView.loadUrl(gt_mobile_req_url);
		GeetestLib.log_v(gt_mobile_req_url);

		webView.setWebChromeClient(new WebChromeClient() {

		});
		webView.setWebViewClient(new WebViewClient() {

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public class Scripte {

		@JavascriptInterface
		public void gtCallBack(String code, String result, String message) {
			GeetestLib.log_v("gtCallBack");
			GeetestLib.log_v("code:" + code.toString());
			GeetestLib.log_v("result:" + result.toString());
			GeetestLib.log_v("message:" + message.toString());
			Toast.makeText(getBaseContext(), "code:" + code, Toast.LENGTH_LONG)
					.show();
			Toast.makeText(getBaseContext(), "result:" + result,
					Toast.LENGTH_LONG).show();
			Toast.makeText(getBaseContext(), "message:" + message,
					Toast.LENGTH_LONG).show();
			
			int codeInt;
			try {
				codeInt = Integer.parseInt(code);
				if(codeInt==0){
					//
					finish();
					
				}else{
					Toast.makeText(getBaseContext(), "message:" + message,
							Toast.LENGTH_LONG).show();
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	
		}

		@JavascriptInterface
		public void gtCloseWindow() {
			GeetestLib.log_v("gtCloseWindow");
			finish();
		}

	}

}
