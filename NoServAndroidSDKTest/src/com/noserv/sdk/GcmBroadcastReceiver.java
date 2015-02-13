package com.noserv.sdk;

import android.content.Context;

import com.google.android.gcm.GCMBroadcastReceiver;

public class GcmBroadcastReceiver extends GCMBroadcastReceiver {

    @Override
    protected String getGCMIntentServiceClassName(Context context) {
        return GcmIntentService.class.getCanonicalName();
    }
}