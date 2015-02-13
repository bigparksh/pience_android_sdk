package com.noserv.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

public class Utils {

	public static void AlertView(Context context, String msg) {
		
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	public boolean NetStatusChk(String url) throws Exception {

		boolean result = false;
		try {
			HttpGet request = new HttpGet(url);

			HttpParams httpParameters = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(httpParameters, 500);
			HttpClient httpClient = new DefaultHttpClient(httpParameters);
			HttpResponse response = httpClient.execute(request);

			int status = response.getStatusLine().getStatusCode();

			if (status == HttpStatus.SC_OK) {
				result = true;
			}
		} catch (UnknownHostException e) {
			result = false; // this is somewhat expected
		}
		return result;

	}

	public static String getMacAddr(Context context) {

		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		String macAddress = wifiInfo.getMacAddress();
		macAddress = macAddress.replace(":", "").toUpperCase(Locale.getDefault());

		return macAddress;
	}

	public static byte[] inputStreamToBytes(InputStream is) throws IOException {

		int len;
		int size = 1024;
		byte[] buf;

		if (is instanceof ByteArrayInputStream) {
			size = is.available();
			buf = new byte[size];
			len = is.read(buf, 0, size);
		} else {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			buf = new byte[size];
			while ((len = is.read(buf, 0, size)) != -1)
				bos.write(buf, 0, len);
			buf = bos.toByteArray();
		}
		return buf;
	}
	
	public static class MapUtil {
		
		public static String mapToString(Map<String, Object> map) {
			StringBuilder stringBuilder = new StringBuilder();

			for (String key : map.keySet()) {
				if (stringBuilder.length() > 0) {
					stringBuilder.append("&");
				}
				Object value = map.get(key);
				try {
					stringBuilder.append((key != null ? URLEncoder.encode(key, "UTF-8") : ""));
					stringBuilder.append("=");
					stringBuilder.append(value != null ? URLEncoder.encode(value.toString(), "UTF-8") : "");
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException("This method requires UTF-8 encoding support", e);
				}
			}

			return stringBuilder.toString();
		}

		public static Map<String, Object> stringToMap(String input) {
			
			Map<String, Object> map = new HashMap<String, Object>();

			String[] nameValuePairs = input.split("&");
			for (String nameValuePair : nameValuePairs) {
				String[] nameValue = nameValuePair.split("=");
				try {
					map.put(URLDecoder.decode(nameValue[0], "UTF-8"), nameValue.length > 1 ? URLDecoder.decode(nameValue[1], "UTF-8") : "");
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException("This method requires UTF-8 encoding support", e);
				}
			}

			return map;
		}
	}
	
	public static class IsoDate {

		public static final int DATE = 1;
		public static final int TIME = 2;
		public static final int DATE_TIME = 3;

		public IsoDate() {
		}

		static void dd(StringBuffer stringbuffer, int i) {
			stringbuffer.append((char) (48 + i / 10));
			stringbuffer.append((char) (48 + i % 10));
		}

		public static String dateToString(Date date, int i) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
			calendar.setTime(date);
			StringBuffer stringbuffer = new StringBuffer();
			if ((i & 1) != 0) {
				int j = calendar.get(1);
				dd(stringbuffer, j / 100);
				dd(stringbuffer, j % 100);
				stringbuffer.append('-');
				dd(stringbuffer, (calendar.get(2) - 0) + 1);
				stringbuffer.append('-');
				dd(stringbuffer, calendar.get(5));
				if (i == 3) {
					stringbuffer.append("T");
				}
			}
			if ((i & 2) != 0) {
				dd(stringbuffer, calendar.get(11));
				stringbuffer.append(':');
				dd(stringbuffer, calendar.get(12));
				stringbuffer.append(':');
				dd(stringbuffer, calendar.get(13));
				stringbuffer.append('.');
				int k = calendar.get(14);
				stringbuffer.append((char) (48 + k / 100));
				dd(stringbuffer, k % 100);
				stringbuffer.append('Z');
			}
			return stringbuffer.toString();
		}

		public static Date stringToDate(String s, int i) {
			Calendar calendar = Calendar.getInstance();
			if ((i & 1) != 0) {
				calendar.set(1, Integer.parseInt(s.substring(0, 4)));
				calendar.set(2, (Integer.parseInt(s.substring(5, 7)) - 1) + 0);
				calendar.set(5, Integer.parseInt(s.substring(8, 10)));
				if (i != 3 || s.length() < 11) {
					calendar.set(11, 0);
					calendar.set(12, 0);
					calendar.set(13, 0);
					calendar.set(14, 0);
					return calendar.getTime();
				}
				s = s.substring(11);
			} else {
				calendar.setTime(new Date(0L));
			}
			calendar.set(11, Integer.parseInt(s.substring(0, 2)));
			calendar.set(12, Integer.parseInt(s.substring(3, 5)));
			calendar.set(13, Integer.parseInt(s.substring(6, 8)));
			int j = 8;
			if (j < s.length() && s.charAt(j) == '.') {
				int k = 0;
				int l = 100;
				do {
					char c = s.charAt(++j);
					if (c < '0' || c > '9') {
						break;
					}
					k += (c - 48) * l;
					l /= 10;
				} while (true);
				calendar.set(14, k);
			} else {
				calendar.set(14, 0);
			}
			if (j < s.length()) {
				if (s.charAt(j) == '+' || s.charAt(j) == '-') {
					calendar.setTimeZone(TimeZone.getTimeZone("GMT"
							+ s.substring(j)));
				} else if (s.charAt(j) == 'Z') {
					calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
				} else {
					throw new RuntimeException("illegal time format!");
				}
			}
			return calendar.getTime();
		}
	}
}
