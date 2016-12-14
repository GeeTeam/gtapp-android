package com.geetest.android.sdk;

/**
 * Created by NikoXu on 08/12/2016.
 */
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * 这个类解析了Android 4.0以下的WebView注入Javascript对象引发的安全漏洞。
 *
 * @author LiHong
 * @since 2013-9-30
 */
public class GTWebView extends WebView {

    private static final String ACTIVITY_TAG = "GTWebView";

    private String baseDomain = "mobilestatic.geetest.com";
    private String[] staticIPList = {"115.28.113.153"};

    private static final boolean DEBUG = true;
    private static final String VAR_ARG_PREFIX = "arg";
    private static final String MSG_PROMPT_HEADER = "GtApp:";
    private static final String KEY_INTERFACE_NAME = "obj";
    private static final String KEY_FUNCTION_NAME = "func";
    private static final String KEY_ARG_ARRAY = "args";
    private static final String[] mFilterMethods = {
            "getClass",
            "hashCode",
            "notify",
            "notifyAll",
            "equals",
            "toString",
            "wait",
    };

    private HashMap<String, Object> mJsInterfaceMap = new HashMap<String, Object>();
    private String mJsStringCache = null;
    private Context mContext;
    private Timer domainTimer;
    private Timer ipTimer;

    private String mParamsString;

    public boolean debug = false;

    public interface GtWebViewListener {
        //通知native验证已准备完毕
        void gtCallReady(Boolean status); // true准备完成/false未准备完成
        //通知javascript发生严重错误
        void  gtError();
    }

    private GtWebViewListener gtListener;

    public void setGtWebViewListener(GtWebViewListener listener) {
        gtListener = listener;
    }

    public GTWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public GTWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GTWebView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mContext = context;

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
        this.onResume();

        // 添加默认的Client
        super.setWebChromeClient(new WebChromeClientEx());
        super.setWebViewClient(new WebViewClientEx());

