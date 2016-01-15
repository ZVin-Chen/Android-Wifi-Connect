package com.zvin.wificonnect.migration;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Serializable;
import java.net.UnknownHostException;
/**
 * cmcc认证异常类
 * @author zhangxx
 *
 */
public class CMCCWifiAuthException extends Exception implements Serializable{

	private static final long serialVersionUID = -1676261678493235786L;
	
	public static final int EXCEPTION_TYPE_TIMEOUT = 998;			// 超时
	public static final int EXCEPTION_TYPE_IO = 997;				// IOException
	public static final int EXCEPTION_TYPE_UNKNOWN_HOST = 996;		// 未知的HostName
	public static final int EXCEPTION_TYPE_NOTVALID_RESCODE = 995;	// response code为-1
	public static final int EXCEPTION_TYPE_RESCODE_NOTOK = 994;		// response code非200
	public static final int EXCEPTION_TYPE_OTHER = 993;				// 其他异常
	
	private int type;
	private String requestDetail;
	
	public CMCCWifiAuthException(int exceptionType, String message, String requestDetail) {
		super(message);
		type = exceptionType;
		this.requestDetail = requestDetail;
	}
	
	public CMCCWifiAuthException(Throwable throwable, String requestDetail) {
		super(throwable);
		this.requestDetail = requestDetail;
		if (throwable instanceof InterruptedIOException) {
			// 超时异常
			type = EXCEPTION_TYPE_TIMEOUT;
		} else if (throwable instanceof UnknownHostException) {
			// 未知的HostName
			type = EXCEPTION_TYPE_UNKNOWN_HOST;
		} else if (throwable instanceof IOException) {
			// IOException
			type = EXCEPTION_TYPE_IO;
		} else {
			// 其他异常
			type = EXCEPTION_TYPE_OTHER;
		}
	}

	public int getType() {
		return type;
	}
	
	public String getRequestDetail() {
		return requestDetail;
	}
}
