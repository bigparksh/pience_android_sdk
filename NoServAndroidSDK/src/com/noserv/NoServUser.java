package com.noserv;

import java.util.Map;

import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;



public class NoServUser extends NoServObject implements NoServConfig {

	private static String API_LOGIN_URL = API_ADDR + API_PATH_LOGIN;
	private static String API_VALIDATE_URL = API_ADDR + API_PATH_VALIDATE;
	private static String API_USERS_URL = API_ADDR + API_PATH_USERS;
	
	/**
	 * Logging In
	 * @param id
	 * @param passwd
	 * @return
	 * @throws Exception
	 */
	public static NoServUser logIn(String id, String passwd) throws Exception {
		
		return NoServObject.taskWaitForCompletion(logInAync(id, passwd));
	}

	/**
	 * Logging In
	 * @param id
	 * @param passwd
	 * @return
	 * @throws Exception
	 */
	public static Task<NoServUser> logInInBackground(String id, String passwd) throws Exception {
		
		return logInAync(id, passwd);
	}
	
	private static Task<NoServUser> logInAync(String id, String passwd) {
		
		NoServUser user = new NoServUser();
		user.setUserID(id);
		user.setPassword(passwd);
		
		return user.loginAsync(API_LOGIN_URL);
	}
	
	/**
	 * Validating Session Tokens / Retrieving Current User
	 * @param sessionToken
	 * @return
	 * @throws Exception
	 */
	public static NoServUser logIn(String sessionToken) throws Exception {
		
		return NoServObject.taskWaitForCompletion(logInAync(sessionToken));
	}
	
	/**
	 * Validating Session Tokens / Retrieving Current User
	 * @param sessionToken
	 * @return
	 */
	public static Task<NoServUser> logInInBackground(String sessionToken) {

		return logInAync(sessionToken);
	}
	
	private static Task<NoServUser> logInAync(String sessionToken) {
		
		NoServUser user = new NoServUser();
		user.setSessionToken(sessionToken);
		
		return user.loginAsync(API_VALIDATE_URL);
	}
	
	public NoServUser setUserID(String value) {
	
		this.put(NoServ.columnID, value);
		return this;
	}

	public NoServUser setPassword(String value) {
		
		this.put(NoServ.columnPW, value);
		return this;
	}
	
	public NoServUser setSessionToken(String value) {
		
		this.put(NoServ.columnToken, value);
		return this;
	}
	
	public String getSessionToken() {
		
		return this.getString(NoServ.columnToken);
	}
	
	/**
	 * Signing Up
	 * @throws Exception
	 */
	public void signUp() throws Exception {

		taskWaitForCompletion(this.signUpAsync());
	}

	/**
	 * Signing Up
	 * @return
	 */
	public Task<Void> signUpInBackground() {
		
		return this.signUpAsync();
	}
	
	@Override
	final protected Task<Void> saveAsync() {
		
		String objectId = (String) NoServUser.this.getObjectId();
		if(objectId == null || objectId.length() == 0) {
			throw new IllegalStateException("objectId not found.");
		}
		return this.upsertAsync(NoServNetwork.Method.PUT, API_USERS_URL + "/" + objectId);
	}

	private Task<Void> signUpAsync() {
		
		return this.upsertAsync(NoServNetwork.Method.POST, API_USERS_URL);
	}
	
	private Task<NoServUser> loginAsync(String url) {
		
		return this.upsertAsync(NoServNetwork.Method.GET, url).onSuccess(new Continuation<Void, NoServUser>() {

			@Override
			public NoServUser then(Task<Void> task) throws Exception {
				return NoServUser.this;
			}
		});
	}
	
	@Override
	final protected Task<Void> deleteAsync() {
		
		String objectId = (String) NoServUser.this.getObjectId();
		if(objectId == null || objectId.length() == 0) {
			throw new IllegalStateException("objectId not found.");
		}
		
		return this.deleteAsync(API_USERS_URL + "/" + objectId);
	}
	
	@Override
	final protected Task<JSONObject> execute(NoServNetwork.Method method, String url, Map<String, Object> dataMap) {
		
		return new NoServNetwork() {
			
			protected HttpUriRequest HttpUriRequest() throws Exception {
				
				HttpUriRequest httpRequest = super.HttpUriRequest();
				
				String token = NoServUser.this.getSessionToken();
				if(token != null) {
					httpRequest.setHeader(NoServNetwork.HTTP_SESSION_TOKEN, token);
				}
				
				return httpRequest;
			}
		}.url(url).data(dataMap).method(method).execute();
	}
}
