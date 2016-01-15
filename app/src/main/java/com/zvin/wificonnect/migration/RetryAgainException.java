package com.zvin.wificonnect.migration;

import java.io.Serializable;

public class RetryAgainException extends Exception implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6263123897641003268L;
	public RetryAgainException(String message) {
		super(message);
	}
}
