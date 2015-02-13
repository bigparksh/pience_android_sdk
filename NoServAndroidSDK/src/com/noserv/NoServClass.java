package com.noserv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bolts.Task;


public class NoServClass extends NoServObject {

	private static String API_URL = API_ADDR + API_PATH_OBJECTS;
	
	private String className;
	private static final Map<String, Class<? extends NoServClass>> classObjectMap = new ConcurrentHashMap<String, Class<? extends NoServClass>>();
	
	protected NoServClass() {
		
		this(null);
	}
	
	public NoServClass(String className) {
		
		this.className = className;
	}
	
	@Override
	protected Task<Void> saveAsync() {
		
		String url = API_URL + "/" + this.className;

		NoServNetwork.Method method = NoServNetwork.Method.PUT;
		
		String objectId = (String) this.getObjectId();
		if(objectId != null) {
			url += "/" + objectId;
		} else {
			method = NoServNetwork.Method.POST;
		}
		
		return this.upsertAsync(method, url);
	}
	
	@Override
	protected Task<Void> deleteAsync() {

		String objectId = (String) NoServClass.this.getObjectId();
		if(objectId == null || objectId.length() == 0) {
			throw new IllegalStateException("objectId not found.");
		}
		
		return this.deleteAsync(API_URL + "/" + this.className + "/" + objectId);
	}
	
	public static NoServClass create(String className) {
		
		if (classObjectMap.containsKey(className)) {
			try {
				NoServClass object = (NoServClass) ((Class<? extends NoServClass>) classObjectMap.get(className)).newInstance();
				object.className = className;
				
				return object;
			} catch (Exception e) {
				throw new RuntimeException("Failed to create instance of className.", e);
			}
		}
		return new NoServClass(className);
	}
	
	public static NoServClass create(Class<? extends NoServClass> clazz) {
		
		return create(getClassName(clazz));
	}
	
	private static String getClassName(Class<? extends NoServClass> clazz) {
		
		for (Entry<String, Class<? extends NoServClass>> entry : classObjectMap.entrySet()) {
	        if (clazz.equals(entry.getValue())) {
	        	return entry.getKey();
	        }
	    }
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends NoServClass> T fromJson(JSONObject json, String className) throws JSONException {
		
		T object = (T) NoServClass.create(className);
		object.jsonToDataMap(json);
		return object;
	}
	
	public static void registerSubclass(String className, Class<? extends NoServClass> subclass) {
		
		classObjectMap.put(className, subclass);
	}
	
	public static void unregisterSubclass(String className) {
		
		classObjectMap.remove(className);
	}
	
	public static <T extends NoServClass> NoServQuery<T> getQuery(final String className) {
		
		return new NoServQuery<T>() {

			@Override
			protected Task<List<T>> findAsync() {
				
				return this.findAsync(API_URL + "/" + className);
			}

			@Override
			protected List<T> jsonArrayToList(JSONArray jsons) throws JSONException {
				
				List<T> objects = new ArrayList<T>();
				
				for (int i=0; i<jsons.length(); i++) {
					T object = NoServClass.fromJson(jsons.getJSONObject(i), className);
					objects.add(object);
				}
				
				return objects;
			}
		};
	}
}
