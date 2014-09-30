package com.eventbooking.android.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * This class defines what to do with the received message.
 */
public class GcmMessageHandler extends IntentService {
    private String message;
    private Handler handler;

    public GcmMessageHandler() {
        super("GcmMessageHandler");
    }

    @Override
    public void onCreate() {
        // TODO: Auto-generated method stub.

        super.onCreate();

        handler = new Handler();
    }

    /**
     * Processes the message received from GCM.
     *
     * @param intent The intent to be handled.
     */
    @Override protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        message = extras.getString("title");

        showToast();

        Log.i(MainActivity.TAG, "Received: (" + messageType + ") " + message);

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    /**
     * Shows a toast notification.
     */
    public void showToast() {
        handler.post(new Runnable() {
            @Override public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
}