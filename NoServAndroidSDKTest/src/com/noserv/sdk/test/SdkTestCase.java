package com.noserv.sdk.test;

import java.util.ArrayList;
import java.util.List;

import android.test.ActivityInstrumentationTestCase2;
import bolts.Continuation;
import bolts.Task;

import com.google.android.gcm.GCMRegistrar;
import com.noserv.NoServFile;
import com.noserv.NoServInstallations;
import com.noserv.NoServObject;
import com.noserv.NoServClass;
import com.noserv.NoServQuery;
import com.noserv.NoServUser;
import com.noserv.sdk.TestActivity;
import com.noserv.sdk.test.object.TestObject;

public class SdkTestCase extends ActivityInstrumentationTestCase2<TestActivity> {

	static {
		NoServClass.registerSubclass(TestObject.CLASS_NAME, TestObject.class);
	}
	
	public SdkTestCase() {
		
		super(TestActivity.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testLogIn() throws Exception {
		
		NoServUser user1 = NoServUser.logIn("test1", "1234");
		if(user1 == null) {
			
			user1 = new NoServUser();
			user1.setUserID("test2");
			user1.setPassword("1234");
	
			user1.put("name", "name");
			user1.put("phone", "01012345678");
			user1.put("email", "test@mail.com");
			
			user1.signUp();
			assertNotNull(user1.getObjectId());
		}
		assertNotNull(user1);
		assertNotNull(user1.getSessionToken());
		
		NoServUser user2 = NoServUser.logIn(user1.getSessionToken());
		assertNotNull(user2);
		assertEquals(user1.getObjectId(), user2.getObjectId());
		assertEquals(user1.getSessionToken(), user2.getSessionToken());
	}
	
	public void testNoServObject() throws Exception {
		
		TestObject object = (TestObject) NoServClass.create(TestObject.CLASS_NAME);
		
		object.setKey1(1).setKey2(2).setKey3(3).save();
		
		assertNotNull(object.getObjectId());
	}
	
	public void testNoServQuery() throws Exception {
		
		NoServQuery<TestObject> query = NoServClass.getQuery(TestObject.CLASS_NAME);
		
		List<TestObject> objects;
		objects = query.find();
	
		for (NoServObject object : objects) {
			object.delete();
		}
		objects = query.find();
		assertTrue(objects.isEmpty());
		
		TestObject test1 = (TestObject) NoServClass.create(TestObject.CLASS_NAME);
		TestObject test2 = (TestObject) NoServClass.create(TestObject.CLASS_NAME);
		TestObject test3 = (TestObject) NoServClass.create(TestObject.CLASS_NAME);
		
		test1.setKey1(1).setKey2(1).setKey3(33).save();
		test2.setKey1(2).setKey2(1).setKey3(22).save();
		test3.setKey1(3).setKey2(2).setKey3(11).save();
		
		objects = query.count(true).find();
		assertFalse(objects.isEmpty());
		assertNull(objects.get(0));
		query.clear();
	
		objects = query.whereEq(TestObject.KEY1, 1).find();
		assertFalse(objects.isEmpty());
		assertEquals(objects.get(0).getObjectId(), test1.getObjectId());
		query.clear();
		
		objects = query.whereNotEq(TestObject.KEY2, 1).find();
		assertFalse(objects.isEmpty());
		assertEquals(objects.get(0).getObjectId(), test3.getObjectId());
		query.clear();
		
		objects = query.whereGt(TestObject.KEY1, 1).find();
		assertEquals(objects.size(), 2);
		query.clear();
		
		objects = query.whereGtOrEq(TestObject.KEY1, 1).find();
		assertEquals(objects.size(), 3);
		query.clear();

		objects = query.whereGtOrEq(TestObject.KEY1, 1).order(TestObject.KEY3).find();
		assertEquals(objects.size(), 3);
		assertEquals(objects.get(0).getObjectId(), test3.getObjectId());
		query.clear();

		objects = query.whereGtOrEq(TestObject.KEY1, 1).order("-"+TestObject.KEY3).find();
		assertEquals(objects.size(), 3);
		assertEquals(objects.get(0).getObjectId(), test1.getObjectId());
		query.clear();

		objects = query.whereGtOrEq(TestObject.KEY1, 1).order(TestObject.KEY3).limit(2).find();
		assertEquals(objects.size(), 2);
		assertEquals(objects.get(0).getObjectId(), test3.getObjectId());
		query.clear();

		objects = query.whereGtOrEq(TestObject.KEY1, 1).order(TestObject.KEY3).limit(2).skip(1).find();
		assertEquals(objects.size(), 2);
		assertEquals(objects.get(0).getObjectId(), test2.getObjectId());
		query.clear();
		
		objects = query.whereLt(TestObject.KEY1, 3).find();
		assertEquals(objects.size(), 2);
		query.clear();
		
		objects = query.whereLtOrEq(TestObject.KEY1, 3).find();
		assertEquals(objects.size(), 3);
		query.clear();
		
		List<Integer> list = new ArrayList<Integer>();
		list.add(11); list.add(22); list.add(33);
		objects = query.whereIn(TestObject.KEY3, list).find();
		assertEquals(objects.size(), 3);
		query.clear();

		for (TestObject object : objects) {
			object.delete();
		}
		
		objects = query.find();
		assertTrue(objects.isEmpty());
	}
	
	public void testNoServFile() throws Exception {
		
		NoServFile fileObject = new NoServFile("test1.txt");
		fileObject.setFile("abcdefg".getBytes()).save();
		assertNotNull(fileObject.getUrl());
		assertNotNull(fileObject.getObjectId());
		fileObject.delete();
		
		// TODO 큰파일 전송시 서버에서 응답을 받지 못하는 이슈 있음.
//		String[] IMAGE_PROJECTION = {
//                MediaStore.Images.ImageColumns.DATA,
//                MediaStore.Images.Thumbnails.DATA
//        };
//
//        final Uri uriImages = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//        Cursor cursor = this.getActivity().managedQuery(uriImages, IMAGE_PROJECTION, null, null, null);
//
//        String imagePath = null;
//        if (cursor != null && cursor.moveToLast()) {
//            imagePath = cursor.getString(0);
//        }
//        assertNotNull(imagePath);
//        File file = new File(imagePath);
//        assertTrue(file.exists());
//        
//        fileObject = new NoServFile(file.getName());
//        fileObject.setFile(file).save();
//		assertNotNull(fileObject.getUrl());
//		assertNotNull(fileObject.getObjectId());
//		fileObject.delete();
	}
	
	public void testInBackgroundByTask() throws Exception {
		
		final NoServQuery<TestObject> query = NoServClass.getQuery(TestObject.CLASS_NAME);

		query.findInBackground().onSuccessTask(new Continuation<List<TestObject>, Task<List<TestObject>>>() {

			@Override
			public Task<List<TestObject>> then(Task<List<TestObject>> task) throws Exception {
				
				List<TestObject> objects = task.getResult();
				for (NoServObject object : objects) {
					object.delete();
				}
				return query.findInBackground();
			}
		}, Task.BACKGROUND_EXECUTOR).continueWith(new Continuation<List<TestObject>, Void>() {

			@Override
			public Void then(Task<List<TestObject>> task) throws Exception {
				
				if(task.isFaulted()) {
					throw task.getError();
				}
				
				List<TestObject> objects = task.getResult();
				assertTrue(objects.isEmpty());
				return null;
			}
		}, Task.UI_THREAD_EXECUTOR).waitForCompletion();
	}
	
	public void testPush() throws Exception {
		
		GCMRegistrar.checkDevice(this.getActivity());
		GCMRegistrar.checkManifest(this.getActivity());
		
		String gcmToken = GCMRegistrar.getRegistrationId(this.getActivity());
		
		NoServQuery<NoServObject> query = NoServInstallations.getQuery();
		List<NoServObject> objects = query.whereEq(NoServInstallations.KEY_DEVICE_TOKEN, gcmToken).find();
		if(objects.isEmpty()) {
			
			NoServInstallations installer = new NoServInstallations();
			installer.setChannels("test_channel").setDeviceToken(gcmToken).save();
			assertNotNull(installer.getObjectId());
		} else {
			
			assertEquals(objects.get(0).get(NoServInstallations.KEY_DEVICE_TOKEN), gcmToken);
		}
	}
}
