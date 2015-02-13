package com.noserv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bolts.Task;

import com.noserv.NoServ.KeyKeeper;
import com.noserv.NoServNetwork.Method;

public class NoServInstallations extends NoServObject implements NoServConfig {

	private static String API_URL = API_ADDR + API_PATH_INSTALL;

	public static String KEY_DEVICE_TOKEN = "deviceToken";
	public static String KEY_CHANNELS = "channels";

	public static String KEY_DEVICE_TYPE = "deviceType";
	public static String KEY_PUSH_TYPE = "gcm";

	public NoServInstallations() {
		
		this.init();
	}
	
	protected void init() {
		
		this.put(KEY_DEVICE_TYPE, "android");
		this.put(KEY_PUSH_TYPE, "gcm");
	}
	
	@Override
	public void clear() {
		
		super.clear();
		this.init();
	}
	
	public NoServInstallations setChannels(String value) {
		
		this.put(KEY_CHANNELS, value);
		return this;
	}
	
	public String getChannels() {
		
		return this.getString(KEY_CHANNELS);
	}
	
	public NoServInstallations setDeviceToken(String value) {
		
		this.put(KEY_DEVICE_TOKEN, value);
		return this;
	}
	
	@Override
	final protected Task<Void> saveAsync() {
		
		String deviceToken = (String) this.getString(KEY_DEVICE_TOKEN);
		if(deviceToken == null || deviceToken.length() == 0) {
			throw new IllegalStateException("device token not found.");
		}
		
		String url = API_URL;

		NoServNetwork.Method method = NoServNetwork.Method.PUT;
		
		String objectId = (String) this.getObjectId();
		if(objectId != null) {
			url += "/" + objectId;
		} else {
			method = NoServNetwork.Method.POST;
		}
		
		return this.upsertAsync(method, url);
	}
	
	public static NoServQuery<NoServObject> getQuery() {
		
		return new NoServQuery<NoServObject>() {

			@Override
			protected Task<List<NoServObject>> findAsync() {
				return this.findAsync(API_URL);
			}
			
			@Override
			protected Task<JSONObject> execute(Method method, String url, Map<String, Object> dataMap) throws Exception {
				
				return new NoServNetwork() {
					
					@Override
					protected HttpUriRequest onGetRequest() throws Exception {
						
						HttpUriRequest httpRequest = super.onGetRequest();
						
						if(KeyKeeper.masterKey == null) {
							throw new IllegalStateException("The masterKey not found.");
						}
						httpRequest.setHeader(NoServNetwork.HTTP_MASTER_KEY, KeyKeeper.masterKey);

						return httpRequest;
					}
				}.url(url).data(dataMap).method(method).execute();
			}

			@Override
			protected List<NoServObject> jsonArrayToList(JSONArray jsons) throws JSONException {
				
				List<NoServObject> objects = new ArrayList<NoServObject>();
				
				for (int i=0; i<jsons.length(); i++) {
					NoServObject object = NoServObject.fromJson(jsons.getJSONObject(i));
					objects.add(object);
				}
				
				return objects;
			}
		};
	}
}
