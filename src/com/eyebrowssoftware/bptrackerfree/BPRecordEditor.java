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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;

/**
 * @author brionemde
 *
 */
public class BPRecordEditor extends BPRecordEditorBase implements OnItemSelectedListener {

    // Static constants

    protected static final String TAG = "BPRecordEditor";

    protected static final int SYS_IDX = 0;
    protected static final int DIA_IDX = 1;
    protected static final int PLS_IDX = 2;
    protected static final int SPINNER_ARRAY_SIZE  = PLS_IDX + 1;

    protected static final int SPINNER_ITEM_RESOURCE_ID = R.layout.bp_spinner_item;
    protected static final int SPINNER_ITEM_TEXT_VIEW_ID = android.R.id.text1;

    // Member Variables
    protected Spinner[] mSpinners = null;

    protected Bundle mOriginalValues = null;

    protected MyAsyncQueryHandler mMAQH;

    protected static final int BPRECORDS_TOKEN = 0;

    protected WeakReference<Spinner[]> mSpinnersReference;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final Intent intent = getIntent();
        final String action = intent.getAction();

        setContentView(R.layout.bp_record_editor);

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
                cv.put(BPRecord.CREATED_DATE, GregorianCalendar.getInstance().getTimeInMillis());
                mUri = this.getContentResolver().insert(intent.getData(), cv);
            }
        } else {
            Log.e(TAG, "Unknown action, exiting");
            finish();
            return;
        }

        mCalendar = new GregorianCalendar();
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

        mDoneButton = (Button) findViewById(R.id.done_button);
        mDoneButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                finish();
            }
        });

        mCancelButton = (Button) findViewById(R.id.revert_button);
        if(mState == STATE_INSERT)
            mCancelButton.setText(R.string.menu_discard);
        mCancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                cancelRecord();
            }
        });

        int[] sys_vals = {
            BPTrackerFree.SYSTOLIC_MAX_DEFAULT,
            RangeAdapter.NO_ZONE,
            RangeAdapter.NO_ZONE,
            RangeAdapter.NO_ZONE,
            BPTrackerFree.SYSTOLIC_MIN_DEFAULT
        };

        int[] dia_vals = {
            BPTrackerFree.DIASTOLIC_MAX_DEFAULT,
            RangeAdapter.NO_ZONE,
            RangeAdapter.NO_ZONE,
            RangeAdapter.NO_ZONE,
            BPTrackerFree.DIASTOLIC_MIN_DEFAULT
        };

        int[] pls_vals = {
            BPTrackerFree.PULSE_MAX_DEFAULT,
            RangeAdapter.NO_ZONE,
            RangeAdapter.NO_ZONE,
            RangeAdapter.NO_ZONE,
            BPTrackerFree.PULSE_MIN_DEFAULT
        };

        mSpinners = new Spinner[SPINNER_ARRAY_SIZE];

        mSpinners[SYS_IDX] = (Spinner) findViewById(R.id.systolic_spin);
        mSpinners[SYS_IDX].setPromptId(R.string.label_sys_spinner);
        mSpinners[SYS_IDX].setOnItemSelectedListener(this);
        mSpinners[SYS_IDX].setAdapter(new RangeAdapter(this, sys_vals, true, SPINNER_ITEM_RESOURCE_ID, SPINNER_ITEM_TEXT_VIEW_ID));

        mSpinners[DIA_IDX] = (Spinner) findViewById(R.id.diastolic_spin);
        mSpinners[DIA_IDX].setPromptId(R.string.label_dia_spinner);
        mSpinners[DIA_IDX].setOnItemSelectedListener(this);
        mSpinners[DIA_IDX].setAdapter(new RangeAdapter(this, dia_vals, true, SPINNER_ITEM_RESOURCE_ID, SPINNER_ITEM_TEXT_VIEW_ID));

        mSpinners[PLS_IDX] = (Spinner) findViewById(R.id.pulse_spin);
        mSpinners[PLS_IDX].setPromptId(R.string.label_pls_spinner);
        mSpinners[PLS_IDX].setOnItemSelectedListener(this);
        mSpinners[PLS_IDX].setAdapter(new RangeAdapter(this, pls_vals, true, SPINNER_ITEM_RESOURCE_ID, SPINNER_ITEM_TEXT_VIEW_ID));

        mNoteText = (EditText) findViewById(R.id.note);

        mSpinnersReference = new WeakReference<Spinner[]>(mSpinners);

        mMAQH = new MyAsyncQueryHandler(this.getContentResolver());
        mMAQH.startQuery(BPRECORDS_TOKEN, this, mUri, PROJECTION, null, null, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Modify our overall title depending on the mode we are running in.
        if (mState == STATE_EDIT) {
            setTitle(getText(R.string.title_edit));
        } else if (mState == STATE_INSERT) {
            setTitle(getText(R.string.title_create));
        }
        if(mCursor != null && mCursor.moveToFirst()) {
            int systolic = mCursor.getInt(COLUMN_SYSTOLIC_INDEX);
            int diastolic = mCursor.getInt(COLUMN_DIASTOLIC_INDEX);
            int pulse = mCursor.getInt(COLUMN_PULSE_INDEX);
            long datetime = mCursor.getLong(COLUMN_CREATED_AT_INDEX);
            String note = mCursor.getString(COLUMN_NOTE_INDEX);

            BPTrackerFree.setSpinner(mSpinners[SYS_IDX], systolic);
            BPTrackerFree.setSpinner(mSpinners[DIA_IDX], diastolic);
            BPTrackerFree.setSpinner(mSpinners[PLS_IDX], pulse);
            mNoteText.setText(note);
            mCalendar.setTimeInMillis(datetime);
            updateDateTimeDisplay();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putAll(mOriginalValues);
        outState.putString(BPTrackerFree.MURI, mUri.toString());
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Try to cancel any async queries we may have started that have not completed
        if(mMAQH != null)
            mMAQH.cancelOperation(BPRECORDS_TOKEN);

        // The user is going somewhere else, so make sure their current
        // changes are safely saved away in the provider. We don't need
        // to do this if only editing.
        if (mCursor != null) {
            int systolic = (Integer) mSpinners[SYS_IDX].getSelectedItem();
            int diastolic = (Integer) mSpinners[DIA_IDX].getSelectedItem();
            int pulse = (Integer) mSpinners[PLS_IDX].getSelectedItem();
            long created = (Long) mCalendar.getTimeInMillis();
            String note = (String) mNoteText.getText().toString();

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
        if(mCursor != null) {
            stopManagingCursor(mCursor);
            mCursor.close();
            mCursor = null;
        }
        super.onDestroy();
    }

    @Override
    protected void finalize() {
        if(mCursor != null) {
            stopManagingCursor(mCursor);
            mCursor.close();
            mCursor = null;
        }
    }

    protected class MyAsyncQueryHandler extends AsyncQueryHandler {

        public MyAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            mCursor = cursor;
            if(mCursor != null) {
                startManagingCursor(mCursor);
                if (mCursor.moveToFirst()) {
                    int systolic = mCursor.getInt(COLUMN_SYSTOLIC_INDEX);
                    int diastolic = mCursor.getInt(COLUMN_DIASTOLIC_INDEX);
                    int pulse = mCursor.getInt(COLUMN_PULSE_INDEX);
                    long datetime = mCursor.getLong(COLUMN_CREATED_AT_INDEX);
                    long mod_datetime = mCursor.getLong(COLUMN_MODIFIED_AT_INDEX);
                    String note = mCursor.getString(COLUMN_NOTE_INDEX);

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
                    Spinner[] spinners = mSpinnersReference.get();
                    EditText noteView = mNoteViewReference.get();
                    Button dateButton = mDateButtonReference.get();
                    Button timeButton = mTimeButtonReference.get();
                    Calendar calendar = mCalendarReference.get();
                    if(spinners != null) {
                        BPTrackerFree.setSpinner(spinners[SYS_IDX], systolic);
                        BPTrackerFree.setSpinner(spinners[DIA_IDX], diastolic);
                        BPTrackerFree.setSpinner(spinners[PLS_IDX], pulse);
                    }
                    if(calendar != null) {
                        calendar.setTimeInMillis(datetime);
                    }
                    Date date = calendar.getTime();
                    if(dateButton != null) {
                        dateButton.setText(BPTrackerFree.getDateString(date, DateFormat.MEDIUM));
                    }
                    if(timeButton != null) {
                        timeButton.setText(BPTrackerFree.getTimeString(date, DateFormat.SHORT));
                    }
                    if(noteView != null) {
                        noteView.setText(note);
                    }

                }
            }
        }

    }

    protected ContentValues getOriginalContentValues() {
        ContentValues cv = super.getOriginalContentValues();
        if(mOriginalValues != null) {
            cv.put(BPRecord.SYSTOLIC, mOriginalValues.getInt(BPRecord.SYSTOLIC));
            cv.put(BPRecord.DIASTOLIC, mOriginalValues.getInt(BPRecord.DIASTOLIC));
            cv.put(BPRecord.PULSE, mOriginalValues.getInt(BPRecord.PULSE));
        }
        return cv;
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos,
            long id) {
        Spinner sp = (Spinner) parent;
        if (sp.equals(mSpinners[SYS_IDX])) {
            int systolic = (Integer) ((RangeAdapter) mSpinners[SYS_IDX].getAdapter()).getItem(pos);
            int diastolic = (Integer) mSpinners[DIA_IDX].getSelectedItem();
            if ((systolic - diastolic) < BPTrackerFree.MIN_RANGE) {
                BPTrackerFree.setSpinner(mSpinners[DIA_IDX], systolic - BPTrackerFree.MIN_RANGE);
            }
        } else if (sp.equals(mSpinners[DIA_IDX])) {
            int systolic = (Integer) mSpinners[SYS_IDX].getSelectedItem();
            int diastolic = (Integer) ((RangeAdapter) mSpinners[DIA_IDX].getAdapter()).getItem(pos);
            if ((systolic - diastolic) < BPTrackerFree.MIN_RANGE) {
                BPTrackerFree.setSpinner(mSpinners[SYS_IDX], diastolic + BPTrackerFree.MIN_RANGE);
            }
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        Spinner sp = (Spinner) parent;
        if (sp.equals(mSpinners[SYS_IDX])) {
            BPTrackerFree.setSpinner(mSpinners[SYS_IDX], mOriginalValues.getInt(BPRecord.SYSTOLIC));
        } else if (sp.equals(mSpinners[DIA_IDX])) {
            BPTrackerFree.setSpinner(mSpinners[DIA_IDX], mOriginalValues.getInt(BPRecord.DIASTOLIC));
        } else if (sp.equals(mSpinners[PLS_IDX])) {
            BPTrackerFree.setSpinner(mSpinners[PLS_IDX], mOriginalValues.getInt(BPRecord.PULSE));
        }
    }

}
