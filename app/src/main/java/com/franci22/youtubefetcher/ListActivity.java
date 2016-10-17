package com.franci22.youtubefetcher;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;

public class ListActivity extends AppCompatActivity {

    RecyclerView list;
    LinearLayoutManager layoutManager;
    FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Context ctx = this;
        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordlist);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(ctx)
                        .title(R.string.attention)
                        .content(R.string.deletealllist)
                        .negativeText(R.string.no)
                        .positiveText(R.string.yes)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                new DBAdapter(ctx).deleteTable();
                                updaterv();
                                Snackbar.make(coordinatorLayout, R.string.listdeleted, Snackbar.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            }
        });
        list = (RecyclerView) findViewById(R.id.my_recycler_view);
        layoutManager = new LinearLayoutManager(this);
        updaterv();
        ItemClickSupport.addTo(list).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                TextView txt1 = (TextView) recyclerView.getChildAt(position - firstVisiblePosition).findViewById(R.id.idlist);
                final int id = Integer.parseInt(txt1.getText().toString());
                TextView txt2 = (TextView) recyclerView.getChildAt(position - firstVisiblePosition).findViewById(R.id.linkitem);
                final String linkitem = txt2.getText().toString();
                TextView txt3 = (TextView) recyclerView.getChildAt(position - firstVisiblePosition).findViewById(R.id.item_title);
                final String title = txt3.getText().toString();
                TextView txt4 = (TextView) recyclerView.getChildAt(position - firstVisiblePosition).findViewById(R.id.pathitem);
                final String path = txt4.getText().toString();
                new MaterialDialog.Builder(ctx)
                        .title(title)
                        .items(R.array.listonclick)
                        .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                                if (charSequence == getString(R.string.copyurl)){
                                    copyTextToClipboard(linkitem);
                                    Snackbar.make(coordinatorLayout, R.string.copied,Snackbar.LENGTH_SHORT).show();
                                } else if (charSequence == getString(R.string.openonyoutube)){
                                    try {
                                        String idvideo = null;
                                        String[] arr = linkitem.split("/");
                                        for(String firstName : arr) {
                                            String replace = firstName.replace("watch?v=", "");
                                            if (!replace.equals("youtube.com") || !replace.equals("www.youtube.com")) {
                                                idvideo = replace;
                                            }
                                        }
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + idvideo));
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    } catch (ActivityNotFoundException e) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkitem));
                                        startActivity(intent);
                                    }

                                } else if (charSequence == getString(R.string.delete)){
                                    new MaterialDialog.Builder(ctx)
                                            .title(R.string.attention)
                                            .content(R.string.wantdeletefile)
                                            .negativeText(R.string.no)
                                            .positiveText(R.string.yes)
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                                    File file = new File(path);
                                                    if (file.exists()) {
                                                        if (file.delete()){
                                                            new MaterialDialog.Builder(ctx)
                                                                    .title(R.string.attention)
                                                                    .negativeText(R.string.ok)
                                                                    .content(R.string.filedeleted)
                                                                    .show();
                                                        } else {
                                                            new MaterialDialog.Builder(ctx)
                                                                    .title(R.string.attention)
                                                                    .negativeText(R.string.ok)
                                                                    .content(R.string.error)
                                                                    .show();
                                                        }
                                                    } else {
                                                        new MaterialDialog.Builder(ctx)
                                                                .title(R.string.attention)
                                                                .negativeText(R.string.ok)
                                                                .content(R.string.filenotexist)
                                                                .show();
                                                    }
                                                }
                                            })
                                            .show();
                                    new DBAdapter(ctx).deletOneRecord(id);
                                    updaterv();
                                } else if (charSequence == getString(R.string.redownload)){
                                    Intent redown = new Intent(ctx, MainActivity.class);
                                    redown.putExtra("link", linkitem);
                                    redown.putExtra("name", title);
                                    startActivity(redown);
                                    finish();
                                } else if (charSequence == getString(R.string.listen)){
                                    File file = new File(path);
                                    if (file.exists()) {
                                        Intent intent = new Intent();
                                        intent.setAction(android.content.Intent.ACTION_VIEW);
                                        intent.setDataAndType(Uri.fromFile(file), "audio/*");
                                        startActivity(intent);
                                    } else {
                                        new MaterialDialog.Builder(ctx)
                                                .title(R.string.attention)
                                                .negativeText(R.string.ok)
                                                .content(R.string.filenotexist)
                                                .show();
                                    }
                                }
                                return false;
                            }
                        })
                .positiveText(R.string.choose)
                .show();
            }
        });

    }

    public abstract class CursorRecyclerAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

        protected boolean mDataValid;
        protected Cursor mCursor;
        protected int mRowIDColumn;

        public CursorRecyclerAdapter(Cursor c) {
            init(c);
        }

        void init(Cursor c) {
            boolean cursorPresent = c != null;
            mCursor = c;
            mDataValid = cursorPresent;
            mRowIDColumn = cursorPresent ? c.getColumnIndexOrThrow("_id") : -1;
            setHasStableIds(true);
        }

        @Override
        public final void onBindViewHolder (VH holder, int position) {
            if (!mDataValid) {
                throw new IllegalStateException("this should only be called when the cursor is valid");
            }
            if (!mCursor.moveToPosition(position)) {
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }

            onBindViewHolder(holder, mCursor);
        }

        public abstract void onBindViewHolder(VH holder, Cursor cursor);

        public Cursor getCursor() {
            return mCursor;
        }

        @Override
        public int getItemCount () {
            if (mDataValid && mCursor != null) {
                return mCursor.getCount();
            } else {
                return 0;
            }
        }

        @Override
        public long getItemId (int position) {
            if(hasStableIds() && mDataValid && mCursor != null){
                if (mCursor.moveToPosition(position)) {
                    return mCursor.getLong(mRowIDColumn);
                } else {
                    return RecyclerView.NO_ID;
                }
            } else {
                return RecyclerView.NO_ID;
            }
        }

        public void changeCursor(Cursor cursor) {
            Cursor old = swapCursor(cursor);
            if (old != null) {
                old.close();
            }
        }

        public Cursor swapCursor(Cursor newCursor) {
            if (newCursor == mCursor) {
                return null;
            }
            Cursor oldCursor = mCursor;
            mCursor = newCursor;
            if (newCursor != null) {
                mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
                mDataValid = true;
                // notify the observers about the new cursor
                notifyDataSetChanged();
            } else {
                mRowIDColumn = -1;
                mDataValid = false;
                // notify the observers about the lack of a data set
                notifyItemRangeRemoved(0, getItemCount());
            }
            return oldCursor;
        }
        public CharSequence convertToString(Cursor cursor) {
            return cursor == null ? "" : cursor.toString();
        }
    }

    public class SimpleCursorRecyclerAdapter extends CursorRecyclerAdapter<SimpleViewHolder> {

        private int mLayout;
        private int[] mFrom;
        private int[] mTo;
        private String[] mOriginalFrom;

        public SimpleCursorRecyclerAdapter (int layout, Cursor c, String[] from, int[] to) {
            super(c);
            mLayout = layout;
            mTo = to;
            mOriginalFrom = from;
            findColumns(c, from);
        }

        @Override
        public SimpleViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(mLayout, parent, false);
            return new SimpleViewHolder(v, mTo);
        }

        @Override
        public void onBindViewHolder (SimpleViewHolder holder, Cursor cursor) {
            final int count = mTo.length;
            final int[] from = mFrom;

            for (int i = 0; i < count; i++) {
                holder.views[i].setText(cursor.getString(from[i]));
            }
        }

        private void findColumns(Cursor c, String[] from) {
            if (c != null) {
                int i;
                int count = from.length;
                if (mFrom == null || mFrom.length != count) {
                    mFrom = new int[count];
                }
                for (i = 0; i < count; i++) {
                    mFrom[i] = c.getColumnIndexOrThrow(from[i]);
                }
            } else {
                mFrom = null;
            }
        }

        @Override
        public Cursor swapCursor(Cursor c) {
            findColumns(c, mOriginalFrom);
            return super.swapCursor(c);
        }
    }

    class SimpleViewHolder extends RecyclerView.ViewHolder {
        public TextView[] views;

        public SimpleViewHolder (View itemView, int[] to)
        {
            super(itemView);
            views = new TextView[to.length];
            for(int i = 0 ; i < to.length ; i++) {
                views[i] = (TextView) itemView.findViewById(to[i]);
            }
        }
    }

    private void updaterv(){
        DBAdapter adapter_ob = new DBAdapter(this);
        String[] from = { DBHelper.title,  DBHelper.code, DBHelper.KEY_ID, DBHelper.path };
        int[] to = { R.id.item_title, R.id.linkitem, R.id.idlist, R.id.pathitem };
        Cursor cursor = adapter_ob.queryName();
        list.setAdapter(new SimpleCursorRecyclerAdapter(R.layout.item_recycler, cursor, from, to));
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        list.setLayoutManager(layoutManager);
        if (list.getAdapter().getItemCount() == 0){
            TextView listempty = (TextView) findViewById(R.id.listempty);
            listempty.setVisibility(View.VISIBLE);
            fab.setVisibility(View.INVISIBLE);
        }
    }

    private void copyTextToClipboard(String text) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            copyTextPreHC(text);
        } else {
            copyTextHC(text);
        }
    }

    @SuppressWarnings("deprecation")
    private void copyTextPreHC(String text) {
        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setText(text);
    }

    @TargetApi(11)
    private void copyTextHC(String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("YoutubeFetch", text);
        clipboard.setPrimaryClip(clip);
    }

}