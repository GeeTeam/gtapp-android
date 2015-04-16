package com.geetest.sdk;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import android.util.Log;

/**
 * Java SDK
 * 
 * @author Zheng
 * @time 2014年7月10日 下午3:29:09
 */
public class GeetestLib {

	/**
	 * SDK版本编号
	 */
	// private final int verCode = 8;

	/**
	 * SDK版本名称
	 */
	private final String verName = "2.15.4.13.1";
	private final String sdkLang = "android_sdk";// SD的语言类型

	private final String baseUrl = "api.geetest.com";
	private final String api_url = "http://" + baseUrl;
	private final String https_api_url = "https://" + baseUrl;// 一些页面是https
	private final int defaultIsMobile = 0;
	private final int defaultMobileWidth = 260;// the default width of the
												// mobile capthca

	/**
	 * 公钥
	 */
	private String captchaId = "";

	/**
	 * 私钥
	 */
	private String privateKey = "";

	/**
	 * the challenge
	 */
	private String challengeId = "";

	/**
	 * set the own private pictures,default is ""
	 */
	private String picId = "";

	/**
	 * he captcha product type,default is 'embed'
	 */
	private String productType = "embed";

	/**
	 * is secure
	 */
	private Boolean isHttps = false;

	public Boolean getIsHttps() {
		return isHttps;
	}

	public void setIsHttps(Boolean isHttps) {
		this.isHttps = isHttps;
	}

	/**
	 * 输出logCat
	 * 
	 * @param msg
	 */
	public static void log_v(String msg) {
		String tag = "geetest";
		Log.v(tag, msg);
	}

	/**
	 * when the productType is popup,it needs to set the submitbutton
	 */
	private String submitBtnId = "submit-button";

	public String getSubmitBtnId() {
		return submitBtnId;
	}

	public void setSubmitBtnId(String submitBtnId) {
		this.submitBtnId = submitBtnId;
	}

	/**
	 * 是否是移动端的
	 */
	private int isMobile = defaultIsMobile;// 1--true,0-false

	public String getChallengeId() {
		return challengeId;
	}

	public void setChallengeId(String challengeId) {
		this.challengeId = challengeId;
	}

	/**
	 * 获取版本编号
	 * 
	 * @author Zheng
	 * @email dreamzsm@gmail.com
	 * @time 2014年7月11日 上午11:07:11
	 * @return
	 */
	public String getVersionInfo() {
		return verName;
	}

	// public void setCaptcha_id(String captcha_id) {
	// this.captcha_id = captcha_id;
	// }

	/**
	 * 一个无参构造函数
	 */
	public GeetestLib() {
	}

	public String getPicId() {
		return picId;
	}

