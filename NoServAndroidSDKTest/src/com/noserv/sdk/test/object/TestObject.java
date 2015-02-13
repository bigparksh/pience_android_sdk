package com.noserv.sdk.test.object;

import com.noserv.NoServClass;

public class TestObject extends NoServClass {
	
	public static final String CLASS_NAME = "test1";

	public static final String KEY1 = "key1";
	public static final String KEY2 = "key2";
	public static final String KEY3 = "key3";
	
	public TestObject setKey1(Object value) {
		
		this.put(KEY1, value);
		return this;
	}
	
	public String getKey1() {
		
		return (String) this.get(KEY1);
	}

	public TestObject setKey2(Object value) {
		
		this.put(KEY2, value);
		return this;
	}
	
	public String getKey2() {
		
		return (String) this.get(KEY2);
	}

	public TestObject setKey3(Object value) {
		
		this.put(KEY3, value);
		return this;
	}
	
	public String getKey3() {
		
		return (String) this.get(KEY3);
	}
}
