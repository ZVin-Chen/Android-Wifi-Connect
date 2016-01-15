package com.zvin.wificonnect.migration;

import java.io.Serializable;

public class PortalResponseObj  implements Serializable{
	private int code;
	private String msg;
	private CMCCWifiAuthException exceptionDetail;	// Exception详情

	private String request;		// 发生Exception的request详情

	public PortalResponseObj() {
	}
	public PortalResponseObj(int code, String msg, String request) {
		this.code = code;
		this.msg = msg;
		this.request = request;
	}
	
	public PortalResponseObj(CMCCWifiAuthException e) {
		this.code = -2;	// TODO code为exception通用
		exceptionDetail = e;
		this.request = e.getRequestDetail();
	}
	
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public CMCCWifiAuthException getExceptionDetail() {
		return exceptionDetail;
	}
	public String getRequest() {
		return request;
	}
	
	public void setRequest(String request) {
		this.request = request;
	}
	public void setExceptionDetail(CMCCWifiAuthException exceptionDetail) {
		this.exceptionDetail = exceptionDetail;
	}

}
