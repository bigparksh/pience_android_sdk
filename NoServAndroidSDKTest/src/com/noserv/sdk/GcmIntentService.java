package com.noserv.sdk;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.noserv.NoServGcmIntentService;

public class GcmIntentService extends NoServGcmIntentService {
	
	@Override
	public void onReceivedMessage(Context context, String title, String alert, String action) {
		
		try {

			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			Notification notification = new Notification(R.drawable.ic_launcher, alert, System.currentTimeMillis());			

			Intent intent = null;
			intent = new Intent(context, PushReceive.class);
			intent.putExtra("title", title);
			intent.putExtra("alert", alert);
			intent.putExtra("action", action);

			PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

			notification.setLatestEventInfo(context, title, alert, pi);
			notification.defaults |= Notification.DEFAULT_SOUND;
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notificationManager.notify(0, notification);

		} catch (Exception e) {
			Log.e("NoN", "[setNotification] Exception : " + e.getMessage());
		}		
	}
}
