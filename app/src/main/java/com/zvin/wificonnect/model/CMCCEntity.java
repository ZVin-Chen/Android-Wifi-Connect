package com.zvin.wificonnect.model;

import java.io.Serializable;

/**
 * 序列化实体类 用于传送数据到别的进程
 * @author lixiujuan
 *
 */
public class CMCCEntity implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String key;
	private Object value;
	
	public CMCCEntity(){
	}
	public CMCCEntity(String _key,Object _value){
		this.key = _key;
		this.value = _value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
