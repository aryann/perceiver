package com.aryannayra.perceiver;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PerceptionService extends Service {

    private static final String LOG_TAG = PerceptionService.class.getCanonicalName();
    private static final int SAMPLING_RATE_MILLIS = 500;

    private Timer timer;
    private MediaRecorder mediaRecorder;

    @Override
    public void onCreate() {

        timer = new Timer();

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile("/dev/null");

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to start recording audio: " + e);
            return;
        }
        mediaRecorder.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "Service started.");

        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                Log.i(LOG_TAG, "Max sound amplitude: " + mediaRecorder.getMaxAmplitude());
            }

        }, 0, SAMPLING_RATE_MILLIS);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (timer != null) {
            timer.cancel();
        }
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
        Log.i(LOG_TAG, "Service stopped.");
    }

}
