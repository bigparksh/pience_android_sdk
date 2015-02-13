package com.noserv.sdk.test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import android.test.ActivityInstrumentationTestCase2;

import com.noserv.NoServClass;
import com.noserv.NoServQuery;
import com.noserv.sdk.TestActivity;
import com.noserv.sdk.test.object.ApObject;

public class PienceTestCase extends ActivityInstrumentationTestCase2<TestActivity> {

	static {
		NoServClass.registerSubclass(ApObject.CLASS_NAME, ApObject.class);
	}
	
	private NoServQuery<ApObject> query;
	private List<ApObject> objects;
	
	public PienceTestCase() {
		
		super(TestActivity.class);

		this.query = NoServClass.getQuery(ApObject.CLASS_NAME);
		this.objects = new ArrayList<ApObject>();
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		this.query.clear();
		this.objects.clear();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testApsQueryAll() throws Exception {
		
		this.objects = this.query.find();
		assertFalse(this.objects.isEmpty());

		for (ApObject object : this.objects) {
			assertNotNull(object.getObjectId());
			assertNotNull(object.ssid());
//			assertNotNull(object.bssid());
//			assertNotNull(object.password());
//			assertNotNull(object.address());
		}
	}

	public void testApsQueryWhereGeoPoint() throws Exception {
		
		this.objects = this.query.find();
		assertFalse(this.objects.isEmpty());
		
		ApObject targetObject = this.objects.get(0);
		final double latitude = targetObject.latitude();
		final double longitude = targetObject.longitude();
		final double latitudePerKm = 2 * Math.PI * 6400 / 360.0f;
		final double longitudePerKm = 2 * Math.PI * 6400 * Math.cos(Math.toRadians(longitude)) / 360.0f;
		final double latitudeDistance = 1 / latitudePerKm;
		final double longitudeDistance = 1 / longitudePerKm;
		
		this.query.whereGtOrEq(ApObject.KEY_LATITUDE, latitude - latitudeDistance);
		this.query.whereLtOrEq(ApObject.KEY_LATITUDE, latitude + latitudeDistance);
		this.query.whereGtOrEq(ApObject.KEY_LONGITUDE, longitude - longitudeDistance);
		this.query.whereLtOrEq(ApObject.KEY_LONGITUDE, longitude + longitudeDistance);
		this.objects = this.query.find();
		assertFalse(this.objects.isEmpty());
		assertEquals(this.objects.get(0).getObjectId(), targetObject.getObjectId());
	}
	
	public void testApsQueryWhereUpdatedAt() throws Exception {
		
		this.objects = this.query.find();
		assertFalse(this.objects.isEmpty());
		
		Calendar c = Calendar.getInstance();
		this.query.whereLt(ApObject.KEY_UPDATED_AT, this.query.getDateQuery(c.getTime()));
		this.objects = this.query.find();
		assertFalse(this.objects.isEmpty());
	}

}
