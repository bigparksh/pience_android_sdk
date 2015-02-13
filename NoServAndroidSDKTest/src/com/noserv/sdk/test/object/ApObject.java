package com.noserv.sdk.test.object;

import com.noserv.NoServClass;

public class ApObject extends NoServClass {

	public static String CLASS_NAME = "aps";
	
	public static String KEY_SSID = "ssid";
	public static String KEY_BSSID = "bssid";
	public static String KEY_PASSWORD = "password";
	public static String KEY_ADDRESS = "address";

	public static String KEY_LATITUDE = "latitude";
	public static String KEY_LONGITUDE = "longitude";
	
	public ApObject setSsid(String value) {
		
		this.put(KEY_SSID, value);
		return this;
	}
	
	public String ssid() {
		
		return this.getString(KEY_SSID);
	}
	
	public ApObject setBSsid(String value) {
		
		this.put(KEY_BSSID, value);
		return this;
	}
	
	public String bssid() {
		
		return this.getString(KEY_BSSID);
	}

	public ApObject setPassword(String value) {
		
		this.put(KEY_PASSWORD, value);
		return this;
	}
	
	public String password() {
		
		return this.getString(KEY_PASSWORD);
	}

	public ApObject setAddress(String value) {
		
		this.put(KEY_ADDRESS, value);
		return this;
	}
	
	public String address() {
		
		return this.getString(KEY_ADDRESS);
	}
	
	public double latitude() {
		
		return this.getDouble(KEY_LATITUDE);
	}

	
	public double longitude() {
		
		return this.getDouble(KEY_LONGITUDE);
	}
}
