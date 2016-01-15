package com.zvin.wificonnect.migration;

/**
 * 接口返回头信息对象
 * @author x_tanghebiao
 *
 */
public class ResponseHeader {
	private int code; //返回码，0为成功
	private String errorMessage;  //发生错误时的错误描述
	private String timestamp;  //响应消息的时间戳
	private String reultDesc;
	
	public ResponseHeader() {
	}
	public ResponseHeader(int code, String errorMessage, String timestamp) {
		super();
		this.code = code;
		this.errorMessage = errorMessage;
		this.timestamp = timestamp;
	}

	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getReultDesc() {
		return reultDesc;
	}
	
	public void setReultDesc(String reultDesc) {
		this.reultDesc = reultDesc;
	}
	
	
}
