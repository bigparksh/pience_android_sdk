package com.noserv;

public class NoServException extends Exception {

	private static final long serialVersionUID = 1L;
	private String code;
	private String exception;

	public NoServException() {
		this(null, null);
	}

	public NoServException(String message) {
		this(message, null);
	}
	
	public NoServException(String message, String code) {
		super(message);
		
		this.exception = message;
		this.code = code;
	}
	
	public String getException() {
		return this.exception;
	}
	
	public String getCode() {
		return this.code;
	}

}
