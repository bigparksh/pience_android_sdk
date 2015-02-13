package com.noserv;

import android.content.Context;
import android.content.Intent;

import com.google.android.gcm.GCMBaseIntentService;

public abstract class NoServGcmIntentService extends GCMBaseIntentService {

	private static String KEY_MSG = "msg";
	private static String KEY_TITLE = "title";
	private static String KEY_ACTION = "action";
	
	@Override
	protected void onError(Context context, String errorId) {
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
	}

	@Override
	final protected void onMessage(Context context, Intent intent) {

		if ("com.google.android.c2dm.intent.RECEIVE".equals(intent.getAction())) {

			String alert = intent.getStringExtra(KEY_MSG);
			String title = intent.getStringExtra(KEY_TITLE);
			String action = intent.getStringExtra(KEY_ACTION);

			this.onReceivedMessage(context, title, alert, action);
		}
	}

	public abstract void onReceivedMessage(Context context, String title, String alert, String action);
}
