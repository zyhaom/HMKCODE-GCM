package com.eventbooking.android.gcm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * This class receives the GCM message and passes it to GcmMessageHandler.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        // Explicitly specify that GcmMessageHandler will handle the intent.
        ComponentName componentName = new ComponentName(context.getPackageName(), GcmMessageHandler.class.getName());

        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(componentName)));

        setResultCode(Activity.RESULT_OK);
    }
}