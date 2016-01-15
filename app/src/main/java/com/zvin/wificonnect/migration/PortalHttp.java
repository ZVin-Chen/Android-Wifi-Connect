/**
 * 
 */
package com.zvin.wificonnect.migration;

import android.os.Build;

import com.zvin.wificonnect.util.Const;
import com.zvin.wificonnect.util.LogUtil;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * portalHttp类
 * @author lixiujuan
 *
 */
public class PortalHttp {
	static final String TAG = "PortalHttp--->";
	
	public static final String GD_JSESSIONID = "JSESSIONID=";

	public static final String BJ_PHPSESSID = "PHPSESSID=";

	public static final String BIGIP_SERVER_AIPORTAL = "BIGipServerAIPortal_7080_pool";

	private HttpURLConnection httpConn;
	private String cookie = null;
	private String response = null;
	private String host = null;
	private String response302 = null;

	/*
	 * Getters/Setters methods
	 */
	public String getHost() {
		return host;
	}

	public String getResponse() {
		return response;
	}
	
	public String getResponse302() {
		return response302;
	}
	
	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public  PortalHttp() {
		disableConnectionReuseIfNecessary();
	}
	
	 private void disableConnectionReuseIfNecessary() {
		   // Work around pre-Froyo bugs in HTTP connection reuse.
		   if (Integer.parseInt(Build.VERSION.SDK) < 8) {
		     System.setProperty("http.keepAlive", "false");
		 }
	}
	 
	private  void initHttpConn(boolean flagHttps, String strHttpUrl, String method,
			int connectTimeout,int readTimeout) throws Exception {
		initHttpConn(flagHttps, strHttpUrl, method, connectTimeout, readTimeout, false);
	}
	
	private  void initHttpConn(boolean flagHttps, String strHttpUrl, String method,
			int connectTimeout,int readTimeout, boolean needRetry) throws Exception {
		try {
			// when using HTTPS
			if (flagHttps) {
				SSLContext sslCont = SSLContext.getInstance("TLS");
				sslCont.init(null, new TrustManager[] { new myTrustManager() }, new SecureRandom());

				HttpsURLConnection.setDefaultSSLSocketFactory(sslCont.getSocketFactory());
				HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
				httpConn = (HttpsURLConnection) new URL(strHttpUrl).openConnection();

				// when using HTTP
			} else {
				httpConn = (HttpURLConnection) new URL(strHttpUrl).openConnection();
			}

			HttpURLConnection.setFollowRedirects(false);
			httpConn.setDoInput(true);
			if ("POST".equals(method)) {
				httpConn.setDoOutput(true);
			} else {
				httpConn.setDoOutput(false);
				
			}
			httpConn.setReadTimeout(readTimeout);
			httpConn.setConnectTimeout(connectTimeout);
			/*
			 * httpConn.setRequestProperty("Accept-Language", "zh-cn");
			 * httpConn.setRequestProperty("Connection", "Keep-Alive");
			 * httpConn.setRequestProperty("Cache-Control", "no-cache");
			 * httpConn.setRequestProperty("Accept-Charset", "UTF-8");
			 */
			httpConn.setRequestProperty("Accept-Charset", "gb2312");
			httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			httpConn.setRequestProperty("User-Agent", "CMCCWIFI");
			// httpConn.setRequestProperty("Content-Type", "text/html");
			httpConn.setInstanceFollowRedirects(false);
			httpConn.setUseCaches(false);
		} catch (InterruptedIOException e) {
			// Timeout
			LogUtil.e(TAG, " initHttpConn InterruptedIOException: " + e.toString());
			throw e;
		} catch (IOException e) {
			LogUtil.e(TAG, "initHttpConn IOException: " + e.toString());
			if (needRetry) {
				initHttpConn(flagHttps, strHttpUrl, method, connectTimeout, readTimeout, false);
			} else {
				throw e;
			}
		} catch (Exception e) {
			LogUtil.i(TAG, "initHttpConn exception: " + e.toString());
			if (needRetry) {
				initHttpConn(flagHttps, strHttpUrl, method, connectTimeout, readTimeout, false);
			} else {
				throw e;
			}
		}
	}

