package com.franci22.youtubefetcher;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

import java.io.File;

public class DownloadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            DownloadManager mDownloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Cursor c = mDownloadManager.query(query);
            if (c.moveToFirst()) {
                int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                String title = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
                String uri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
                String[] arr = title.split(" - ");
                String linkyt = null;
                for (String firstName : arr) {
                    linkyt = firstName;
                }
                String titleyt = null;
                if (linkyt != null) {
                    titleyt = title.replace(" - " + linkyt, "");
                }
                if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)){
                    if (Build.VERSION.SDK_INT >= 11) {
                        String downloadedPackageUriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                        Utils.scanMedia(downloadedPackageUriString, context);
                        File path = new File(downloadedPackageUriString);
                        Intent i2 = new Intent();
                        i2.setAction(android.content.Intent.ACTION_VIEW);
                        i2.setDataAndType(Uri.fromFile(path), "audio/*");
                        PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), i2, 0);
                        Notification n = null;
                        if (Build.VERSION.SDK_INT >= 16) {
                            n = new Notification.Builder(context)
                                    .setContentTitle(context.getString(R.string.downloadcompleted))
                                    .setContentText(title)
                                    .setAutoCancel(true)
                                    .setContentIntent(pIntent)
                                    .setSmallIcon(R.drawable.ic_notify).build();
                        } else if (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT <= 15) {
                            //noinspection deprecation
                            n = new Notification.Builder(context)
                                    .setContentTitle(context.getString(R.string.downloadcompleted))
                                    .setContentText(title)
                                    .setAutoCancel(true)
                                    .setContentIntent(pIntent)
                                    .setSmallIcon(R.drawable.ic_notify).getNotification();
                        }
                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(0, n);
                        new DBAdapter(context).insertDetails(titleyt, linkyt, downloadedPackageUriString);
                    }
                } else if (DownloadManager.STATUS_FAILED == c.getInt(columnIndex)){
                    if (Build.VERSION.SDK_INT >= 11) {
                        Intent redown = new Intent(context, MainActivity.class);
                        redown.putExtra("link", linkyt);
                        redown.putExtra("name", title);
                        PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), redown, 0);
                        Notification n = null;
                        if (Build.VERSION.SDK_INT >= 16) {
                            n = new Notification.Builder(context)
                                    .setContentTitle(context.getString(R.string.downloadfalied))
                                    .setContentText(context.getString(R.string.touchtoredown) + title)
                                    .setAutoCancel(true)
                                    .setContentIntent(pIntent)
                                    .setSmallIcon(R.drawable.ic_notify).build();
                        } else if (Build.VERSION.SDK_INT >= 11  && Build.VERSION.SDK_INT <= 15){
                            //noinspection deprecation
                            n = new Notification.Builder(context)
                                    .setContentTitle(context.getString(R.string.downloadfalied))
                                    .setContentText(context.getString(R.string.touchtoredown) + title)
                                    .setAutoCancel(true)
                                    .setContentIntent(pIntent)
                                    .setSmallIcon(R.drawable.ic_notify).getNotification();
                        }
                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(0, n);
                    }
                }
            }
        }
    }
}