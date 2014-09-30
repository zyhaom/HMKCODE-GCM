package com.eventbooking.android.gcm;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class MainActivity extends Activity {
    public static final String TAG = "GCM";
    private TextView textViewRegId;
    private GoogleCloudMessaging gcm;
    private String regId;
    private final static String PROJECT_NUMBER = "393260886080";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private Context context;
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String serverUrl = "http://10.100.0.31:4000";
    private static final String email = "clamm@eventbooking.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewRegId = (TextView) findViewById(R.id.editTextRegId);
        context = getApplicationContext();

        if (GooglePlayServicesIsAvailable()) {
            gcm = GoogleCloudMessaging.getInstance(this);

            regId = getRegId(context);

            if (regId.isEmpty()) {
                registerInBackground();
            } else {
                sendRegistrationIdToServer();
                textViewRegId.setText("Device already registered.\n\n" + regId);
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        GooglePlayServicesIsAvailable();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    /**
     * Click handler for buttonGetRegId.
     *
     * @param view The button.
     */
    public void onClick(View view) {
        regId = getRegId(context);

        if (regId.isEmpty()) {
            registerInBackground();
        } else {
            sendRegistrationIdToServer();
            textViewRegId.setText("Device already registered.\n\n" + regId);
        }
    }

    /**
     * Gets the current registration ID for the application on GCM service.
     * <p/>
     * If result is empty, the app will register.
     *
     * @param context The current context.
     *
     * @return The current registration ID or an empty string if the app needs to register.
     */
    private String getRegId(Context context) {
        final SharedPreferences prefs = getAppPreferences();

        String registrationId = prefs.getString(PROPERTY_REG_ID, "");

        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }

        if (appWasUpdated(context, prefs)) return "";

        return registrationId;
    }

    /**
     * Gets the registration ID from the GCM server.
     */
    public void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String message;

                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }

                    regId = gcm.register(PROJECT_NUMBER);

                    message = "Device registered, registration.\n\n" + regId;

                    sendRegistrationIdToServer();

                    storeRegistrationId(context, regId);

                    Log.i(TAG, message);
                } catch (IOException ex) {
                    message = "Error: " + ex.getMessage();
                }

                return message;
            }

            @Override
            protected void onPostExecute(String message) {
                textViewRegId.setText(message);
            }
        }.execute(null, null, null);
    }

    private void sendRegistrationIdToServer() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String message;
                String postUrl = serverUrl + "/Device/Register";

                try {
                    StringEntity registrationIdEntity = new StringEntity(regId);
                    StringEntity emailEntity = new StringEntity(email);

                    HttpClient client = new DefaultHttpClient();
                    HttpPost post = new HttpPost(postUrl);

                    post.setEntity(registrationIdEntity);
                    post.setEntity(emailEntity);

                    Log.i(TAG, "Sending POST to " + postUrl);

                    HttpResponse response = client.execute(post);

                    message = response.getStatusLine().toString();

                    Log.i(TAG, message);
                } catch (IOException e) {
                    message = "Error: " + e.getMessage();
                }

                return message;
            }

            @Override
            protected void onPostExecute(String message) {
                String currentContent = textViewRegId.getText().toString();
                textViewRegId.setText(currentContent + "\n\n" + message);
            }
        }.execute(null, null, null);
    }

    /**
     * Checks if Google Play Services are available on the device.
     *
     * @return Returns true if Google Play Services are available.
     */
    private boolean GooglePlayServicesIsAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not support.");
                finish();
            }

            return false;
        }

        return true;
    }

    /**
     * Gets the application's shared preferences.
     * <p/>
     * The demo had context as a parameter but did not use it.
     *
     * @return The app's shared preferences.
     */
    private SharedPreferences getAppPreferences() {
        return getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    /**
     * Checks if the app was updated. If it was, it must clear the registration ID since the
     * existing ID is not guaranteed to work with the new version.
     *
     * @param context The current context.
     * @param prefs   The app's shared preferences.
     *
     * @return Returns true if the app's current version does not match the registered version.
     */
    private boolean appWasUpdated(Context context, SharedPreferences prefs) {
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);

        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");

            return true;
        }

        return false;
    }

    /**
     * Gets the applications current version number.
     *
     * @param context The current context.
     *
     * @return The app's version code.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get the package name: " + e);
        }
    }

    /**
     * Stores the registration ID and app versionCode in the application's shared preferences.
     *
     * @param context        The current context.
     * @param registrationId GCM registration ID.
     */
    private void storeRegistrationId(Context context, String registrationId) {
        final SharedPreferences prefs = getAppPreferences();

        int appVersion = getAppVersion(context);

        Log.i(TAG, "Saving registration ID on app version " + appVersion);

        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(PROPERTY_REG_ID, registrationId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);

        editor.apply(); // Demo used commit() but JetBrains suggests apply.
    }
}
