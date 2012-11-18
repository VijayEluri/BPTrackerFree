/*
 * Copyright 2010 - Brion Noble Emde
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.eyebrowssoftware.bptrackerfree;

import java.lang.ref.WeakReference;
import java.text.DateFormat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;

public class BPRecordList extends ListActivity implements OnClickListener {

    private static final String TAG = "BPRecordList";

    private static final String[] PROJECTION = {
        BPRecord._ID,
        BPRecord.SYSTOLIC,
        BPRecord.DIASTOLIC,
        BPRecord.PULSE,
        BPRecord.CREATED_DATE,
        BPRecord.NOTE
    };

    // private static final int COLUMN_ID_INDEX = 0;
    // private static final int COLUMN_SYSTOLIC_INDEX = 1;
    // private static final int COLUMN_DIASTOLIC_INDEX = 2;
    // private static final int COLUMN_PULSE_INDEX = 3;
    private static final int COLUMN_CREATED_AT_INDEX = 4;
    // private static final int COLUMN_NOTE_INDEX = 5;

    private static final int DELETE_DIALOG_ID = 0;

    private static final String[] VALS = {
        BPRecord.CREATED_DATE,
        BPRecord.CREATED_DATE,
        BPRecord.SYSTOLIC,
        BPRecord.DIASTOLIC,
        BPRecord.PULSE,
        BPRecord.NOTE
    };

    private static final int[] IDS = {
        R.id.date,
        R.id.time,
        R.id.sys_value,
        R.id.dia_value,
        R.id.pulse_value,
        R.id.note
    };

    private static final String CONTEXT_URI = "context_uri";

    private long mContextMenuRecordId = 0;

    private MyAsyncQueryHandler mMAQH;

    private static final int BPRECORDS_TOKEN = 0;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(BPRecords.CONTENT_URI);
        }
        setContentView(R.layout.bp_record_list);

        setTitle(R.string.title_list);

        RelativeLayout mEmptyContent = (RelativeLayout) findViewById(R.id.empty_content);
        mEmptyContent.setOnClickListener(this);

        View v = this.getLayoutInflater().inflate(R.layout.bp_record_list_header, null);

        ListView lv = getListView();
        lv.addHeaderView(v, null, true);
        lv.setOnCreateContextMenuListener(this);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.bp_record_list_item, null, VALS, IDS);
        adapter.setViewBinder(new MyViewBinder());

        mMAQH = new MyAsyncQueryHandler(this.getContentResolver(), adapter);
        mMAQH.startQuery(BPRECORDS_TOKEN, this, intent.getData(), PROJECTION, null, null, BPRecord.CREATED_DATE + " DESC");

        if(savedInstanceState != null) {
            mContextMenuRecordId = savedInstanceState.getLong(CONTEXT_URI);
        }
        setListAdapter(adapter);
    }

    private class MyViewBinder implements SimpleCursorAdapter.ViewBinder {
        String val;

        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            int id = view.getId();

            switch (id) {
            case R.id.sys_value:
            case R.id.dia_value:
            case R.id.pulse_value: // Pulse
                ((TextView) view).setText(String.valueOf(cursor.getInt(columnIndex)));
                return true;
            case R.id.date: // Date
            case R.id.time: // Time -- these use the same cursor column
                long datetime = cursor.getLong(columnIndex);
                val = (id == R.id.date) ? BPTrackerFree.getDateString(datetime, DateFormat.SHORT)
                        : BPTrackerFree.getTimeString(datetime, DateFormat.SHORT);
                ((TextView)view).setText(val);
                return true;
            case R.id.note:
                String note = cursor.getString(columnIndex);
                if(note != null && note.length() > 0) {
                    ((TextView)view).setText(note);
                    view.setVisibility(View.VISIBLE);
                } else {
                    view.setVisibility(View.GONE);
                }
                return true;
            default:
                return false;
            }
        }
    }

    private class MyAsyncQueryHandler extends AsyncQueryHandler {

        private WeakReference<SimpleCursorAdapter> mAdapter;

        public MyAsyncQueryHandler(ContentResolver cr, SimpleCursorAdapter adapter) {
            super(cr);
            mAdapter = new WeakReference<SimpleCursorAdapter>(adapter);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (cursor != null) {
                startManagingCursor(cursor);
                SimpleCursorAdapter adapter = mAdapter.get();
                if(adapter != null) {
                    adapter.changeCursor(cursor);
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(CONTEXT_URI, mContextMenuRecordId);
    }

    @Override
    protected void onDestroy() {
        SimpleCursorAdapter adapter = (SimpleCursorAdapter) this.getListAdapter();
        this.setListAdapter(null);
        if(adapter != null) {
            adapter.changeCursor(null);
        }
        super.onDestroy();
    }


    @Override
    protected void finalize() {
        try {
            SimpleCursorAdapter adapter = (SimpleCursorAdapter) this.getListAdapter();
            this.setListAdapter(null);
            if(adapter != null) {
                adapter.changeCursor(null);
            }
            super.finalize();
        } catch (Throwable e) {
            Log.e(TAG, "Finalize error", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bp_list_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case R.id.menu_send:
            startActivity(new Intent(Intent.ACTION_SEND, BPRecords.CONTENT_URI, this, BPSend.class));
            return true;
        case R.id.menu_data:
            startActivity(new Intent(this, BPDataManager.class));
            return true;
        case R.id.menu_settings:
            Toast.makeText(this, "Settings: Not Implemented", Toast.LENGTH_LONG).show();
            // Fallthrough
        default:
            return false;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {

        AdapterView.AdapterContextMenuInfo info;
        MenuInflater inflater = this.getMenuInflater();
        try {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }
        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
        if (cursor != null && cursor.moveToFirst()) {
            long datetime = cursor.getLong(COLUMN_CREATED_AT_INDEX);
            String date = BPTrackerFree.getDateString(datetime, DateFormat.SHORT);
            String time = BPTrackerFree.getTimeString(datetime, DateFormat.SHORT);
            String fmt = getString(R.string.datetime_format);
            menu.setHeaderTitle(String.format(fmt, date, time));
        }
        inflater.inflate(R.menu.bp_list_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            // Get the Uri of the record we're intending to operate on
            mContextMenuRecordId = info.id;
            Uri contextMenuUri = ContentUris.withAppendedId(getIntent().getData(), mContextMenuRecordId);
            switch (item.getItemId()) {
            case R.id.menu_delete:
                showDialog(DELETE_DIALOG_ID);
                return true;
            case R.id.menu_edit:
                startActivity(new Intent(Intent.ACTION_EDIT, contextMenuUri));
                return true;
            case R.id.menu_send:
                startActivity(new Intent(Intent.ACTION_SEND, contextMenuUri, this, BPSend.class));
                return true;
            }
            return false;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }

    }

    private void deleteRecord() {
        Uri contextMenuUri = ContentUris.withAppendedId(getIntent().getData(), mContextMenuRecordId);
        getContentResolver().delete(contextMenuUri, null, null);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DELETE_DIALOG_ID:
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.really_delete))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.label_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteRecord();
                    }
                })
                .setNegativeButton(getString(R.string.label_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
            return builder.create();
        default:
            return null;
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if(id < 0) {
            Uri data = getIntent().getData();
            startActivity(new Intent(Intent.ACTION_INSERT, data));
        } else {
            Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
            String action = getIntent().getAction();
            if (Intent.ACTION_PICK.equals(action)
                    || Intent.ACTION_GET_CONTENT.equals(action)) {
                // The caller is waiting for us to return a note selected by
                // the user. The have clicked on one, so return it now.
                setResult(RESULT_OK, new Intent().setData(uri));
            } else {
                // Launch activity to view/edit the currently selected item
                startActivity(new Intent(Intent.ACTION_EDIT, uri));
            }
        }
    }

    public void onClick(View v) {
        Uri data = getIntent().getData();
        startActivity(new Intent(Intent.ACTION_INSERT, data));
    }
}
