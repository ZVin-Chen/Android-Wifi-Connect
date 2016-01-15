package com.zvin.wificonnect.migration;

public class OneStepResponseModule {
    private boolean is302;
	private String responseStr;
    private String location;
    public boolean isIs302() {
		return is302;
	}
	public void setIs302(boolean is302) {
		this.is302 = is302;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}

	public String getResponseStr() {
		return responseStr;
	}
	public void setResponseStr(String responseStr) {
		this.responseStr = responseStr;
	}
}
