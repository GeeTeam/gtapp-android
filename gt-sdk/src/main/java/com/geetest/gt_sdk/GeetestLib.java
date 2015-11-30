package com.geetest.gt_sdk;

import android.text.TextUtils;
import android.util.Log;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class GeetestLib {

    private String captchaURL;
    private String validateURL;
    private String captcha;
    private String challenge;
    private CookieManager cookieManager;

    public GeetestLib() {}

    public void setCaptchaURL(String url) {
        this.captchaURL = url;
    }

    public void setValidateURL(String url) {
        this.validateURL = url;
    }

    public String getCaptcha() {

        return this.captcha;
    }

    public String getChallenge() {

        return this.challenge;
    }

    public static void log_v(String msg) {
        String tag = "geetest";
        Log.v(tag, msg);
    }

    public boolean startCaptcha() {

        boolean result = false;

        try {

            String info = readContentFromGet(captchaURL);

            JSONObject config = new JSONObject(info);

            int success = config.getInt("success");

            if (success == 1) {

                captcha = config.getString("gt");
                challenge = config.getString("challenge");

                result = true;

            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return result;
    }

    private String readContentFromGet(String getURL) throws IOException {

        URL url = new URL(getURL);

        StringBuffer sBuffer = new StringBuffer();

        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        cookieManager = new CookieManager();

        Map<String, List<String>> headerFields = connection.getHeaderFields();
        List<String> cookiesHeader = headerFields.get("Set-Cookie");

        if(cookiesHeader != null) {
            for (String cookie : cookiesHeader) {
                cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
            }
        }

        try {

            connection.setConnectTimeout(1000);

            connection.setReadTimeout(1000);

            connection.connect();

            byte[] buf = new byte[1024];

            InputStream inStream = connection.getInputStream();

            for (int n; (n = inStream.read(buf)) != -1;) {

                sBuffer.append(new String(buf, 0, n, "UTF-8"));

            }

            inStream.close();

            connection.disconnect();

        } catch (EOFException e) {

            e.printStackTrace();
            log_v(e.getMessage());

        }

        return sBuffer.toString();
    }

    public String submitPostData(Map<String, String> params, String encode) {

        byte[] data = getRequestData(params, encode).toString().getBytes();

        try {
            URL url = new URL(validateURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            if(cookieManager.getCookieStore().getCookies().size() > 0) {
                httpURLConnection.setRequestProperty("Cookie",
                        TextUtils.join(";", cookieManager.getCookieStore().getCookies()));
            }
            httpURLConnection.setConnectTimeout(3000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setUseCaches(false);

            httpURLConnection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            httpURLConnection.setRequestProperty("Content-Length",
                    String.valueOf(data.length));

            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(data);

            int response = httpURLConnection.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                InputStream inptStream = httpURLConnection.getInputStream();
                return dealResponseResult(inptStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private StringBuffer getRequestData(Map<String, String> params,
            String encode) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append(entry.getKey()).append("=")
                        .append(URLEncoder.encode(entry.getValue(), encode))
                        .append("&");
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer;
    }

    private String dealResponseResult(InputStream inputStream) {
        String resultData;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultData = new String(byteArrayOutputStream.toByteArray());
        return resultData;
    }
}
