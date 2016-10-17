package com.franci22.youtubefetcher;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class MainActivity extends AppCompatActivity {

    String url;
    Context ctx;
    CoordinatorLayout coordinatorLayout;
    long enqueue;
    DownloadManager dm;
    Uri uridown;
    String videonamen;
    String linkorigin = null;
    MaterialDialog loading;
    SharedPreferences prfs;
    InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        ctx = this;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ctx, ListActivity.class));
            }
        });
        prfs = getSharedPreferences("settings", Context.MODE_PRIVATE);
        Button disconweb = (Button) findViewById(R.id.discoveronsite);
        disconweb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://franci22.ml/ytfetch"));
                startActivity(i);
            }
        });
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout);
        final EditText linkedittext = (EditText) findViewById(R.id.link);
        Button download = (Button) findViewById(R.id.download);
        Bundle extras = getIntent().getExtras();
        try {
            if (extras.getString("name") != null) {
                linkedittext.setText(extras.getString("link"));
                Snackbar.make(coordinatorLayout, getString(R.string.touchdown) + extras.getString("name"), Snackbar.LENGTH_LONG).show();
            }
        } catch (RuntimeException ignored) {}
        assert download != null;
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.closeKeyboard(ctx, linkedittext.getWindowToken());
                if (Utils.haveInternetConnection(ctx)) {
                    String link = linkedittext.getText().toString().replace(" ", "");
                    String[] arr = link.split("/");
                    for (String firstName : arr) {
                        String replace = firstName.replace("watch?v=", "");
                        if (!replace.equals("youtube.com") || !replace.equals("www.youtube.com")) {
                            url = "http://YoutubeInMP3.com/fetch/?format=JSON&video=http://youtube.com/watch?v=" + replace;
                            linkorigin = "http://youtube.com/watch?v=" + replace;
                        }
                    }
                    new JsonTask().execute();
                    loading = new MaterialDialog.Builder(ctx)
                            .title(R.string.wait)
                            .content(R.string.loading)
                            .progress(true, 0)
                            .show();
                } else {
                    new MaterialDialog.Builder(ctx)
                            .title(R.string.attention)
                            .content(R.string.nointernet)
                            .negativeText(R.string.ok)
                            .show();
                }
            }
        });

        if (prfs.getBoolean("code", false)) {
            new AsyncTask<String, Void, String>() {
                @Override
                protected String doInBackground(String... urls) {
                    return Utils.GET(urls[0]);
                }
                @Override
                protected void onPostExecute(String result) {
                    if (Integer.parseInt(result) == 0) {
                        showad();
                        prfs.edit().remove("code").remove("pcode").apply();
                    }
                }
            }.execute("https://franci22.ml/promocode/verifycode.php?code=" + prfs.getString("pcode", "") + "&app=youtubefetch");
        } else if(Utils.showAd(ctx)){
            showad();
        }
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-5123347830871154/3524145428");
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                Toast.makeText(MainActivity.this, R.string.thanks, Toast.LENGTH_SHORT).show();
                Long tsLong = System.currentTimeMillis() / 1000;
                String ts = tsLong.toString();
                final SharedPreferences.Editor editor = prfs.edit();
                editor.putString("timestamp", ts);
                editor.apply();
            }
        });
        new Update().execute("http://franci22.ml/apk/getversion.php?app=youtubefetch");
    }


    private class Update extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return Utils.GET(urls[0]);
        }
        @Override
        protected void onPostExecute(String result) {
            int version = Integer.parseInt(result);
            try {
                if (version > getPackageManager().getPackageInfo(getPackageName(), 0).versionCode){
                    new MaterialDialog.Builder(ctx)
                            .title(R.string.newupdate)
                            .content(R.string.newupdatecontent)
                            .positiveText(R.string.update)
                            .negativeText(R.string.close)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse("https://franci22.ml/ytfetch"));
                                    startActivity(i);
                                }
                            })
                            .show();
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_setdirectory) {
            Intent i = new Intent(ctx, FilePickerActivity.class);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
            i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
            i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
            startActivityForResult(i, 0);
            return true;
        } else if (id == R.id.action_web) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://franci22.ml"));
            startActivity(i);
        } else if (id == R.id.action_aboutad) {
            requestNewInterstitial();
            new MaterialDialog.Builder(ctx)
                    .title(R.string.aboutad)
                    .content(R.string.aboutadcontent)
                    .positiveText(R.string.show)
                    .neutralText(R.string.promocode)
                    .negativeText(R.string.close)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            if (mInterstitialAd.isLoaded()) {
                                mInterstitialAd.show();
                            } else {
                                Snackbar.make(coordinatorLayout, R.string.adnotloaded, Snackbar.LENGTH_SHORT).show();
                                mInterstitialAd.setAdListener(new AdListener() {
                                    @Override
                                    public void onAdLoaded() {
                                        mInterstitialAd.show();
                                    }
                                });
                            }
                        }
                    })
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            if (!prfs.getBoolean("code", false)) {
                                new MaterialDialog.Builder(ctx)
                                        .title(R.string.promocode)
                                        .customView(R.layout.dialog_promocode, true)
                                        .positiveText(R.string.confirm)
                                        .negativeText(R.string.close)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                                loading = new MaterialDialog.Builder(ctx)
                                                        .title(R.string.wait)
                                                        .content(R.string.loading)
                                                        .progress(true, 0)
                                                        .show();
                                                EditText promocode = (EditText) materialDialog.getCustomView().findViewById(R.id.promocode);
                                                new PromoCode().execute("https://franci22.ml/promocode/promocode.php?code=" + promocode.getText().toString() + "&app=youtubefetch");
                                                SharedPreferences.Editor editor = prfs.edit();
                                                editor.putString("pcode", promocode.getText().toString());
                                                editor.apply();
                                            }
                                        })
                                        .show();
                            } else {
                                new MaterialDialog.Builder(ctx)
                                        .title(R.string.promocode)
                                        .content(R.string.promocodeused)
                                        .negativeText(R.string.close)
                                        .show();
                            }
                        }
                    })
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    private class PromoCode extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return Utils.GET(urls[0]);
        }
        @Override
        protected void onPostExecute(String result) {
            loading.dismiss();
            String msg = getText(R.string.error).toString();
            switch (result) {
                case "0":
                    msg = getText(R.string.servererror).toString();
                    break;
                case "1":
                    msg = getText(R.string.codenotex).toString();
                    break;
                case "2":
                    msg = getText(R.string.codevalid).toString();
                    SharedPreferences.Editor editor = prfs.edit();
                    editor.putBoolean("code", true);
                    editor.apply();
                    break;
                case "3":
                    msg = getText(R.string.codeused).toString();
                    break;
            }
            new MaterialDialog.Builder(ctx)
                    .title(R.string.promocode)
                    .content(msg)
                    .negativeText(R.string.close)
                    .show();
        }
    }

    private class JsonTask extends AsyncTask<Void, Void, Void> {
        String responseStr = null;
        String titlevideo = null;
        String lengthvideo = null;
        String isconverted = "no";

        @Override
        protected Void doInBackground(Void... params) {
            InputStream source = Utils.retrieveStream(url);
            Gson gson = new Gson();
            Reader reader;
            if (source != null) {
                reader = new InputStreamReader(source);
                try {
                    SearchResponse response = gson.fromJson(reader, SearchResponse.class);
                    responseStr = response.link;
                    titlevideo = response.title;
                    lengthvideo = response.length;
                    videonamen = titlevideo;
                } catch (JsonSyntaxException e) {
                    isconverted = "true";
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            loading.dismiss();
            if (responseStr == null && isconverted.equals("no")) {
                new MaterialDialog.Builder(ctx)
                        .content(R.string.urlnovalid)
                        .title(R.string.attention)
                        .negativeText(R.string.close)
                        .show();
            } else if (isconverted.equals("true")) {
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
                                uridown = Uri.parse(responseStr);
                                DownloadManager.Request r = new DownloadManager.Request(uridown);
                                r.setVisibleInDownloadsUi(true);
                                r.setTitle(titlevideo + " - " + linkorigin);
                                String dir = prfs.getString("path", "");
                                if (dir.isEmpty()) {
                                    r.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, titlevideo + ".mp3");
                                } else {
                                    r.setDestinationInExternalPublicDir(dir.replace(Environment.getExternalStorageDirectory().toString(), "") + "/", titlevideo + ".mp3");
                                }
                                /*if (Build.VERSION.SDK_INT >= 11) {
                                    r.allowScanningByMediaScanner();
                                }*/
                                dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                enqueue = dm.enqueue(r);
                                Snackbar.make(coordinatorLayout, R.string.downloadinprogress, Snackbar.LENGTH_LONG).show();
                            }
                        })
                        .show();
            }
            super.onPostExecute(result);
        }
    }

    public class SearchResponse {

        @SerializedName("length")
        public String length;

        @SerializedName("title")
        public String title;

        @SerializedName("link")
        public String link;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if ((0 == requestCode) && resultCode == Activity.RESULT_OK) {
            final String path = data.getData().toString().replace("file://", "");
            final SharedPreferences.Editor editor = prfs.edit();
            editor.putString("path", path);
            editor.apply();
            Snackbar.make(coordinatorLayout, getString(R.string.directorysetted) + path, Snackbar.LENGTH_LONG)
                    .setAction(R.string.restore, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            editor.putString("path", "");
                            editor.apply();
                            Snackbar.make(coordinatorLayout, R.string.annulled, Snackbar.LENGTH_SHORT).show();
                        }
                    }).show();
        }
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

    private void showad(){
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

}