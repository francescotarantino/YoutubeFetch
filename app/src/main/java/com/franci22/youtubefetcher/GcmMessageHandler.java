package com.franci22.youtubefetcher;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class GcmMessageHandler extends IntentService {

    public static final int NOTIFICATION_ID = 1;
    private static final String TAG = "GcmMessageHandler";

    public GcmMessageHandler() {
        super("GcmMessageHandler");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        String msgTYPE = extras.getString("Time");
        String msgTXT = extras.getString("Notice");
        sendNotification(msgTXT, msgTYPE);
        Log.i(TAG, "Received: " + extras.toString());
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg, String date) {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("Hai una nuova notifica!")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentText(msg)
                        .setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}