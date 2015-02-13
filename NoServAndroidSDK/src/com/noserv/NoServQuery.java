package com.noserv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import bolts.Continuation;
import bolts.Task;

import com.noserv.NoServObject.NoServListener;
import com.noserv.utils.Utils;
import com.noserv.utils.Utils.IsoDate;

public abstract class NoServQuery<T> implements NoServConfig {

	private Integer limit;
	private Integer skip;
	private Integer count;
	private Map<String, Object> whereMap = new HashMap<String, Object>();
	private List<String> orderList = new ArrayList<String>();
	
	public void clear() {
		
		this.limit = null;
		this.skip = null;
		this.count = null;
		this.whereMap.clear();
		this.orderList.clear();
	}
	
	@SuppressWarnings("unchecked")
	private void addCondition(String key, String condition, Object value) {

		Map<String, Object> whereValue = null;
		
		if (this.whereMap.containsKey(key)) {
			Object existingValue = this.whereMap.get(key);
			if ((existingValue instanceof Map<?, ?>)) {
				whereValue = (Map<String, Object>) existingValue;
			}
		}
		
		if (whereValue == null) {
			whereValue = new HashMap<String, Object>();
		}
		
		whereValue.put(condition, value);

		this.whereMap.put(key, whereValue);
	}
	
	public NoServQuery<T> whereEq(String key, Object value) {
		
		this.whereMap.put(key, value);
		return this;
	}
	
	public NoServQuery<T> whereNotEq(String key, Object value) {
		
		addCondition(key, "$ne", value);
		return this;
	}
	
	public NoServQuery<T> whereLt(String key, Object value) {
		
		addCondition(key, "$lt", value);
		return this;
	}

	public NoServQuery<T> whereGt(String key, Object value) {
		
		addCondition(key, "$gt", value);
		return this;
	}

	public NoServQuery<T> whereLtOrEq(String key, Object value) {
		
		addCondition(key, "$lte", value);
		return this;
	}

	public NoServQuery<T> whereGtOrEq(String key, Object value) {
		
		addCondition(key, "$gte", value);
		return this;
	}

	public NoServQuery<T> whereIn(String key, Collection<? extends Object> values) {
		
		addCondition(key, "$in", new ArrayList(values));
		return this;
	}

	public NoServQuery<T> whereAll(String key, Collection<?> values) {
		
		addCondition(key, "$all", new ArrayList(values));
		return this;
	}

	public NoServQuery<T> order(String value) {
		
		this.orderList.add(value);
		return this;
	}
	
	public NoServQuery<T> limit(int value) {
		
		this.limit = Integer.valueOf(value);
		return this;
	}

	public NoServQuery<T> skip(int value) {
		
		this.skip = Integer.valueOf(value);
		return this;
	}

	public NoServQuery<T> count(boolean value) {
		
		if(value)
			this.count = 1;
		else
			this.count = null;
		return this;
	}
	
	public Object getDateQuery(Date date) {
		
		return this.getDateQuery(IsoDate.dateToString(date, IsoDate.DATE_TIME));
	}
	
	public Object getDateQuery(String date) {
		
		Map<String, Object> dateQuery = new HashMap<String, Object>();
		dateQuery.put("$ISODate", date);
		
		return dateQuery;
	}
	
	public List<T> find() throws Exception {
		
		return NoServObject.taskWaitForCompletion(this.findAsync());
	}
	
	public Task<List<T>> findInBackground() {
		
		return this.findAsync();
	}

	public void findInBackground(NoServListener<List<T>> listener) {
		
		NoServObject.taskContinueWithToListener(this.findAsync(), listener);
	}
	
	/**
	 * 
	 *	@Override
	 *	protected Task<List<T>> findAsync() {
	 *		return this.findAsync(Your URL);
	 *	}
	 * @return
	 */
	protected abstract Task<List<T>> findAsync();
	
	protected abstract List<T> jsonArrayToList(JSONArray jsons) throws JSONException;
	
	private List<T> jsonToList(JSONObject json) throws JSONException {
		
		List<T> objects = new ArrayList<T>();

		if (json == null || !json.has("results")) {
			return objects;
		}

		if(json.has("count")) {
			for (int i=0, count = json.getInt("count"); i<count; i++) {
				objects.add(null);
			}
		} else {
			JSONArray jsons = json.getJSONArray("results");
			objects = jsonArrayToList(jsons);
		}
		
		return objects;
	}

	protected Task<List<T>> findAsync(String url) {
		
		return Task.forResult(url).onSuccessTask(new Continuation<String, Task<JSONObject>>() {

			@Override
			public Task<JSONObject> then(Task<String> task) throws Exception {
				
				return execute(NoServNetwork.Method.GET, task.getResult(), NoServQuery.this.makeFindQuery());
			}
		}, Task.BACKGROUND_EXECUTOR).onSuccessTask(new Continuation<JSONObject, Task<List<T>>>() {

			@Override
			public Task<List<T>> then(Task<JSONObject> task) throws Exception {
				
				return Task.forResult(jsonToList(task.getResult()));
			}
		});
	}
	
	protected Task<JSONObject> execute(NoServNetwork.Method method, String url, Map<String, Object> dataMap) throws Exception {
		
		return new NoServNetwork().url(url).data(dataMap).method(method).execute();
	}
	
	@SuppressWarnings("unchecked")
	private Object encode(Object object) throws JSONException {

		if(object instanceof Map<?, ?>) {

			Map<String, Object> map = (Map<String, Object>) object;
			JSONObject json = new JSONObject();
			for (Map.Entry<String, Object> pair : map.entrySet()) {
				json.put((String) pair.getKey(), encode(pair.getValue()));
			}
			return json;
		}
		if(object instanceof List) {
			
			JSONArray array = new JSONArray();
			for (Object item : (List<?>) object) {
				array.put(encode(item));
			}
			return array;
		}
		
		return object;
	}
	
	private Map<String, Object> makeFindQuery() throws JSONException {
		
		HashMap<String, Object> findQuery = new HashMap<String, Object>();
		if(!this.whereMap.isEmpty()) {
			findQuery.put("where", this.encode(this.whereMap));
		}
		if(!this.orderList.isEmpty()) {
			findQuery.put("order", TextUtils.join(",", this.orderList));
		}
		if(this.limit != null) {
			findQuery.put("limit", this.limit);
		}
		if(this.skip != null) {
			findQuery.put("skip", this.skip);
		}
		if(this.count != null) {
			findQuery.put("count", this.count);
		}
		
		return findQuery;
	}
}