	public void setPicId(String picId) {
		this.picId = picId;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public int getIsMobile() {
		return isMobile;
	}

	public void setIsMobile(int isMobile) {
		this.isMobile = isMobile;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public GeetestLib(String privateKey) {
		this.privateKey = privateKey;
	}

	// public GeetestLib(String privateKey, String captcha_id) {
	// this.privateKey = privateKey;
	// this.captcha_id = captcha_id;
	// }

	// public int getVerCode() {
	// return verCode;
	// }

	public String getVerName() {
		return verName;
	}

	public String getCaptchaId() {
		return captchaId;
	}

	public void setCaptchaId(String captchaId) {
		this.captchaId = captchaId;
	}

	/**
	 * processing before the captcha display on the web front
	 * 
	 * @return
	 */
	public int preProcess() {

		// first check the server status , to handle failback
		// if (getGtServerStatus() != 1) {
		// return 0;
		// }

		// just check the server side register
		if (registerChallenge() != 1) {
			return 0;
		}

		return 1;

	}

	/**
	 * generate the dynamic front source
	 * 
	 * @param different
	 *            product display mode :float,embed,popup
	 * @return
	 */
	public String getGtFrontSource() {

		String base_path = "";
		if (this.isHttps) {
			base_path = this.https_api_url;
		} else {
			base_path = this.api_url;
		}

		String frontSource = String.format(
				"<script type=\"text/javascript\" src=\"%s/get.php?"
						+ "gt=%s&challenge=%s", base_path, this.captchaId,
				this.challengeId);

		if (this.productType.equals("popup")) {
			frontSource += String.format("&product=%s&popupbtnid=%s",
					this.productType, this.submitBtnId);
		} else {
			frontSource += String.format("&product=%s", this.productType);
		}

		frontSource += "\"></script>";

		return frontSource;
	}

	/**
	 * 获取极验的服务器状态
	 * 
	 * @author Zheng
	 * @email dreamzsm@gmail.com
	 * @time 2014年7月10日 下午7:12:38
	 * @return
	 */
	public int getGtServerStatus() {

		try {
			final String GET_URL = api_url + "/check_status.php";
			if (readContentFromGet(GET_URL).equals("ok")) {
				return 1;
			} else {
				System.out.println("gServer is Down");
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * generate a random num
	 * 
	 * @return
	 */
	public int getRandomNum() {

		int rand_num = (int) (Math.random() * 100);
		// System.out.print(rand_num);
		return rand_num;
	}

	/**
	 * Register the challenge
	 * 
	 * @return
	 */
	public int registerChallenge() {
		try {
			String register_url = api_url + "/register.php?gt="
					+ this.captchaId;

			// if (this.productType.equals("popup")) {
			// GET_URL += String.format("&product=%s&popupbtnid=%s",
			// this.productType, this.submitBtnId);
			// } else {
			// GET_URL += String.format("&product=%s", this.productType);
			// }

			// System.out.print(GET_URL);
			String result_str = readContentFromGet(register_url);
			log_v("register:" + result_str);

			// String get_url = api_url + "/get.php?gt=" + this.captchaId
			// + "&challenge=" + result_str;
			// log_v("get_url:" + get_url);
			//
			// String get_result_str = readContentFromGet(get_url);
			// log_v("get_result_str:" + get_result_str);

			// System.out.println(result_str);
			if (32 == result_str.length()) {
				this.challengeId = result_str;
				return 1;
			} else {
				System.out.println("gServer register challenge failed");
				return 0;
			}
		} catch (Exception e) {
			gtlog("exception:register api:" + e.getMessage());
			// e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 读取服务器
	 * 
	 * @author Zheng dreamzsm@gmail.com
	 * @time 2014年7月10日 下午7:11:11
	 * @param getURL
	 * @return
	 * @throws IOException
	 */
	private String readContentFromGet(String getURL) throws IOException {

		URL getUrl = new URL(getURL);
		// 发送数据到服务器并使用Reader读取返回的数据
		StringBuffer sBuffer = new StringBuffer();
		HttpURLConnection connection = (HttpURLConnection) getUrl
				.openConnection();

		try {

			connection.setConnectTimeout(1000);// 设置连接主机超时（单位：毫秒）
			connection.setReadTimeout(1000);// 设置从主机读取数据超时（单位：毫秒）

			// 建立与服务器的连接，并未发送数据

			connection.connect();

			InputStream inStream = null;
			byte[] buf = new byte[1024];
			inStream = connection.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(inStream);

			for (int n; (n = inStream.read(buf)) != -1;) {
				sBuffer.append(new String(buf, 0, n, "UTF-8"));
			}
			inStream.close();
			connection.disconnect();// 断开连接
			return sBuffer.toString();

		} catch (EOFException e) {
			// return sBuffer.toString();
		} catch (Exception e) {
			e.printStackTrace();
			log_v(e.getMessage());

		}
		return sBuffer.toString();
	}

	/**
	 * 判断一个表单对象值是否为空
	 * 
	 * @time 2014年7月10日 下午5:54:25
	 * @param gtObj
	 * @return
	 */
	private boolean objIsEmpty(Object gtObj) {
		if (gtObj != null) {
			return false;
		}
		// && gtObj.toString().trim().length() > 0

		return true;
	}

	/**
	 * the old api use before version code 8(not include)
	 * 
	 * @param challenge
	 * @param validate
	 * @param seccode
	 * @return
	 * @time 2014122_171529 by zheng
	 */
	private boolean validate(String challenge, String validate, String seccode) {
		String host = baseUrl;
		String path = "/validate.php";
		int port = 80;
		if (validate.length() > 0 && checkResultByPrivate(challenge, validate)) {
			String query = "seccode=" + seccode;
			String response = "";
			try {
				response = postValidate(host, path, query, port);
				gtlog(response);
			} catch (Exception e) {
				e.printStackTrace();
			}

			gtlog("md5: " + md5Encode(seccode));

			if (response.equals(md5Encode(seccode))) {
				return true;
			}
		}
		return false;

	}

	/**
	 * Print out log message Use to Debug
	 * 
	 * @time 2014122_151829 by zheng
	 * 
	 * @param message
	 */
	public static void gtlog(String message) {
		System.out.println("gtlog: " + message);
	}

	private boolean checkResultByPrivate(String origin, String validate) {
		String encodeStr = md5Encode(privateKey + "geetest" + origin);
		return validate.equals(encodeStr);
	}

	private String postValidate(String host, String path, String data, int port)
			throws Exception {
		String response = "error";
		// data=fixEncoding(data);
		InetAddress addr = InetAddress.getByName(host);
		Socket socket = new Socket(addr, port);
		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
				socket.getOutputStream(), "UTF8"));
		wr.write("POST " + path + " HTTP/1.0\r\n");
		wr.write("Host: " + host + "\r\n");
		wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
		wr.write("Content-Length: " + data.length() + "\r\n");
		wr.write("\r\n"); // 以空行作为分割
		// 发送数据
		wr.write(data);
		wr.flush();
		// 读取返回信息
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				socket.getInputStream(), "UTF-8"));
		String line;
		while ((line = rd.readLine()) != null) {
			System.out.println(line);
			response = line;
		}
		wr.close();
		rd.close();
		socket.close();
		return response;
	}
	
	
	/*
	 * Function : 发送Post请求到服务器 Param : params请求体内容，encode编码格式 Author : 博客园-依旧淡然
	 */
	public String submitPostData(String post_url,
			Map<String, String> params, String encode) {

		byte[] data = getRequestData(params, encode).toString().getBytes();// 获得请求体

		try {
			URL url = new URL(post_url);
			HttpURLConnection httpURLConnection = (HttpURLConnection) url
					.openConnection();
			httpURLConnection.setConnectTimeout(3000);// 设置连接超时时间
			httpURLConnection.setDoInput(true); // 打开输入流，以便从服务器获取数据
			httpURLConnection.setDoOutput(true); // 打开输出流，以便向服务器提交数据
			httpURLConnection.setRequestMethod("POST"); // 设置以Post方式提交数据
			httpURLConnection.setUseCaches(false); // 使用Post方式不能使用缓存
			
			
			// 设置请求体的类型是文本类型
			httpURLConnection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			// 设置请求体的长度
			httpURLConnection.setRequestProperty("Content-Length",
					String.valueOf(data.length));
			// 获得输出流，向服务器写入数据
			OutputStream outputStream = httpURLConnection.getOutputStream();
			outputStream.write(data);

			int response = httpURLConnection.getResponseCode(); // 获得服务器的响应码
			if (response == HttpURLConnection.HTTP_OK) {
				InputStream inptStream = httpURLConnection.getInputStream();
				return dealResponseResult(inptStream); // 处理服务器的响应结果
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	private StringBuffer getRequestData(Map<String, String> params,
			String encode) {
		StringBuffer stringBuffer = new StringBuffer(); // 存储封装好的请求体信息
		try {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				stringBuffer.append(entry.getKey()).append("=")
						.append(URLEncoder.encode(entry.getValue(), encode))
						.append("&");
			}
			stringBuffer.deleteCharAt(stringBuffer.length() - 1); // 删除最后的一个"&"
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stringBuffer;
	}

	/*
	 * @ Function : 处理服务器的响应结果（将输入流转化成字符串） Param : inputStream服务器的响应输入流
	 * 
	 * @ Author : 博客园-依旧淡然
	 */
	private String dealResponseResult(InputStream inputStream) {
		String resultData = null; // 存储处理结果
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] data = new byte[1024];
		int len = 0;
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
	

	/**
	 * 转为UTF8编码
	 * 
	 * @time 2014年7月10日 下午3:29:45
	 * @param str
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String fixEncoding(String str) throws UnsupportedEncodingException {
		String tempStr = new String(str.getBytes("UTF-8"));
		return URLEncoder.encode(tempStr, "UTF-8");
	}

	/**
	 * md5 加密
	 * 
	 * @time 2014年7月10日 下午3:30:01
	 * @param plainText
	 * @return
	 */
	public String md5Encode(String plainText) {
		String re_md5 = new String();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			byte b[] = md.digest();
			int i;
			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}

			re_md5 = buf.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return re_md5;
	}

}
