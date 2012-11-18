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


import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;

/**
 * @author brionemde
 *
 */
public class BPRecordEditorText extends Activity implements OnDateSetListener, OnTimeSetListener {

    // Static constants

    private static final String TAG = "BPRecordEditorText";

    private static final String[] PROJECTION = {
        BPRecord._ID,
        BPRecord.SYSTOLIC,
        BPRecord.DIASTOLIC,
        BPRecord.PULSE,
        BPRecord.CREATED_DATE,
        BPRecord.MODIFIED_DATE,
        BPRecord.NOTE
    };

    // BP Record Indices
    // private static final int COLUMN_ID_INDEX = 0;
    private static final int COLUMN_SYSTOLIC_INDEX = 1;
    private static final int COLUMN_DIASTOLIC_INDEX = 2;
    private static final int COLUMN_PULSE_INDEX = 3;
    private static final int COLUMN_CREATED_AT_INDEX = 4;
    private static final int COLUMN_MODIFIED_AT_INDEX = 5;
    private static final int COLUMN_NOTE_INDEX = 6;

    // The different distinct states the activity can be run in.
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;

    private static final int DATE_DIALOG_ID = 0;
    private static final int TIME_DIALOG_ID = 1;
    private static final int DELETE_DIALOG_ID = 2;

    private static final int SYS_IDX = 0;
    private static final int DIA_IDX = 1;
    private static final int PLS_IDX = 2;
    private static final int VALUES_ARRAY_SIZE  = PLS_IDX + 1;

    // Member Variables
    private int mState;

    private Uri mUri;

    private Cursor mCursor;

    private Button mDateButton;
    private Button mTimeButton;

    private Calendar mCalendar = GregorianCalendar.getInstance();

    private TextView mSysLabel;
    private TextView mDiaLabel;
    private TextView mPlsLabel;

    private EditText[] mEditValues = new EditText[VALUES_ARRAY_SIZE];

    private EditText mNote;

    private Bundle mOriginalValues = null;

    @TargetApi(5)
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final Intent intent = getIntent();
        final String action = intent.getAction();

        setContentView(R.layout.bp_record_editor_text);

        if (icicle != null)
            mOriginalValues = new Bundle(icicle);

        if (Intent.ACTION_EDIT.equals(action)) {
            mState = STATE_EDIT;
            mUri = intent.getData();
        } else if (Intent.ACTION_INSERT.equals(action)) {
            mState = STATE_INSERT;
            if (icicle != null)
                mUri = Uri.parse(icicle.getString(BPTrackerFree.MURI));
            else {
                ContentValues cv = new ContentValues();
                cv.put(BPRecord.SYSTOLIC, BPTrackerFree.SYSTOLIC_DEFAULT);
                cv.put(BPRecord.DIASTOLIC, BPTrackerFree.DIASTOLIC_DEFAULT);
                cv.put(BPRecord.PULSE, BPTrackerFree.PULSE_DEFAULT);
                mUri = this.getContentResolver().insert(intent.getData(), cv);
            }
        } else {
            Log.e(TAG, "Unknown action, exiting");
            finish();
            return;
        }

        mCursor = managedQuery(mUri, PROJECTION, null, null, null);

