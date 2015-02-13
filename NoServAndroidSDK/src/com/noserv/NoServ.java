package com.noserv;

import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

import android.content.Context;

public class NoServ {
	
	protected static boolean isStart = false;

	protected static String columnID = "username";
	protected static String columnPW = "password";
	protected static String columnUnique = "objectId";	
	protected static String columnToken = "sessionToken";	

	protected static Context context;

	protected static class KeyKeeper {
		protected static String applicationId;
		protected static String restapiKey;
		protected static String masterKey;
	}
	
	public NoServ() {
		HttpURLConnection.setFollowRedirects(true);
		HttpsURLConnection.setFollowRedirects(true);
	}
	
    private static class InstanceHolder {
        private static final NoServ mSingleton = new NoServ();
    }

    public static NoServ conn() {
        return InstanceHolder.mSingleton;
    }

	public static void initialize(Context context, String applicationId, String restApiKey) {

		NoServ.context = context;
		
		KeyKeeper.applicationId = applicationId;
		KeyKeeper.restapiKey = restApiKey;		
		isStart = true;
		conn();

	}
	
	public static void setMasterKey(String masterKey) {
		
		KeyKeeper.masterKey = masterKey;
	}

}
