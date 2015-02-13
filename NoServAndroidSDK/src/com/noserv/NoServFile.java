package com.noserv;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import com.noserv.NoServ.KeyKeeper;
import com.noserv.utils.Utils;

import bolts.Task;

public class NoServFile extends NoServObject implements NoServConfig {

	private static String API_URL = API_ADDR + API_PATH_FILES;
	private static int FILE_MAX_SIZE = 10 * 1024 * 1024; /* 10MB */

	private String fileName;
	private Map<String, Object> fileMap = new HashMap<String, Object>();

	public NoServFile(String fileName) {
		
		this.fileName = fileName;
	}
	
	/**
	 * 
	 * @param value [File, byte[], InputStream, String of file path]
	 * @return
	 */
	public NoServFile setFile(Object value) {
		
		this.fileMap.put("file", value);
		return this;
	}
	
	public String getUrl() {
		
		return this.getString("url");
	}

	@Override
	public String getObjectId() {

		return this.getString("name");
	}
	
	@Override
	final protected Task<Void> saveAsync() {
		
		if(this.fileName == null || this.fileName.length() == 0) {
			throw new IllegalStateException("file name not found.");
		}
		
		return this.upsertAsync(NoServNetwork.Method.POST, API_URL + "/" + this.fileName);
	}

	@Override
	final protected Task<Void> deleteAsync() {
		
		if(this.fileName == null || this.fileName.length() == 0) {
			throw new IllegalStateException("file name not found.");
		}
		
		return this.deleteAsync(API_URL + "/" + this.fileName);
	}
	
	@Override
	final protected Task<JSONObject> execute(NoServNetwork.Method method, String url, Map<String, Object> dataMap) {
		
		return new NoServNetworkFile().url(url).data(this.fileMap).method(method).execute();
	}
	
	private class NoServNetworkFile extends NoServNetwork {

		@Override
		protected HttpClient getHttpClient() {
			
			HttpClient client = super.getHttpClient();
			
			HttpConnectionParams.setConnectionTimeout(client.getParams(), 0);
			HttpConnectionParams.setSoTimeout(client.getParams(), 0);
			
			return client;
		}
		
		@Override
		protected HttpUriRequest onPutRequest() throws Exception {
			
			throw new IllegalAccessError("request method 'put' not supported.");
		}

		@Override
		protected HttpUriRequest onGetRequest() throws Exception {
			
			throw new IllegalAccessError("request method 'get' not supported.");
		}
		
		@Override
		protected HttpUriRequest onPostRequest() throws Exception {
			
			HttpPost httpRequest = new HttpPost(this.url);
			
			if(this.dataMap == null || this.dataMap.isEmpty()) {
				throw new IllegalArgumentException("file not found.");
			}
			
			if(this.dataMap.size() > 1) {
				throw new IllegalArgumentException("too many files.");
			}
			
			Iterator<String> iter = this.dataMap.keySet().iterator();
			byte[] byteArray = this.objectToByteArray(this.dataMap.get(iter.next()));
			if(byteArray.length > FILE_MAX_SIZE) {
				throw new IllegalArgumentException("File is too large for destination file system.");
			}
			httpRequest.setEntity(new ByteArrayEntity(byteArray));
			httpRequest.setHeader(HTTP.CONTENT_TYPE, "octet-stream");
			
			return httpRequest;
		}
		
		@Override
		protected HttpUriRequest onDeleteRequest() throws Exception {
			
			HttpUriRequest httpRequest = super.onDeleteRequest();
			
			if(KeyKeeper.masterKey == null) {
				throw new IllegalStateException("The masterKey not found.");
			}
			httpRequest.setHeader(NoServNetwork.HTTP_MASTER_KEY, KeyKeeper.masterKey);

			return httpRequest;
		}
		
		private byte[] objectToByteArray(Object obj) throws Exception{
			
			if(obj instanceof File) {

				File file = (File) obj;
				return Utils.inputStreamToBytes(new FileInputStream(file));

			} else if(obj instanceof byte[]) {
				
				return (byte[]) obj;
				
			} else if(obj instanceof InputStream) {
				
				InputStream is = (InputStream) obj;
				return Utils.inputStreamToBytes(is);
				
			} else if(obj instanceof String) {
				
				String path = (String) obj;
				return Utils.inputStreamToBytes(new FileInputStream(new File(path)));
				
			} else {
				
				throw new IllegalArgumentException("The format is not supported : " + obj.getClass());
			}
		}
	}
}