        mDateButton = (Button) findViewById(R.id.date_button);
        mDateButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        mTimeButton = (Button) findViewById(R.id.time_button);
        mTimeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                showDialog(TIME_DIALOG_ID);
            }
        });

        Button button;

        button = (Button) findViewById(R.id.done_button);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                onBackPressed();
            }
        });

        button = (Button) findViewById(R.id.revert_button);
        if(mState == STATE_INSERT)
            button.setText(R.string.menu_discard);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                cancelRecord();
            }
        });

        mSysLabel = (TextView) findViewById(R.id.systolic_label);
        mSysLabel.setText(R.string.label_systolic);

        mEditValues[SYS_IDX] = (EditText) findViewById(R.id.systolic_edit_text);

        mDiaLabel = (TextView) findViewById(R.id.diastolic_label);
        mDiaLabel.setText(R.string.label_diastolic);

        mEditValues[DIA_IDX] = (EditText) findViewById(R.id.diastolic_edit_text);

        mPlsLabel = (TextView) findViewById(R.id.pulse_label);
        mPlsLabel.setText(R.string.label_pulse);

        mEditValues[PLS_IDX] = (EditText) findViewById(R.id.pulse_edit_text);

        mNote = (EditText) findViewById(R.id.note);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If we didn't have any trouble retrieving the data, it is now
        // time to get at the stuff.
        if (mCursor != null && mCursor.moveToFirst()) {

            // Modify our overall title depending on the mode we are running in.
            if (mState == STATE_EDIT) {
                setTitle(getText(R.string.title_edit));
            } else if (mState == STATE_INSERT) {
                setTitle(getText(R.string.title_create));
            }
            int systolic = mCursor.getInt(COLUMN_SYSTOLIC_INDEX);
            mEditValues[SYS_IDX].setText(String.valueOf(systolic));

            int diastolic = mCursor.getInt(COLUMN_DIASTOLIC_INDEX);
            mEditValues[DIA_IDX].setText(String.valueOf(diastolic));

            int pulse = mCursor.getInt(COLUMN_PULSE_INDEX);
            mEditValues[PLS_IDX].setText(String.valueOf(pulse));

            long datetime = mCursor.getLong(COLUMN_CREATED_AT_INDEX);
            long mod_datetime = mCursor.getLong(COLUMN_MODIFIED_AT_INDEX);
            mCalendar.setTimeInMillis(datetime);
            updateDateTimeDisplay();

            String note = mCursor.getString(COLUMN_NOTE_INDEX);
            mNote.setText(note);

            // If we hadn't previously retrieved the original text, do so
            // now. This allows the user to revert their changes.
            if(mOriginalValues == null) {
                mOriginalValues = new Bundle();
                mOriginalValues.putInt(BPRecord.SYSTOLIC, systolic);
                mOriginalValues.putInt(BPRecord.DIASTOLIC, diastolic);
                mOriginalValues.putInt(BPRecord.PULSE, pulse);
                mOriginalValues.putLong(BPRecord.CREATED_DATE, datetime);
                mOriginalValues.putLong(BPRecord.MODIFIED_DATE, mod_datetime);
                mOriginalValues.putString(BPRecord.NOTE, note);
            }
        } else {
            setTitle(getText(R.string.title_error));
        }
    }

    /**
     * Update the date/time display
     */
    public void updateDateTimeDisplay() {
        mDateButton.setText(BPTrackerFree.getDateString(mCalendar.getTime(),
                DateFormat.MEDIUM));
        mTimeButton.setText(BPTrackerFree.getTimeString(mCalendar.getTime(),
                DateFormat.SHORT));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putAll(mOriginalValues);
        outState.putString(BPTrackerFree.MURI, mUri.toString());
    }

    @Override
    protected void onPause() {
        super.onPause();

        // The user is going somewhere else, so make sure their current
        // changes are safely saved away in the provider. We don't need
        // to do this if only editing.
        if (mCursor != null) {
            int systolic = Integer.valueOf(mEditValues[SYS_IDX].getText().toString());
            int diastolic = Integer.valueOf(mEditValues[DIA_IDX].getText().toString());
            int pulse = Integer.valueOf(mEditValues[PLS_IDX].getText().toString());
            long created = (Long) mCalendar.getTimeInMillis();
            String note = mNote.getText().toString();

            ContentValues values = new ContentValues();
            values.put(BPRecord.SYSTOLIC, systolic);
            values.put(BPRecord.DIASTOLIC, diastolic);
            values.put(BPRecord.PULSE, pulse);
            values.put(BPRecord.CREATED_DATE, created);
            values.put(BPRecord.MODIFIED_DATE, System.currentTimeMillis());
            values.put(BPRecord.NOTE, note);
            getContentResolver().update(mUri, values, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        closeCursors();
        super.onDestroy();
    }

    @Override
    protected void finalize() {
        try {
            closeCursors();
            super.finalize();
        } catch (Throwable e) {
            Log.e(TAG, "Finalize error", e);
            e.printStackTrace();
        }
    }

    private void closeRecordCursor() {
        if(mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
    }

    private void closeCursors() {
        closeRecordCursor();
    }

    @TargetApi(5)
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Field sdk_field = null;
        int sdk = 0;
        try {
            sdk_field = android.os.Build.VERSION.class.getField("SDK_INT");
            Build build = new android.os.Build();
            sdk = sdk_field.getInt(build);
        } catch (SecurityException e) {
        } catch (NoSuchFieldException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        }
        if(sdk_field == null) {
            sdk = 0;
        }
        if(sdk < android.os.Build.VERSION_CODES.ECLAIR
            && keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onBackPressed() {
        boolean invalid = false;
        String range;

        String sys_string = mEditValues[SYS_IDX].getText().toString();
        String dia_string = mEditValues[DIA_IDX].getText().toString();
        String pls_string = mEditValues[PLS_IDX].getText().toString();
        if(sys_string == null || sys_string.length() == 0) {
            Toast.makeText(this, R.string.sys_not_empty, Toast.LENGTH_SHORT).show();
            invalid = true;
        } else {
            int systolic = Integer.valueOf(sys_string);
            if(systolic < BPTrackerFree.SYSTOLIC_MIN_DEFAULT || systolic > BPTrackerFree.SYSTOLIC_MAX_DEFAULT) {
                range = getString(R.string.sys_range_error);
                Toast.makeText(this, String.format(range, BPTrackerFree.SYSTOLIC_MIN_DEFAULT,
                        BPTrackerFree.SYSTOLIC_MAX_DEFAULT), Toast.LENGTH_SHORT).show();
                invalid = true;
            }
        }
        if(dia_string == null || dia_string.length() == 0) {
            Toast.makeText(this, R.string.dia_not_empty, Toast.LENGTH_SHORT).show();
            invalid = true;
        } else {
            int diastolic = Integer.valueOf(dia_string);
            if(diastolic < BPTrackerFree.DIASTOLIC_MIN_DEFAULT || diastolic > BPTrackerFree.DIASTOLIC_MAX_DEFAULT) {
                range = getString(R.string.dia_range_error);
                Toast.makeText(this, String.format(range, BPTrackerFree.DIASTOLIC_MIN_DEFAULT,
                    BPTrackerFree.DIASTOLIC_MAX_DEFAULT), Toast.LENGTH_SHORT).show();
                invalid = true;
            }
        }
        if(pls_string == null || pls_string.length() == 0) {
            Toast.makeText(this, R.string.pls_not_empty, Toast.LENGTH_SHORT).show();
            invalid = true;
        } else {
            int pulse = Integer.valueOf(pls_string);
            if(pulse < BPTrackerFree.PULSE_MIN_DEFAULT || pulse > BPTrackerFree.PULSE_MAX_DEFAULT) {
                range = getString(R.string.pls_range_error);
                Toast.makeText(this, String.format(range, BPTrackerFree.PULSE_MIN_DEFAULT,
                    BPTrackerFree.PULSE_MAX_DEFAULT), Toast.LENGTH_SHORT).show();
                invalid = true;
            }
        }

        if(!invalid) {
            finish();
        }
        return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.bp_record_editor_options_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Build the menus that are shown when editing.
        if (mState == STATE_EDIT) {
            menu.setGroupVisible(R.id.edit_menu_group, true);
            menu.setGroupVisible(R.id.create_menu_group, false);
            return true;
        } else if (mState == STATE_INSERT){
            menu.setGroupVisible(R.id.edit_menu_group, false);
            menu.setGroupVisible(R.id.create_menu_group, true);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle all of the possible menu actions.
        switch (item.getItemId()) {
        case R.id.menu_delete:
            showDialog(DELETE_DIALOG_ID);
            return true;
        case R.id.menu_discard:
            cancelRecord();
            return true;
        case R.id.menu_revert:
            cancelRecord();
            return true;
        case R.id.menu_done:
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private ContentValues getOriginalContentValues() {
        ContentValues cv = new ContentValues();
        if(mOriginalValues != null) {
            cv.put(BPRecord.SYSTOLIC, mOriginalValues.getInt(BPRecord.SYSTOLIC));
            cv.put(BPRecord.DIASTOLIC, mOriginalValues.getInt(BPRecord.DIASTOLIC));
            cv.put(BPRecord.PULSE, mOriginalValues.getInt(BPRecord.PULSE));
            cv.put(BPRecord.CREATED_DATE, mOriginalValues.getLong(BPRecord.CREATED_DATE));
            cv.put(BPRecord.MODIFIED_DATE, mOriginalValues.getLong(BPRecord.MODIFIED_DATE));
            cv.put(BPRecord.NOTE, mOriginalValues.getString(BPRecord.NOTE));
        }
        return cv;
    }

    /**
     * Take care of canceling work on a BPRecord. Deletes the record if we had created
     * it, otherwise reverts to the original record data.
     */
    private final void cancelRecord() {
        if (mCursor != null) {
            if (mState == STATE_EDIT) {
                // Restore the original information we loaded at first.
                closeCursors();
                getContentResolver().update(mUri, getOriginalContentValues(), null, null);
            } else if (mState == STATE_INSERT) {
                // We inserted an empty record, make sure to delete it
                deleteRecord();
            }
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * Take care of deleting a record. Simply close the cursor and deletes the entry.
     */
    private final void deleteRecord() {
        closeCursors();
        getContentResolver().delete(mUri, null, null);
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
                        setResult(RESULT_OK);
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.label_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
            return builder.create();
        case DATE_DIALOG_ID:
            return new DatePickerDialog(this, this, mCalendar
                    .get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                    mCalendar.get(Calendar.DAY_OF_MONTH));
        case TIME_DIALOG_ID:
            return new TimePickerDialog(this, this, mCalendar
                    .get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE),
                    false);
        default:
            return null;
        }
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        mCalendar.set(year, month, day);
        long now = System.currentTimeMillis();
        if (mCalendar.getTimeInMillis() > now) {
            Toast.makeText(BPRecordEditorText.this,
                    getString(R.string.msg_future_date), Toast.LENGTH_LONG)
                    .show();
            mCalendar.setTimeInMillis(now);
        }
        updateDateTimeDisplay();
    }

    public void onTimeSet(TimePicker view, int hour, int minute) {
        mCalendar.set(Calendar.HOUR_OF_DAY, hour);
        mCalendar.set(Calendar.MINUTE, minute);
        long now = System.currentTimeMillis();
        if (mCalendar.getTimeInMillis() > now) {
            Toast.makeText(BPRecordEditorText.this,
                    getString(R.string.msg_future_date), Toast.LENGTH_LONG)
                    .show();
            mCalendar.setTimeInMillis(now);
        }
        updateDateTimeDisplay();
    }
}
