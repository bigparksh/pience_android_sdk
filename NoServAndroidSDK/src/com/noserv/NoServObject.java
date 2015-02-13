package com.noserv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;


public class NoServObject implements NoServConfig {

	public static String KEY_UPDATED_AT = "updatedAt";
	public static String KEY_CREATED_AT = "createdAt";
	
	private HashMap<String, Object> dataMap = new HashMap<String, Object>();
	
	public interface NoServListener<T> {
		
		public void done(T result, Exception e) throws Exception;
	}
	
	public static <T> T taskWaitForCompletion(Task<T> task) throws Exception {
		
		try {
			task.waitForCompletion();
			
			if(task.isFaulted()) {
				throw task.getError();
			}
		} catch (InterruptedException e) {
			throw new RuntimeException();
		}
		return task.getResult();
	}
	
	public static <T> void taskContinueWithToListener(Task<T> task, final NoServListener<T> listener) {
		
		task.continueWith(new Continuation<T, Void>() {

			@Override
			public Void then(Task<T> task) throws Exception {
				
				if(listener != null) {
					listener.done(task.getResult(), task.getError());
				}
				return null;
			}
		});
	}
	
	public static NoServObject fromJson(JSONObject json) throws JSONException {

		NoServObject object = new NoServObject();
		object.jsonToDataMap(json);
		return object;
	}
	
	public NoServObject put(String key, Object value) {
		
		this.dataMap.put(key, value);
		return this;
	}
	
	public Object get(String key) {
		
		return this.dataMap.get(key);
	}
	
	public String getString(String key) {
		
		return (String) this.get(key);
	}

	public Double getDouble(String key) {
		
		return (Double) this.get(key);
	}
	
	public NoServObject setObjectId(String value) {
		
		return this.put(NoServ.columnUnique, value);
	}
	
	public String getObjectId() {
		
		return this.getString(NoServ.columnUnique);
	}
	
	public void clear() {
		
		this.dataMap.clear();
	}
	
	public void save() throws Exception {
		
		taskWaitForCompletion(this.saveAsync());
	}
	
	public Task<Void> saveInBackground() {
		
		return this.saveAsync();
	}

	public void saveInBackground(NoServListener<Void> listener) {
		
		taskContinueWithToListener(this.saveAsync(), listener);
	}
	
	protected Task<Void> saveAsync() {
		
		return Task.forError(new IllegalAccessException("not supported."));
	}
	
	protected Task<Void> upsertAsync(final NoServNetwork.Method method, final String url) {
		
		return Task.forResult(null).onSuccessTask(new Continuation<Object, Task<JSONObject>>() {

			@Override
			public Task<JSONObject> then(Task<Object> task) throws Exception {
				
				return execute(method, url, NoServObject.this.dataMap);
			}
		}, Task.BACKGROUND_EXECUTOR).onSuccessTask(new Continuation<JSONObject, Task<Void>>() {

			@Override
			public Task<Void> then(Task<JSONObject> task) throws Exception {
				
				jsonToDataMap(task.getResult());
				return null;
			}
		});
	}
	
	public void delete() throws Exception {
		
		taskWaitForCompletion(this.deleteAsync());
	}
	
	public Task<Void> deleteInBackground() {
		
		return this.deleteAsync();
	}

	public void deleteInBackground(NoServListener<Void> listener) {
		
		taskContinueWithToListener(this.deleteAsync(), listener);
	}
	
	protected Task<Void> deleteAsync() {
		
		return Task.forError(new IllegalAccessException("not supported."));
	}
	
	protected Task<Void> deleteAsync(String url) {

		return Task.forResult(url).onSuccessTask(new Continuation<String, Task<JSONObject>>() {

			@Override
			public Task<JSONObject> then(Task<String> task) throws Exception {
				
				return execute(NoServNetwork.Method.DELETE, task.getResult(), NoServObject.this.dataMap);
			}
		}, Task.BACKGROUND_EXECUTOR).onSuccessTask(new Continuation<JSONObject, Task<Void>>() {

			@Override
			public Task<Void> then(Task<JSONObject> task) throws Exception {

				clear();
				return null;
			}
		});
	}
	
	protected Task<JSONObject> execute(NoServNetwork.Method method, String url, Map<String, Object> dataMap) {
		
		return new NoServNetwork().url(url).data(dataMap).method(method).execute();
	}
	
	protected void jsonToDataMap(JSONObject json) throws JSONException {

		if (json == null) {
			return;
		}
			
		Iterator<String> keysItr = json.keys();
		while (keysItr.hasNext()) {
			String key = keysItr.next();
			Object value = json.get(key);

			if (value instanceof JSONArray) {
				value = jsonToList((JSONArray) value);
			}
			
			if (JSONObject.NULL.equals(value)) {
				continue;
			}
			this.put(key, value);
		}
	}

	private List<Object> jsonToList(JSONArray array) throws JSONException {
		
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < array.length(); i++) {
			Object value = array.get(i);
			list.add(value);
		}
		return list;
	}
}
