package com.eventbooking.android.gcm;

import android.app.IntentService;
import android.content.Intent;

public class GcmMessageHandler extends IntentService {
    public GcmMessageHandler() {
        super("GcmMessageHandler");
    }

    @Override protected void onHandleIntent(Intent intent) {

    }
}