	public  boolean sendDataPost(boolean isHttps, String strUrl, String outData,
			int connectTimeout,int readTimeout) throws CMCCWifiAuthException {
		try {
			return sendDataPost(isHttps, strUrl, outData, connectTimeout,
					readTimeout, false);
		} catch (RetryAgainException e) {
			e.printStackTrace();
			// TODO Auto-generated catch block
			//重试一次
			try {
				LogUtil.i(TAG, "重试一次");
				return sendDataPost(isHttps, strUrl, outData, connectTimeout,
						readTimeout, false);
			} catch (RetryAgainException e1) {
				//如果这里有异常，说明程序逻辑有问题
				LogUtil.i(TAG, "程序逻辑有问题");
				throw new CMCCWifiAuthException(
						CMCCWifiAuthException.EXCEPTION_TYPE_NOTVALID_RESCODE,
						null, removeSensitiveInfo(strUrl + "?" + outData));
				
			}
		}
	}
	
	public  boolean sendDataPostNoRetry(boolean isHttps, String strUrl, String outData,
			int connectTimeout,int readTimeout) throws CMCCWifiAuthException {
		try {
			return sendDataPost(isHttps, strUrl, outData, connectTimeout, readTimeout, false);
		} catch (RetryAgainException e) {
		}
		return false;
	}
	
	private synchronized boolean sendDataPost(boolean isHttps, String strUrl, String outData,
			int connectTimeout,int readTimeout, boolean needRetry) throws CMCCWifiAuthException,RetryAgainException {
		long postStartTime = System.currentTimeMillis();
		try {
			initHttpConn(isHttps, strUrl, "POST",connectTimeout,readTimeout);
			response = null;

			// send out data using POST method
			httpConn.setRequestMethod("POST");
			// add "Cookie" value
			// for(String key:map.keySet()){
			// httpConn.setRequestProperty(key, newValue);
			// }

			httpConn.setRequestProperty("Cookie", cookie);
			//RunLogCat.i("AuthenPortal", "____cookie of SendDataPost(): " + cookie);
			LogUtil.i(TAG, "____cookie of SendDataPost(): " + cookie);

			httpConn.connect();

			DataOutputStream dout = new DataOutputStream(httpConn.getOutputStream());
			dout.write(outData.getBytes());
			dout.flush();
			dout.close();

			// receive the response
			int rspCode = httpConn.getResponseCode();
			LogUtil.i(TAG, "POST response code: " + rspCode);
			//RunLogCat.i(TAG, "POST response code: " + rspCode);

			if (rspCode == -1) {
				// response code为-1
				if (needRetry) {
					// 重试一次
//					return sendDataPost(isHttps, strUrl, outData, connectTimeout,
//							readTimeout, false);
					throw new RetryAgainException("重试一次");
				} else {
					throw new CMCCWifiAuthException(
							CMCCWifiAuthException.EXCEPTION_TYPE_NOTVALID_RESCODE,
							null, removeSensitiveInfo(strUrl + "?" + outData));
				}
			}
			
			/*// process HTTP 200-OK
			if (rspCode == HttpURLConnection.HTTP_OK) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						httpConn.getInputStream(), "gb2312"));

				StringBuffer rspBuf = new StringBuffer();
				
				 * while((strLine=br.readLine()) != null) {
				 * rspBuf.append(strLine + "\r\n"); }
				 
				int count = 1024*1024;
				int result = -1;
				char[] readChars = new char[count];
				String temp = null;
				do {
					result = br.read(readChars, 0, count);
					if (result > 0) {
						temp = new String(readChars, 0, result);
						rspBuf.append(temp);
					}

				} while (result != -1);
				response = rspBuf.toString();

				Utils.writeLog("PortalHttp  Response of SendDataPost(): " + response);
				br.close();

				httpConn.getInputStream().close();
				httpConn.disconnect();
				return true;
			} else if (rspCode == HttpURLConnection.HTTP_MOVED_TEMP) {
				int i = 0;
				// process HTTP 302-redirection
//				while (rspCode != HttpURLConnection.HTTP_OK) {
//					if (rspCode == HttpURLConnection.HTTP_MOVED_TEMP) {
						String location;
						location = httpConn.getHeaderField("Location");

						++i;
						//RunLogCat.i("AuthenPortal", i + "____RspCode of SendDataPost(): " + rspCode);
						//RunLogCat.i("AuthenPortal", i + "____location of SendDataPost(): " + location);
						Utils.writeLog(i + "____location of SendDataPost(): " + location);

						httpConn.disconnect();

						PortalHttp getHttp = new PortalHttp();
						getHttp.setCookie(cookie);
						boolean getResult = getHttp.sendDataGet(location.startsWith("https"), location);
						if (getResult) {
							// 302后，Get成功
							RunLogCat.i("PortalHttp", "SendDataPost get " + location + " success");
							Utils.writeLog("SendDataPost get " + location + " success");
							rspCode = HttpURLConnection.HTTP_OK;
							response = getHttp.getResponse();
							host = getHttp.getHost();
							return true;
						} else {
							RunLogCat.i("PortalHttp", "SendDataPost get " + location + " failed");
							Utils.writeLog("SendDataPost get " + location + "failed");
							return false;
						}
//					}
//				}
*/						
			int i = 0;
			// process HTTP 302-redirection
			while (rspCode != HttpURLConnection.HTTP_OK) {

				if (rspCode == HttpURLConnection.HTTP_MOVED_TEMP) {
					String location;
					location = httpConn.getHeaderField("Location");

					++i;
					//RunLogCat.i("AuthenPortal", i + "____RspCode of SendDataPost(): " + rspCode);
					//RunLogCat.i("AuthenPortal", i + "____location of SendDataPost(): " + location);
					LogUtil.i(TAG, i + "____location of SendDataPost(): " + location);

					httpConn.disconnect();

					initHttpConn(location.startsWith("https"), location, "GET",connectTimeout,readTimeout);
					httpConn.setRequestMethod("GET");
					// add "Cookie" value
					httpConn.setRequestProperty("Cookie", cookie);
					//RunLogCat.i("AuthenPortal", i + "____cookie of SendDataPost(): " + cookie);
					LogUtil.i(TAG, i + "____cookie of SendDataPost(): " + cookie);

					httpConn.connect();

					rspCode = httpConn.getResponseCode();
					LogUtil.i(TAG, i + " GET response code after 302: " + rspCode);
				} else {
					break;
				}
			}

			// process HTTP 200-OK
			if (rspCode == HttpsURLConnection.HTTP_OK) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						httpConn.getInputStream(), "gb2312"));

