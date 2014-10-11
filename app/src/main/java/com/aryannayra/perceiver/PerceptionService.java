package com.aryannayra.perceiver;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PerceptionService extends Service {

    private static final String LOG_TAG = PerceptionService.class.getCanonicalName();

    /**
     * How often data should be collected and sent to Google Analytics.
     */
    private static final int DELAY_BETWEEN_SAMPLES_MILLIS = 2000;

    private ScheduledThreadPoolExecutor executor;
    private MediaRecorder mediaRecorder;


    @Override
    public void onCreate() {
        executor = new ScheduledThreadPoolExecutor(1);

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

        String trackingId = intent.getStringExtra(getString(R.string.ga_tracking_id_key));
        Log.d(LOG_TAG, "Google Analytics tracking ID: " + trackingId);

        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        final Tracker tracker = analytics.newTracker(trackingId);

        executor.scheduleAtFixedRate(
                new Runnable() {

                    @Override
                    public void run() {
                        // This is pretty awful. A better approach would be to collect data at a
                        // faster rate in one thread and send batches of data to Google Analytics
                        // in another thread. Unfortunately, it doesn't look like the Analytics SDK
                        // has a mechanism for batching AND it is not possible to artificially set
                        // the time of the hits. Yikes! I might have to write my own server. Or
                        // maybe I just didn't look at the docs enough, and I am wrong.
                        int amplitude = mediaRecorder.getMaxAmplitude();
                        if (amplitude == 0) {
                            return;
                        }
                        Log.d(LOG_TAG, "Max sound amplitude: " + amplitude);
                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory("sensors")
                                .setAction("sound")
                                .setValue(amplitude)
                                .build());
                    }

                }, 0, DELAY_BETWEEN_SAMPLES_MILLIS, TimeUnit.MILLISECONDS);

        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (executor != null) {
            executor.shutdown();
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
