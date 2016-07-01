package com.geetest.android.sdk;

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
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

public class Geetest {

    private String captchaURL;
    private String validateURL;

    private String gt;
    private String challenge;
    private int success;
    private CookieManager cookieManager;
    private HttpURLConnection mReadConnection;
    private HttpURLConnection mSubmitConneciton;
    private Timer timer;
    private int responseCode;
    private int mTimeout = 10000;//默认10000ms

    public Boolean isOperating;

    public Geetest(String captchaURL, String validateURL) {
        this.captchaURL = captchaURL;
        this.validateURL = validateURL;
    }

    public String getGt() {
        return this.gt;
    }

    public String getChallenge() {
        return this.challenge;
    }

    public boolean getSuccess() {
        if (success == 1) {
            return true;
        }
        return false;
    }

    public void setTimeout(int timeout) {
        mTimeout = timeout;
    }

    public void cancelReadConnection() {
        if (isOperating) {
            mReadConnection.disconnect();
        }
    }

    public interface GeetestListener {
        void readContentTimeout();
        void submitPostDataTimeout();
    }

    private GeetestListener geetestListener;

    public void setGeetestListener(GeetestListener listener) {
        geetestListener = listener;
    }

    public boolean checkServer() {

        try {

            String info = readContentFromGet(captchaURL);

            if (info.length() > 0) {

                Log.i("Geetest", "checkServer: " + info);

                JSONObject config = new JSONObject(info);

                gt = config.getString("gt");
                challenge = config.getString("challenge");
                success = config.getInt("success");

                if (gt.length() == 32) {
                    return getSuccess();
                }

            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return false;
    }

    private String readContentFromGet(String getURL) throws IOException {

        isOperating = true;

        final StringBuffer sBuffer = new StringBuffer();

        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (responseCode != HttpURLConnection.HTTP_OK || sBuffer.toString().length() == 0) {
                    mReadConnection.disconnect();
                    isOperating = false;
                    if (geetestListener != null) {
                        geetestListener.readContentTimeout();
                    }
                }
            }
        };
        timer.schedule(timerTask, mTimeout, 1);

        URL url = new URL(getURL);

        HttpURLConnection readConnection = (HttpURLConnection)url.openConnection();
        mReadConnection = readConnection;

        cookieManager = new CookieManager();

        Map<String, List<String>> headerFields = readConnection.getHeaderFields();
        List<String> cookiesHeader = headerFields.get("Set-Cookie");

        if(cookiesHeader != null) {
            for (String cookie : cookiesHeader) {
                cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
            }
        }

        try {

            readConnection.setConnectTimeout((int)(mTimeout/2));

            readConnection.setReadTimeout((int) (mTimeout/2));

            readConnection.connect();

            byte[] buf = new byte[1024];

            InputStream inStream = readConnection.getInputStream();

            for (int n; (n = inStream.read(buf)) != -1;) {

                sBuffer.append(new String(buf, 0, n, "UTF-8"));

            }

            inStream.close();

            responseCode = readConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                timer.cancel();
                timer.purge();
                return sBuffer.toString();
            }

            if (responseCode == HttpsURLConnection.HTTP_CLIENT_TIMEOUT || responseCode == -1) {
                if (geetestListener != null) {
                    geetestListener.readContentTimeout();
                }
            }

        } catch (EOFException e) {

            e.printStackTrace();

        } finally {
            mReadConnection.disconnect();
            isOperating = false;
        }
        return "";
    }

    public String submitPostData(Map<String, String> params, String encode) {

        isOperating = true;

        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    mSubmitConneciton.disconnect();
                    isOperating = false;
                    if (geetestListener != null) {
                        geetestListener.submitPostDataTimeout();
                    }
                }
            }
        };
        timer.schedule(timerTask, mTimeout, 1);

        byte[] data = getRequestData(params, encode).toString().getBytes();

        try {
            URL url = new URL(validateURL);
            HttpURLConnection submitConnection = (HttpURLConnection) url.openConnection();
            mSubmitConneciton = submitConnection;
            if(cookieManager.getCookieStore().getCookies().size() > 0) {
                submitConnection.setRequestProperty("Cookie",
                        TextUtils.join(";", cookieManager.getCookieStore().getCookies()));
            }
            submitConnection.setConnectTimeout(mTimeout);
            submitConnection.setDoInput(true);
            submitConnection.setDoOutput(true);
            submitConnection.setRequestMethod("POST");
            submitConnection.setUseCaches(false);

            submitConnection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            submitConnection.setRequestProperty("Content-Length",
                    String.valueOf(data.length));

            OutputStream outputStream = submitConnection.getOutputStream();
            outputStream.write(data);

            int response = submitConnection.getResponseCode();

            if (response == HttpURLConnection.HTTP_OK) {
                timer.cancel();
                timer.purge();
                InputStream inptStream = submitConnection.getInputStream();
                return dealResponseResult(inptStream);
            }

            if (response == -1) {
                if (geetestListener != null) {
                    geetestListener.submitPostDataTimeout();
                }
            }

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            mSubmitConneciton.disconnect();

            isOperating = false;
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
