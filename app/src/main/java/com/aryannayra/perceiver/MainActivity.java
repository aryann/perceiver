package com.aryannayra.perceiver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onToggleClicked(View view) {
        boolean on = ((Switch) view).isChecked();
        Intent intent = new Intent(this, PerceptionService.class);
        if (on) {
            startService(intent);
        } else {
            stopService(intent);
        }
    }
}
