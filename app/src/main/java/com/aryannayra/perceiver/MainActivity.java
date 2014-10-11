package com.aryannayra.perceiver;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;


public class MainActivity extends Activity {

    SharedPreferences sharedPref;
    EditText trackingIdEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getPreferences(Context.MODE_PRIVATE);

        String trackingId = getCurrentTrackingId();

        trackingIdEditText = (EditText) findViewById(R.id.edit_analytics_tracking_id);
        trackingIdEditText.setHint(trackingId);
    }

    private String getCurrentTrackingId() {
        return sharedPref.getString(
                getString(R.string.ga_tracking_id_key), null);
    }

    public void updateAnalyticsTrackingId(View view) {
        String newTrackingId = trackingIdEditText.getText().toString();
        String currentTrackingId = getCurrentTrackingId();
        if (newTrackingId != currentTrackingId) {
            sharedPref.edit().putString(getString(R.string.ga_tracking_id_key),
                    newTrackingId).commit();
            if (isServiceRunning(PerceptionService.class)) {
                Intent intent = new Intent(this, PerceptionService.class);
                stopService(intent);
                intent.putExtra(getString(R.string.ga_tracking_id_key), getCurrentTrackingId());
                startService(intent);
            }
        }
    }

    public void onToggleClicked(View view) {
        boolean on = ((Switch) view).isChecked();
        Intent intent = new Intent(this, PerceptionService.class);
        if (on) {
            intent.putExtra(getString(R.string.ga_tracking_id_key), getCurrentTrackingId());
            startService(intent);
        } else {
            stopService(intent);
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service :
                manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
