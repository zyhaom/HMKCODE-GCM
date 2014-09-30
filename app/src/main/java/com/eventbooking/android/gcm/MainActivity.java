package com.eventbooking.android.gcm;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

public class MainActivity extends Activity {
    public static final String TAG = "GCM";

    private EditText editTextRegId;

    private GoogleCloudMessaging gcm;

    private String regId;

    private String PROJECT_NUMBER = "393260886080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextRegId = (EditText) findViewById(R.id.editTextRegId);
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

    public void onClick(View view) {
        getRegId();
    }

    public void getRegId() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String message;

                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }

                    regId = gcm.register(PROJECT_NUMBER);

                    message = "Device registered, registration ID = " + regId;

                    Log.i(TAG, message);
                } catch (IOException ex) {
                    message = "Error: " + ex.getMessage();
                }

                return message;
            }

            @Override
            protected void onPostExecute(String message) {
                editTextRegId.setText(message + "\n");
            }
        }.execute(null, null, null);
    }
}
