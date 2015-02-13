package com.noserv;

import java.security.KeyStore;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import bolts.Task;

import com.noserv.NoServ.KeyKeeper;
import com.noserv.utils.Utils;

public class NoServNetwork {

	protected enum Method {
		POST,
		PUT,
		GET,
		DELETE
	}
	
	protected static final String HTTP_APPLICATION_ID = "X-Noserv-Application-Id";
	protected static final String HTTP_REST_API_KEY = "X-Noserv-REST-API-Key";
	protected static final String HTTP_MASTER_KEY = "X-Noserv-Master-Key";
	protected static final String HTTP_SESSION_TOKEN = "X-Noserv-Session-Token";
	
	protected String url;
	protected Map<String, Object> dataMap;
	protected Method method;

	public NoServNetwork url(String url) {
		
		this.url = url;
		return this;
	}
	
	public NoServNetwork data(Map<String, Object> dataMap) {
		
		this.dataMap = dataMap;
		return this;
	}
	
	public NoServNetwork method(Method method) {
		
		this.method = method;
		return this;
	}

	public Task<JSONObject> execute() {

		Task<JSONObject> task;
		
		try {
			HttpClient client = this.getHttpClient();
			HttpUriRequest httpRequest = this.HttpUriRequest();
			
			HttpResponse response = client.execute(httpRequest);
			String result = EntityUtils.toString(response.getEntity());
			String code = Integer.toString(response.getStatusLine().getStatusCode());
	
			JSONObject json = null;
			try { json = new JSONObject(result); } catch (Exception e) {}
			NoServException e = this.checkError(json, code);

			if(e != null) {
				task = Task.forError(e);
			} else {
				task = Task.forResult(json);
			}

		} catch (Exception e) {
			task = Task.forError(e);
		}
		
		return task;
	}
	
	protected HttpUriRequest onPostRequest() throws Exception {
		
		HttpUriRequest httpRequest = new HttpPost(this.url);
		
		if(this.dataMap != null && !this.dataMap.isEmpty()) {
			((HttpPost) httpRequest).setEntity(new StringEntity(new JSONObject(this.dataMap).toString(), HTTP.UTF_8));
			httpRequest.setHeader(HTTP.CONTENT_TYPE, "application/json");
		}
		return httpRequest;
	}

	protected HttpUriRequest onPutRequest() throws Exception {
		
		HttpUriRequest httpRequest = new HttpPut(this.url);
		
		if(this.dataMap != null && !this.dataMap.isEmpty()) {
			((HttpPut) httpRequest).setEntity(new StringEntity(new JSONObject(this.dataMap).toString(), HTTP.UTF_8));	
			httpRequest.setHeader(HTTP.CONTENT_TYPE, "application/json");
		}
		return httpRequest;
	}

	protected HttpUriRequest onGetRequest() throws Exception {
		
		String url = this.url;
		if(this.dataMap != null && !this.dataMap.isEmpty()) {
			url += "?" + Utils.MapUtil.mapToString(this.dataMap);
		}

		return new HttpGet(url);
	}

	protected HttpUriRequest onDeleteRequest() throws Exception {
		
		return new HttpDelete(this.url);
	}
	
	protected HttpUriRequest HttpUriRequest() throws Exception {
		
		HttpUriRequest httpRequest = null;
		
		if (Method.POST == this.method) {
			httpRequest = this.onPostRequest();
		} else if (Method.PUT == this.method) {
			httpRequest = this.onPutRequest();
		} else if (Method.GET == this.method) {	
			httpRequest = this.onGetRequest();
		} else if (Method.DELETE == this.method) {	
			httpRequest = this.onDeleteRequest();
		} else {
			throw new IllegalArgumentException("Not implemented method : " + this.method);
		}
		
		if(KeyKeeper.applicationId == null || KeyKeeper.restapiKey == null) {
			throw new IllegalStateException("The applicationId or restapiKey not found.");
		}
		
		httpRequest.setHeader(NoServNetwork.HTTP_APPLICATION_ID, KeyKeeper.applicationId);
		httpRequest.setHeader(NoServNetwork.HTTP_REST_API_KEY, KeyKeeper.restapiKey);
		
		return httpRequest;
	}
	
	protected HttpClient getHttpClient() {
    	
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new NoServSSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient(new BasicHttpParams());
        }
    }
    
	protected NoServException checkError(JSONObject json, String code) throws JSONException {
		
		NoServException e = null;

		if (code.equals("200")) {

		} else if (code.equals("201")) {

		} else if (code.equals("403")) {

			if (json.isNull("code") == false) {
				if (json.getString("code").equals("NotAuthorized")) {
					e = new NoServException(NoServConfig.EXCEPTION_MSG_1, code);
				}
			}
		} else if (code.equals("404")) {

			if (json.isNull("code") == false) {
				if (json.getString("code").equals("NotFound")) {
					e = new NoServException(NoServConfig.EXCEPTION_MSG_2, code);
				} else if (json.getString("code").equals("ResourceNotFound")) {
					e = new NoServException(NoServConfig.EXCEPTION_MSG_8, code);
				}
			}

		} else if (code.equals("409")) {

			if (json.isNull("code") == false) {
				if (json.getString("code").equals("InvalidArgument")) {
					e = new NoServException(NoServConfig.EXCEPTION_MSG_3, code);
				}
			}

		} else if (code.equals("500")) {

			if (json.isNull("code") == false) {
				if (json.getString("code").equals("InternalServerError")) {
					e = new NoServException(NoServConfig.EXCEPTION_MSG_4, code);
				} else if (json.getString("code").equals("InternalError")) {
					e = new NoServException(NoServConfig.EXCEPTION_MSG_4, code);
				}
			}

		} else {
			
			e = new NoServException("Undefined code", code);
		}
		
		return e;
	}
}