        // 删除掉Android默认注册的JS接口
        removeSearchBoxImpl();
    }

    @Override
    public void loadUrl(String url) {
        domainTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (getProgress() < 100) {
                            stopLoading();
                            new PingTask().execute();

                            domainTimer.cancel();
                            domainTimer.purge();
                        }
                    }
                });
            }
        };
        domainTimer.schedule(timerTask, 5000);

        super.loadUrl(url);
    }

    public void loadIPUrl(String aIP, String paramsString) {
        String mobile_ip_request_url = "http://" + aIP +"/static/appweb/app-index.html" + paramsString;
        Log.i(ACTIVITY_TAG, "load url: " + mobile_ip_request_url);
        final Map<String, String> additionalHttpHeaders = new HashMap<String, String>();
        additionalHttpHeaders.put("Host", baseDomain);
        loadUrl(mobile_ip_request_url, additionalHttpHeaders);
        Log.i(ACTIVITY_TAG, "webview did load ip url");
        ipTimer = new Timer();
        TimerTask timerTask1 = new TimerTask() {
            @Override
            public void run() {
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (getProgress() < 100) {
                            stopLoading();
                            if (gtListener != null) {
                                gtListener.gtCallReady(false);
                            }
                            ipTimer.cancel();
                            ipTimer.purge();
                        }
                    }
                });
            }
        };
        ipTimer.schedule(timerTask1, 10000);
    }

    @Override
    public void addJavascriptInterface(Object obj, String interfaceName) {
        if (TextUtils.isEmpty(interfaceName)) {
            return;
        }

        // 如果在4.2以上，直接调用基类的方法来注册
        if (hasJellyBeanMR1()) {
            super.addJavascriptInterface(obj, interfaceName);
        } else {
            mJsInterfaceMap.put(interfaceName, obj);
        }
    }

    @Override
    public void removeJavascriptInterface(String interfaceName) {
        if (hasJellyBeanMR1()) {
            super.removeJavascriptInterface(interfaceName);
        } else {
            mJsInterfaceMap.remove(interfaceName);
            mJsStringCache = null;
            injectJavascriptInterfaces();
        }
    }

    private boolean removeSearchBoxImpl() {
        if (hasHoneycomb() && !hasJellyBeanMR1()) {
            super.removeJavascriptInterface("searchBoxJavaBridge_");
            return true;
        }

        return false;
    }

    private void injectJavascriptInterfaces() {
        if (!TextUtils.isEmpty(mJsStringCache)) {
            loadJavascriptInterfaces();
            return;
        }

        String jsString = genJavascriptInterfacesString();
        mJsStringCache = jsString;
        loadJavascriptInterfaces();
    }

    private void injectJavascriptInterfaces(WebView webView) {
        if (webView instanceof GTWebView) {
            injectJavascriptInterfaces();
        }
    }

    private void loadJavascriptInterfaces() {
        this.loadUrl(mJsStringCache);
    }

    private String genJavascriptInterfacesString() {
        if (mJsInterfaceMap.size() == 0) {
            mJsStringCache = null;
            return null;
        }

        /*
         * 要注入的JS的格式，其中XXX为注入的对象的方法名，例如注入的对象中有一个方法A，那么这个XXX就是A
         * 如果这个对象中有多个方法，则会注册多个window.XXX_js_interface_name块，我们是用反射的方法遍历
         * 注入对象中的所有带有@JavaScripterInterface标注的方法
         *
         * javascript:(function JsAddJavascriptInterface_(){
         *   if(typeof(window.XXX_js_interface_name)!='undefined'){
         *       console.log('window.XXX_js_interface_name is exist!!');
         *   }else{
         *       window.XXX_js_interface_name={
         *           XXX:function(arg0,arg1){
         *               return prompt('MyApp:'+JSON.stringify({obj:'XXX_js_interface_name',func:'XXX_',args:[arg0,arg1]}));
         *           },
         *       };
         *   }
         * })()
         */

        Iterator<Entry<String, Object>> iterator = mJsInterfaceMap.entrySet().iterator();
        // Head
        StringBuilder script = new StringBuilder();
        script.append("javascript:(function JsAddJavascriptInterface_(){");

        // Add methods
        try {
            while (iterator.hasNext()) {
                Entry<String, Object> entry = iterator.next();
                String interfaceName = entry.getKey();
                Object obj = entry.getValue();

                createJsMethod(interfaceName, obj, script);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // End
        script.append("})()");

        return script.toString();
    }

    private void createJsMethod(String interfaceName, Object obj, StringBuilder script) {
        if (TextUtils.isEmpty(interfaceName) || (null == obj) || (null == script)) {
            return;
        }

        Class<? extends Object> objClass = obj.getClass();

        script.append("if(typeof(window.").append(interfaceName).append(")!='undefined'){");
        if (DEBUG) {
            script.append("    console.log('window." + interfaceName + "_js_interface_name is exist!!');");
        }

        script.append("}else {");
        script.append("    window.").append(interfaceName).append("={");

        // Add methods
        Method[] methods = objClass.getMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            // 过滤掉Object类的方法，包括getClass()方法，因为在Js中就是通过getClass()方法来得到Runtime实例
            if (filterMethods(methodName)) {
                continue;
            }

            script.append("        ").append(methodName).append(":function(");
            // 添加方法的参数
            int argCount = method.getParameterTypes().length;
            if (argCount > 0) {
                int maxCount = argCount - 1;
                for (int i = 0; i < maxCount; ++i) {
                    script.append(VAR_ARG_PREFIX).append(i).append(",");
                }
                script.append(VAR_ARG_PREFIX).append(argCount - 1);
            }

            script.append(") {");

            // Add implementation
            if (method.getReturnType() != void.class) {
                script.append("            return ").append("prompt('").append(MSG_PROMPT_HEADER).append("'+");
            } else {
                script.append("            prompt('").append(MSG_PROMPT_HEADER).append("'+");
            }

            // Begin JSON
            script.append("JSON.stringify({");
            script.append(KEY_INTERFACE_NAME).append(":'").append(interfaceName).append("',");
            script.append(KEY_FUNCTION_NAME).append(":'").append(methodName).append("',");
            script.append(KEY_ARG_ARRAY).append(":[");
            //  添加参数到JSON串中
            if (argCount > 0) {
                int max = argCount - 1;
                for (int i = 0; i < max; i++) {
                    script.append(VAR_ARG_PREFIX).append(i).append(",");
                }
                script.append(VAR_ARG_PREFIX).append(max);
            }

            // End JSON
            script.append("]})");
            // End prompt
            script.append(");");
            // End function
            script.append("        }, ");
        }

        // End of obj
        script.append("    };");
        // End of if or else
        script.append("}");
    }

    private boolean handleJsInterface(WebView view, String url, String message, String defaultValue,
                                      JsPromptResult result) {
        String prefix = MSG_PROMPT_HEADER;
        if (!message.startsWith(prefix)) {
            return false;
        }

        String jsonStr = message.substring(prefix.length());
        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            String interfaceName = jsonObj.getString(KEY_INTERFACE_NAME);
            String methodName = jsonObj.getString(KEY_FUNCTION_NAME);
            JSONArray argsArray = jsonObj.getJSONArray(KEY_ARG_ARRAY);
            Object[] args = null;
            if (null != argsArray) {
                int count = argsArray.length();
                if (count > 0) {
                    args = new Object[count];

                    for (int i = 0; i < count; ++i) {
                        args[i] = argsArray.get(i);
                    }
                }
            }

            if (invokeJSInterfaceMethod(result, interfaceName, methodName, args)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        result.cancel();
        return false;
    }

    private boolean invokeJSInterfaceMethod(JsPromptResult result,
                                            String interfaceName, String methodName, Object[] args) {

        boolean succeed = false;
        final Object obj = mJsInterfaceMap.get(interfaceName);
        if (null == obj) {
            result.cancel();
            return false;
        }

        Class<?>[] parameterTypes = null;
        int count = 0;
        if (args != null) {
            count = args.length;
        }

        if (count > 0) {
            parameterTypes = new Class[count];
            for (int i = 0; i < count; ++i) {
                parameterTypes[i] = getClassFromJsonObject(args[i]);
            }
        }

        try {
            Method method = obj.getClass().getMethod(methodName, parameterTypes);
            Object returnObj = method.invoke(obj, args); // 执行接口调用
            boolean isVoid = returnObj == null || returnObj.getClass() == void.class;
            String returnValue = isVoid ? "" : returnObj.toString();
            result.confirm(returnValue); // 通过prompt返回调用结果
            succeed = true;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        result.cancel();
        return succeed;
    }

    private Class<?> getClassFromJsonObject(Object obj) {
        Class<?> cls = obj.getClass();

        // js对象只支持int boolean string三种类型
        if (cls == Integer.class) {
            cls = Integer.TYPE;
        } else if (cls == Boolean.class) {
            cls = Boolean.TYPE;
        } else {
            cls = String.class;
        }

        return cls;
    }

    private boolean filterMethods(String methodName) {
        for (String method : mFilterMethods) {
            if (method.equals(methodName)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    private boolean hasJellyBeanMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    private class WebChromeClientEx extends WebChromeClient {
        @Override
        public final void onProgressChanged(WebView view, int newProgress) {
            injectJavascriptInterfaces(view);
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public final boolean onJsPrompt(WebView view, String url, String message,
                                        String defaultValue, JsPromptResult result) {
            if (view instanceof GTWebView) {
                if (handleJsInterface(view, url, message, defaultValue, result)) {
                    return true;
                }
            }

            return super.onJsPrompt(view, url, message, defaultValue, result);
        }

        @Override
        public final void onReceivedTitle(WebView view, String title) {
            injectJavascriptInterfaces(view);
        }
    }

    private class WebViewClientEx extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            mContext.startActivity(intent);

            return true;
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            // TODO Auto-generated method stub
            if (gtListener != null) {
                gtListener.gtCallReady(false);
            }
            super.onReceivedError(view, request, error);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            // TODO Auto-generated method stub
            if (gtListener != null) {
                gtListener.gtCallReady(false);
            }
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

//        @TargetApi(23)
//        @Override
//        public void onReceivedHttpError(
//                WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
//            //
//            if (gtListener != null) {
//                gtListener.gtCallReady(false);
//            }
//            super.onReceivedHttpError(view, request, errorResponse);
//        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            //handler.cancel(); 默认的处理方式，WebView变成空白页
            //handler.process();接受证书
            //handleMessage(Message msg); 其他处理
            if (gtListener != null) {
                gtListener.gtError();
            }
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            injectJavascriptInterfaces(view);
            super.onLoadResource(view, url);
        }

        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            injectJavascriptInterfaces(view);
            super.doUpdateVisitedHistory(view, url, isReload);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            Log.i(ACTIVITY_TAG, "webview did start");
            injectJavascriptInterfaces(view);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (debug) {
                //当验证无法访问, 可能展示PROXY ERROR
                if (gtListener != null) {
                    gtListener.gtCallReady(false);
                }
            }
            // TODO Auto-generated method stub
            Log.i(ACTIVITY_TAG, "webview did finish");
            injectJavascriptInterfaces(view);
            super.onPageFinished(view, url);
        }
    }

    private class PingTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            //"115.28.113.153"
            for (int i = 0; i < staticIPList.length; i++) {
                String ip = ping(staticIPList[i], 0);
                if (null != ip) {
                    return ip;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (null != result) {
                loadIPUrl(result, mParamsString);
            }
            else {
                if (gtListener != null) {
                    gtListener.gtCallReady(false);
                }
            }
        }

        private String ping(String host, int port) {
            if (port == 0) port = 80;

            Socket connect = new Socket();
            try {
                connect.connect(new InetSocketAddress(host, port), 2 * 1000);
                Log.i(ACTIVITY_TAG, "Ping " + host);
                if (connect.isConnected()) {
                    return host;
                }
                else {
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    connect.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}
