package com.franci22.youtubefetcher;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class DownloadActivity extends AppCompatActivity {

    String link, url, idvideo;
    Context ctx;
    MaterialDialog loading;
    InterstitialAd mInterstitialAd;
    SharedPreferences prfs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        prfs = getSharedPreferences("settings", Context.MODE_PRIVATE);
        ctx = this;
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-5123347830871154/8852008622");
        if (prfs.getBoolean("code", false)) {
            new AsyncTask<String, Void, String>() {
                @Override
                protected String doInBackground(String... urls) {
                    return Utils.GET(urls[0]);
                }

                @Override
                protected void onPostExecute(String result) {
                    if (Integer.parseInt(result) == 0) {
                        requestNewInterstitial();
                        prfs.edit().remove("code").remove("pcode").apply();
                    }
                }
            }.execute("https://franci22.ml/promocode/verifycode.php?code=" + prfs.getString("pcode", "") + "&app=youtubefetch");
        } else if(Utils.showAd(ctx)){
            requestNewInterstitial();
        }
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mInterstitialAd.show();
                if (prfs.getBoolean("toastaddown", true)) {
                    Toast.makeText(DownloadActivity.this, R.string.addown, Toast.LENGTH_LONG).show();
                    SharedPreferences.Editor editor = prfs.edit();
                    editor.putBoolean("toastaddown", false);
                    editor.apply();
                }
            }
        });
        loading = new MaterialDialog.Builder(ctx)
                .title(R.string.wait)
                .content(R.string.loading)
                .progress(true, 0)
                .show();
        Bundle extras = getIntent().getExtras();
        link = extras.getString(Intent.EXTRA_TEXT);
        if (Utils.haveInternetConnection(ctx)) {
            try {
                String[] arr = link.split("/");
                for (String firstName : arr) {
                    url = "http://YoutubeInMP3.com/fetch/?format=JSON&video=http://youtube.com/watch?v=" + firstName;
                    idvideo = firstName;
                }
                new JsonTask().execute();
            } catch (NullPointerException e) {
                loading.dismiss();
                new MaterialDialog.Builder(ctx)
                        .title(R.string.attention)
                        .content(R.string.onlyyt)
                        .negativeText(R.string.close)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                finish();
                            }
                        })
                        .show();
            }
        } else {
            loading.dismiss();
            new MaterialDialog.Builder(ctx)
                    .title(R.string.attention)
                    .content(R.string.nointernet)
                    .negativeText(R.string.ok)
                    .show();
        }
    }

    private class JsonTask extends AsyncTask<Void, Void, Void> {
        String responseStr = null;
        String titlevideo = null;
        String lengthvideo = null;
        boolean isntconverted = false;

        @Override
        protected Void doInBackground(Void... params) {
            InputStream source = Utils.retrieveStream(url);
            Gson gson = new Gson();
            Reader reader;
            if (source != null) {
                reader = new InputStreamReader(source);
                try {
                    MainActivity.SearchResponse response = gson.fromJson(reader, MainActivity.SearchResponse.class);
                    responseStr = response.link;
                    titlevideo = response.title;
                    lengthvideo = response.length;
                } catch (JsonSyntaxException e) {
                    isntconverted = true;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            loading.dismiss();
            if (isntconverted) {
                new MaterialDialog.Builder(ctx)
                        .content(R.string.videodaconv)
                        .title(R.string.attention)
                        .positiveText(R.string.open)
                        .negativeText(R.string.close)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                                Toast.makeText(ctx, getString(R.string.press) + " Convert & Download MP3", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                finish();
                            }
                        })
                        .show();
            } else {
                int length = Integer.parseInt(lengthvideo);
                int minutes = length / 60;
                int seconds = length % 60;
                String time;
                if (seconds < 10) {
                    time = Integer.toString(minutes) + ":0" + Integer.toString(seconds);
                } else {
                    time = Integer.toString(minutes) + ":" + Integer.toString(seconds);
                }
                new MaterialDialog.Builder(ctx)
                        .title(R.string.details)
                        .content(getString(R.string.videotitle) + titlevideo + "\n" + getString(R.string.lenght) + time)
                        .negativeText(R.string.close)
                        .positiveText(R.string.download)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                String linkorigin = "http://youtube.com/watch?v=" + idvideo;
                                Uri uridown = Uri.parse(responseStr);
                                DownloadManager.Request r = new DownloadManager.Request(uridown);
                                r.setVisibleInDownloadsUi(true);
                                String dir = prfs.getString("path", "");
                                if (dir.isEmpty()) {
                                    r.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, titlevideo + ".mp3");
                                } else {
                                    r.setDestinationInExternalPublicDir(dir.replace(Environment.getExternalStorageDirectory().toString(), "") + "/", titlevideo + ".mp3");
                                }
                                /*if (Build.VERSION.SDK_INT >= 11) {
                                    r.allowScanningByMediaScanner();
                                }*/
                                r.setTitle(titlevideo + " - " + linkorigin);
                                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                dm.enqueue(r);
                                finish();
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                finish();
                            }
                        })
                        .show();
                super.onPostExecute(result);
            }

        }

    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

}