				StringBuffer rspBuf = new StringBuffer();
				/*
				 * while((strLine=br.readLine()) != null) {
				 * rspBuf.append(strLine + "\r\n"); }
				 */
				//memory alloc too much
//				int count = 1024*1024;
				int count = 1024;
				int result = -1;
				char[] readChars = new char[count];
				String temp = null;
				do {
					result = br.read(readChars, 0, count);
					if (result > 0) {
						temp = new String(readChars, 0, result);
						rspBuf.append(temp);
					}

				} while (result != -1);
				response = rspBuf.toString();

			    LogUtil.i(TAG, "PortalHttp  Response of SendDataPost(): " + response);
				br.close();

				httpConn.getInputStream().close();
				httpConn.disconnect();
				return true;
			} else {
				// response code非200和302
				// 也要重试
				if (needRetry) {
					// 重试一次
//					return sendDataPost(isHttps, strUrl, outData, connectTimeout,
//							readTimeout, false);
					throw new RetryAgainException("重试一次");
				} else {
//					throw new CMCCWifiAuthException(e, removeSensitiveInfo(strUrl + "?" + outData));
					throw new CMCCWifiAuthException(CMCCWifiAuthException.EXCEPTION_TYPE_RESCODE_NOTOK,
							"response code " + rspCode, removeSensitiveInfo(strUrl + "?" + outData));
				}
			}
		} catch (InterruptedIOException e) {
			e.printStackTrace();
			LogUtil.i(TAG, "sendDataPost InterruptedIOException: " + e.toString());
			// InterruptedIOException类型异常，包装成CMCCWifiAuthException抛出
			// 超时不重试
			long postEndTime = System.currentTimeMillis();
			// 不到超时时间也抛Timeout异常的情况，也要重试
			if (needRetry 
					// 超时也重试
					&& (postStartTime != 0 && postEndTime - postStartTime > 0
					&& postEndTime - postStartTime < 60000)) {
				// 重试一次
//				return sendDataPost(isHttps, strUrl, outData, connectTimeout,
//						readTimeout, false);
				throw new RetryAgainException("重试一次");
			} else {
				throw new CMCCWifiAuthException(e, removeSensitiveInfo(strUrl + "?" + outData));
			}
		} catch (UnknownHostException e) {
			LogUtil.i(TAG, "sendDataPost UnknownHostException: " + e.toString());
			if (needRetry) {
				// 重试一次
//				return sendDataPost(isHttps, strUrl, outData, connectTimeout,
//						readTimeout, false);
				throw new RetryAgainException("重试一次");
			} else {
				throw new CMCCWifiAuthException(e, removeSensitiveInfo(strUrl + "?" + outData));
			}
		} catch (IOException e) {
			e.printStackTrace();
			LogUtil.i(TAG, "sendDataPost IOException: " + e.toString());
			if (needRetry) {
				// 重试一次
//				return sendDataPost(isHttps, strUrl, outData, connectTimeout,
//						readTimeout, false);
				throw new RetryAgainException("重试一次");
			} else {
				throw new CMCCWifiAuthException(e, removeSensitiveInfo(strUrl + "?" + outData));
			}
		} catch (Exception e) {
			// java.io.IOException: Read error: I/O error during system call,
			// Connection timed out
			// message: Read error: I/O error during system call, Connection
			// timed out
			e.printStackTrace();
			//RunLogCat.i(TAG, "sendDataPost exception: " + e.toString());
			LogUtil.i(TAG, "sendDataPost exception: " + e.toString() + " message:" + e.getMessage());
			if (e.getMessage() != null) {
				//RunLogCat.i(TAG, "sendDataPost exception: " + e.toString() + " message:" + e.getMessage());
				LogUtil.i(TAG, "sendDataPost exception: " + e.toString() + " message:" + e.getMessage());
				if (e.getMessage().indexOf("I/O error during system call") != -1
						|| e.getMessage().indexOf("Connection timed out") != -1) {
					// if (sendDataGet(false, "http://www.baidu.com")) {
					// if (response != null && response.indexOf("www.baidu.com")
					// != -1){
					// return false;
					// }
					// }
					// response = "I/O error during system call";
					response = e.getMessage();
					// return true;
				}
			}
			if (needRetry) {
				// 重试一次
//				return sendDataPost(isHttps, strUrl, outData, connectTimeout,
//						readTimeout, false);
				throw new RetryAgainException("重试一次");
			} else {
				throw new CMCCWifiAuthException(e, removeSensitiveInfo(strUrl + "?" + outData));
			}
		}finally{
			LogUtil.i(TAG, "sendDataPost finally httpConn.disconnect()");
			try{
				httpConn.disconnect();
				}catch(Exception e){
			}
		}
	}
	public  String sendGetOneStep(String url) throws CMCCWifiAuthException {
		try {
			return sendGetOneStep(url, true);
		} catch (RetryAgainException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//重试一次
			try {
				LogUtil.i(TAG, "重试一次");
				return  sendGetOneStep(url, false);
			} catch (RetryAgainException e1) {
				//如果这里有异常，说明程序逻辑有问题
				LogUtil.i(TAG, "程序逻辑有问题");
				e1.printStackTrace();
				throw new CMCCWifiAuthException(
						CMCCWifiAuthException.EXCEPTION_TYPE_NOTVALID_RESCODE,
						null, removeSensitiveInfo(url));
				
			}
		}
	}
	
	public synchronized String sendGetOneStep(String url, boolean needRetry) throws CMCCWifiAuthException,RetryAgainException {
		try {
			LogUtil.i(TAG, "sendGetOneStep get url=" + url + " | needRetry=" + needRetry);
			initHttpConn(false, url, "GET", (int)Const.http_timeout,
					(int)Const.http_timeout);
			response = null;
			host = null;

			// send out data using POST method
			httpConn.setRequestMethod("GET");
			httpConn.connect();

			// receive the response
			int rspCode = httpConn.getResponseCode();
			//RunLogCat.i(TAG, "sendGetOneStep response code: " + rspCode);
			LogUtil.i(TAG, "sendGetOneStep response code: " + rspCode);

			if (rspCode == -1) {
				// response code为-1
				if (needRetry) {
					// 重试一次
					//return sendGetOneStep(url, false);
					throw new RetryAgainException("重试一次");
				} else {
					throw new CMCCWifiAuthException(
							CMCCWifiAuthException.EXCEPTION_TYPE_NOTVALID_RESCODE,
							null, removeSensitiveInfo(url));
				}
			}
			
			if (rspCode == HttpURLConnection.HTTP_MOVED_TEMP) {
				String location;
				location = httpConn.getHeaderField("Location");
				LogUtil.i(TAG, "sendGetOneStep 302 location: " + location);
				
				try {
					if (httpConn.getInputStream() != null) {
						BufferedReader br = new BufferedReader(new InputStreamReader(
								httpConn.getInputStream(), "gb2312"));
						StringBuffer rspBuf = new StringBuffer();
						int count = 1024;
						int result = -1;
						char[] readChars = new char[count];
						String temp = null;
						do {
							result = br.read(readChars, 0, count);
							if (result > 0) {
								temp = new String(readChars, 0, result);
								rspBuf.append(temp);
							}
						} while (result != -1);
						response302 = rspBuf.toString();
						LogUtil.i(TAG, "302, response body is " + response302);
					} else {
						//RunLogCat.i(TAG, "302, InputStream is null!");
						LogUtil.i(TAG, "302, InputStream is null!");
					}
				} catch (Exception e) {
					LogUtil.i(TAG, "read InputStream when 302, exception: " + e.toString());
				}
				
				return location;
			} else if (rspCode == HttpURLConnection.HTTP_OK) {
				host = httpConn.getURL().getHost();
				// extract the cookie property
				String setCookie = readCookie(httpConn);
				if (setCookie != null) {
					cookie = setCookie;
				}

				BufferedReader br = new BufferedReader(new InputStreamReader(
						httpConn.getInputStream(), "gb2312"));
				StringBuffer rspBuf = new StringBuffer();
				int count = 1024;
				int result = -1;
				char[] readChars = new char[count];
				String temp = null;
				do {
					result = br.read(readChars, 0, count);
					if (result > 0) {
						temp = new String(readChars, 0, result);
						rspBuf.append(temp);
					}
				} while (result != -1);
				response = rspBuf.toString();

				//RunLogCat.i("PortalHttp", "response of sendGetOneStep: " + response);
				LogUtil.i(TAG, "PortalHttp response of sendGetOneStep: " + response);
				br.close();

				httpConn.getInputStream().close();
				httpConn.disconnect();
				return Const.BAIDU_URL;
			} else {
				// response code非200和302
				// 也要重试
				if (needRetry) {
					// 重试一次
					//return sendGetOneStep(url, false);
					throw new RetryAgainException("重试一次");
				} else {
					throw new CMCCWifiAuthException(CMCCWifiAuthException.EXCEPTION_TYPE_RESCODE_NOTOK,
							"response code " + rspCode, removeSensitiveInfo(url));
				}
			}
		} catch (InterruptedIOException e) {
			LogUtil.i(TAG, "sendGetOneStep InterruptedIOException: " + e.toString());
			// InterruptedIOException类型异常，包装成CMCCWifiAuthException抛出
			// 不重试
			throw new CMCCWifiAuthException(e, removeSensitiveInfo(url));
		} catch (UnknownHostException e) {
			LogUtil.i(TAG, "sendGetOneStep UnknownHostException: " + e.toString());
			if (needRetry) {
				// 重试一次
				//return sendGetOneStep(url, false);
				throw new RetryAgainException("重试一次");
			} else {
				throw new CMCCWifiAuthException(e, removeSensitiveInfo(url));
			}
		} catch (IOException e) {
			LogUtil.i(TAG, "sendGetOneStep IOException: " + e.toString());
			if (needRetry) {
				// 重试一次
				//return sendGetOneStep(url, false);
				throw new RetryAgainException("重试一次");
			} else {
				throw new CMCCWifiAuthException(e, removeSensitiveInfo(url));
			}
		} catch (Exception e) {
			response = null;
			//RunLogCat.i(TAG, "sendGetOneStep exception: " + e.toString());
			LogUtil.i(TAG, "sendGetOneStep exception: " + e.toString());
			e.printStackTrace();
			if (needRetry) {
				// 重试一次
				//return sendGetOneStep(url, false);
				throw new RetryAgainException("重试一次");
			} else {
				throw new CMCCWifiAuthException(e, removeSensitiveInfo(url));
			}
		} finally {
			try{
				httpConn.disconnect();
				}catch(Exception e){
			}
		}
	}
	
	
	public  String sendGetOneStepForAcNameAcIp(String url) throws CMCCWifiAuthException {
		try {
			initHttpConn(false, url, "GET", (int)Const.http_timeout-30*1000,
					(int)Const.http_timeout-30*1000);
			
			// send out data using POST method
			httpConn.setRequestMethod("GET");
			httpConn.connect();

			// receive the response
			int rspCode = httpConn.getResponseCode();
			//RunLogCat.i(TAG, "sendGetOneStep response code: " + rspCode);
			LogUtil.i(TAG, "sendGetOneStep response code: " + rspCode);

			if (rspCode == -1) {
				throw new CMCCWifiAuthException(
							CMCCWifiAuthException.EXCEPTION_TYPE_NOTVALID_RESCODE,
							null, removeSensitiveInfo(url));
			}
			
			if (rspCode == HttpURLConnection.HTTP_MOVED_TEMP) {
				String location;
				location = httpConn.getHeaderField("Location");
				//RunLogCat.i(TAG, "sendGetOneStep 302 location: " + location);
				LogUtil.i(TAG, "sendGetOneStep 302 location: " + location);
				
				return location;
			} else if (rspCode == HttpURLConnection.HTTP_OK) {
				
				return Const.BAIDU_URL;
			} else {
					throw new CMCCWifiAuthException(CMCCWifiAuthException.EXCEPTION_TYPE_RESCODE_NOTOK,
							"response code " + rspCode, removeSensitiveInfo(url));
			}
		} catch (InterruptedIOException e) {
			e.printStackTrace();
			LogUtil.i(TAG, "sendGetOneStep InterruptedIOException: " + e.toString());
			// InterruptedIOException类型异常，包装成CMCCWifiAuthException抛出
			// 不重试
			throw new CMCCWifiAuthException(e, removeSensitiveInfo(url));
		} catch (UnknownHostException e) {
			e.printStackTrace();
			LogUtil.i(TAG, "sendGetOneStep UnknownHostException: " + e.toString());
			throw new CMCCWifiAuthException(e, removeSensitiveInfo(url));
			
		} catch (IOException e) {
			e.printStackTrace();
			LogUtil.i(TAG, "sendGetOneStep IOException: " + e.toString());
			throw new CMCCWifiAuthException(e, removeSensitiveInfo(url));
			
		} catch (Exception e) {
			
			LogUtil.i(TAG, "sendGetOneStep exception: " + e.toString());
			e.printStackTrace();
			throw new CMCCWifiAuthException(e, removeSensitiveInfo(url));
			
		} finally {
			try{
				httpConn.disconnect();
				}catch(Exception e){
			}
		}
	}

	public synchronized OneStepResponseModule sendPostOneStepResponse(String strUrl, String outData,
			int connectTimeout,int readTimeout,String encode) throws CMCCWifiAuthException {
		try {
			initHttpConn(false, strUrl, "POST",connectTimeout,readTimeout);
			response = null;

			// send out data using POST method
			httpConn.setRequestMethod("POST");
			httpConn.setRequestProperty("Accept-Charset", encode);
			httpConn.connect();
			
			
			DataOutputStream dout = new DataOutputStream(httpConn.getOutputStream());
			dout.write(outData.getBytes());
			dout.flush();
			dout.close();

			int rspCode = httpConn.getResponseCode();
			LogUtil.i(TAG, "sendPostOneStepResponse POST response code: " + rspCode);
			if (rspCode == -1) {
					throw new CMCCWifiAuthException(
							CMCCWifiAuthException.EXCEPTION_TYPE_NOTVALID_RESCODE,
							null, removeSensitiveInfo(strUrl + "?" + outData));
			}
			

			if (rspCode == HttpURLConnection.HTTP_MOVED_TEMP) {
					String location;
					location = httpConn.getHeaderField("Location");
					LogUtil.i(TAG, " 302____location of sendPostOneStepResponse(): " + location);
					httpConn.disconnect();
					OneStepResponseModule  result=new OneStepResponseModule();
					result.setIs302(true);
					result.setLocation(location);
					result.setResponseStr("");
					return result;
					
			} else if (rspCode == HttpsURLConnection.HTTP_OK) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						httpConn.getInputStream(), encode));

				StringBuffer rspBuf = new StringBuffer();
				int count = 1024*1024;
				int result = -1;
				char[] readChars = new char[count];
				String temp = null;
				do {
					result = br.read(readChars, 0, count);
					if (result > 0) {
						temp = new String(readChars, 0, result);
						rspBuf.append(temp);
					}

				} while (result != -1);
				response = rspBuf.toString();

				LogUtil.i(TAG, "PortalHttp  Response of sendPostOneStepResponse(): " + response);
				br.close();

				httpConn.getInputStream().close();
				httpConn.disconnect();

				OneStepResponseModule  res=new OneStepResponseModule();
				res.setIs302(false);
				res.setLocation(strUrl);
				res.setResponseStr(response);
				return res;
			} else {
					throw new CMCCWifiAuthException(CMCCWifiAuthException.EXCEPTION_TYPE_RESCODE_NOTOK,
							"response code " + rspCode, removeSensitiveInfo(strUrl + "?" + outData));
			}
		} catch (InterruptedIOException e) {
			LogUtil.i(TAG, "sendPostOneStepResponse InterruptedIOException: " + e.toString());
			throw new CMCCWifiAuthException(e, removeSensitiveInfo(strUrl + "?" + outData));

		} catch (UnknownHostException e) {
			LogUtil.i(TAG, "sendPostOneStepResponse UnknownHostException: " + e.toString());
			throw new CMCCWifiAuthException(e, removeSensitiveInfo(strUrl + "?" + outData));

		} catch (IOException e) {
			LogUtil.i(TAG, "sendPostOneStepResponse IOException: " + e.toString());
			throw new CMCCWifiAuthException(e, removeSensitiveInfo(strUrl + "?" + outData));
			
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.i(TAG, "sendPostOneStepResponse exception: " + e.toString() + " message:" + e.getMessage());
			throw new CMCCWifiAuthException(e, removeSensitiveInfo(strUrl + "?" + outData));
		}finally{
			LogUtil.i(TAG, "sendPostOneStepResponse finally httpConn.disconnect()");
			try{
				httpConn.disconnect();
				}catch(Exception e){
			}
		}
	}

	private  String removeSensitiveInfo(String data) {
		if (data != null) {
			int index = data.indexOf("PWD=");
			if (index != -1) {
				String s1 = data.substring(0, index);
				String s2 = data.substring(index);
				if (s2.indexOf("&") != -1) {
					s2 = s2.substring(s2.indexOf("&"));
				} else {
					s2 = "";
				}
				return s1 + "PWD=******" + s2;
			}
		}
		return data;
	}
	
	private  String readCookie(HttpURLConnection connection) {
		try {
			String setCookie = httpConn.getHeaderField("Set-Cookie");
			//RunLogCat.i(TAG, "Set-Cookie=" + setCookie);
			LogUtil.i(TAG, "Set-Cookie=" + setCookie);
	
			// extract the cookie property
			if (setCookie != null) {
				return setCookie.trim();
			}
		} catch (Exception e) {
			//RunLogCat.i(TAG, "readCookie exception: " + e.toString());
			LogUtil.i(TAG, "readCookie exception: " + e.toString());
			e.printStackTrace();
		}
		return null;
	}
	
	public  boolean sendDataGet(boolean isHttps, String outDataUrl) throws CMCCWifiAuthException {
		try {
			return sendDataGet(isHttps, outDataUrl, false);
		} catch (RetryAgainException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//重试一次
			try {
				LogUtil.i(TAG, "重试一次");
				return  sendDataGet(isHttps, outDataUrl, false);
			} catch (RetryAgainException e1) {
				//如果这里有异常，说明程序逻辑有问题
				LogUtil.i(TAG, "程序逻辑有问题");
				e1.printStackTrace();
				throw new CMCCWifiAuthException(
						CMCCWifiAuthException.EXCEPTION_TYPE_NOTVALID_RESCODE,
						null, removeSensitiveInfo(outDataUrl));
				
			}
		}
	}
	
	private synchronized boolean sendDataGet(boolean isHttps, String outDataUrl, boolean needRetry)
			throws CMCCWifiAuthException, RetryAgainException {
		try {
			initHttpConn(isHttps, outDataUrl, "GET",(int)Const.http_timeout,(int)Const.http_timeout);
			response = null;
			host = null;

			// send out data using POST method
			httpConn.setRequestMethod("GET");
			httpConn.connect();

			// receive the response
			int rspCode = httpConn.getResponseCode();
			//RunLogCat.i(TAG, "sendDataGet response code: " + rspCode);
			LogUtil.i(TAG, "sendDataGet response code: " + rspCode);

			if (rspCode == -1) {
				// response code为-1
				// 重试一次
				if (needRetry) {
//					return sendDataGet(isHttps, outDataUrl, false);
					throw new RetryAgainException("重试一次");
				} else {
					throw new CMCCWifiAuthException(
							CMCCWifiAuthException.EXCEPTION_TYPE_NOTVALID_RESCODE,
							null, removeSensitiveInfo(outDataUrl));
				}
			}
			
			int i = 0;
			// process HTTP 302-redirection
			while (rspCode != HttpURLConnection.HTTP_OK) {
				if (rspCode == HttpURLConnection.HTTP_MOVED_TEMP) {
					String location;
					location = httpConn.getHeaderField("Location");
					LogUtil.i(TAG, "sendDataGet response location: " + location);
					++i;
					
					//RunLogCat.i(TAG, "302, location of SendDataGet(): " + location);
					LogUtil.i(TAG, "302, location of SendDataGet(): " + location);
					
					httpConn.disconnect();

					initHttpConn(location.startsWith("https"), location, "GET",
							(int)Const.http_timeout,(int)Const.http_timeout);
					httpConn.setRequestMethod("GET");
					httpConn.connect();

					rspCode = httpConn.getResponseCode();
					LogUtil.i(TAG, i + " GET response code after 302: " + rspCode);
				} else {
					break;
				}
			}
			if (HttpsURLConnection.HTTP_OK == rspCode) {
				host = httpConn.getURL().getHost();
				// extract the cookie property
				String setCookie = readCookie(httpConn);
				if (setCookie != null) {
					cookie = setCookie;
				}

				BufferedReader br = new BufferedReader(new InputStreamReader(
						httpConn.getInputStream(), "gb2312"));
				StringBuffer rspBuf = new StringBuffer();
				int count = 1024;
				int result = -1;
				char[] readChars = new char[count];
				String temp = null;
				do {
					result = br.read(readChars, 0, count);
					if (result > 0) {
						temp = new String(readChars, 0, result);
						rspBuf.append(temp);
					}
				} while (result != -1);
				response = rspBuf.toString();

				//RunLogCat.i(TAG, "Response of SendDataGet(): " + response);
//				LogUtil.i(TAG, "PortalHttp  Response of SendDataGet(): " + response);
				
				br.close();

				httpConn.getInputStream().close();
				httpConn.disconnect();
				return true;
			} else {
				// response code非200和302
				// 也要重试
				// 重试一次
				if (needRetry) {
//					return sendDataGet(isHttps, outDataUrl, false);
					throw new RetryAgainException("重试一次");
				} else {
					throw new CMCCWifiAuthException(CMCCWifiAuthException.EXCEPTION_TYPE_RESCODE_NOTOK,
							"response code " + rspCode, removeSensitiveInfo(outDataUrl));
				}
			}
		} catch (InterruptedIOException e) {
			LogUtil.i(TAG, "sendDataGet InterruptedIOException: " + e.toString());
			// InterruptedIOException类型异常，包装成CMCCWifiAuthException抛出
			// 不重试
			throw new CMCCWifiAuthException(e, removeSensitiveInfo(outDataUrl));
//			throw new RetryAgainException("重试一次");
		} catch (UnknownHostException e) {
			LogUtil.i(TAG, "sendDataGet UnknownHostException: " + e.toString());
			// 重试一次
			if (needRetry) {
//				return sendDataGet(isHttps, outDataUrl, false);
				throw new RetryAgainException("重试一次");
			} else {
				throw new CMCCWifiAuthException(e, removeSensitiveInfo(outDataUrl));
			}
		} catch (IOException e) {
			LogUtil.i(TAG, "sendDataGet IOException: " + e.toString());
			// 重试一次
			if (needRetry) {
//				return sendDataGet(isHttps, outDataUrl, false);
				throw new RetryAgainException("重试一次");
			} else {
				throw new CMCCWifiAuthException(e, removeSensitiveInfo(outDataUrl));
			}
		} catch (Exception e) {
			response = null;
			//RunLogCat.i(TAG, "sendDataGet exception: " + e.toString());
			LogUtil.i(TAG, "sendDataGet exception: " + e.toString());
			e.printStackTrace();
			// 重试一次
			if (needRetry) {
//				return sendDataGet(isHttps, outDataUrl, false);
				throw new RetryAgainException("重试一次");
			} else {
				throw new CMCCWifiAuthException(e, removeSensitiveInfo(outDataUrl));
			}
		}finally{
			try{
			httpConn.disconnect();
			}catch(Exception e){
			}
		}
	}

	private class MyHostnameVerifier implements HostnameVerifier {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			// if (BJ_IP.equals(hostname)) {
			// return true;
			// }
			return true;
		}

	}

	private  class myTrustManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			// TODO Auto-generated method stub

		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			// TODO Auto-generated method stub

		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			// TODO Auto-generated method stub
			return null;
		}
	}

}
