package com.noserv;

public interface NoServConfig {

	// Server Address
	public static final String API_BASE_URL		 = "http://www.noserv.com:2337";
	public static final String API_VER			 = "1";
	public static final String API_ADDR			 = API_BASE_URL+"/"+API_VER;
	
	// Users API PATH
	public static final String API_PATH_USERS	 = "/users";
	public static final String API_PATH_LOGIN	 = "/login";
	public static final String API_PATH_VALIDATE = "/users/me";
	public static final String API_PATH_PWRESET	 = "/requestPasswordReset";
	
	// Objects API PATH
	public static final String API_PATH_OBJECTS	 = "/classes";

	// Files API PATH
	public static final String API_PATH_FILES	 = "/files";

	// Push Install API PATH
	public static final String API_PATH_INSTALL	 = "/installations";
	
	// GCM SENDER_ID
	public static final String SEND_ID = "665706133237";	

	// Exceptions
	public static final String EXCEPTION_MSG_1 = "NOT_AUTHORIZED";
	public static final String EXCEPTION_MSG_2 = "INVALID_URL";
	public static final String EXCEPTION_MSG_3 = "DUPLICATE_VALUE";
	public static final String EXCEPTION_MSG_4 = "INTERNAL_SERVER_ERROR";
	public static final String EXCEPTION_MSG_5 = "NO_DATA_IS_VIEWED";
	public static final String EXCEPTION_MSG_6 = "FILE_NOT_EXIST";
	public static final String EXCEPTION_MSG_7 = "FILE_SIZE_IS_LARGER_THAN_10MB";
	public static final String EXCEPTION_MSG_8 = "INVALID_OBJECT_ID";	
}
