package com.eyebrowssoftware.bptrackerfree;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;

/**
 * @author brionemde
 *
 */
public abstract class BPRecordEditorBase extends Activity  implements OnDateSetListener, OnTimeSetListener {

    // Static constants

    protected static final String TAG = "BPRecordEditorBase";

    protected static final String[] PROJECTION = {
        BPRecord._ID,
        BPRecord.SYSTOLIC,
        BPRecord.DIASTOLIC,
        BPRecord.PULSE,
        BPRecord.CREATED_DATE,
        BPRecord.MODIFIED_DATE,
        BPRecord.NOTE
    };

    // BP Record Indices
    // protected static final int COLUMN_ID_INDEX = 0;
    protected static final int COLUMN_SYSTOLIC_INDEX = 1;
    protected static final int COLUMN_DIASTOLIC_INDEX = 2;
    protected static final int COLUMN_PULSE_INDEX = 3;
    protected static final int COLUMN_CREATED_AT_INDEX = 4;
    protected static final int COLUMN_MODIFIED_AT_INDEX = 5;
    protected static final int COLUMN_NOTE_INDEX = 6;

    // The different distinct states the activity can be run in.
    protected static final int STATE_EDIT = 0;
    protected static final int STATE_INSERT = 1;

    protected static final int DATE_DIALOG_ID = 0;
    protected static final int TIME_DIALOG_ID = 1;
    protected static final int DELETE_DIALOG_ID = 2;

    protected static final int SYS_IDX = 0;
    protected static final int DIA_IDX = 1;
    protected static final int PLS_IDX = 2;
    protected static final int SPINNER_ARRAY_SIZE  = PLS_IDX + 1;

    protected static final int SPINNER_ITEM_RESOURCE_ID = R.layout.bp_spinner_item;
    protected static final int SPINNER_ITEM_TEXT_VIEW_ID = android.R.id.text1;

    // Member Variables
    protected int mState;

    protected Uri mUri;

    protected Cursor mCursor;

    protected Button mDateButton;
    protected Button mTimeButton;
    protected EditText mNoteText;

    protected Calendar mCalendar;

    protected Bundle mOriginalValues = null;
    protected Bundle mCurrentValues = null;

    protected Button mDoneButton;

    protected Button mCancelButton;

    protected static final int BPRECORDS_TOKEN = 0;

    protected MyAsyncQueryHandler mMAQH;

    protected WeakReference<EditText> mNoteViewReference;
    protected WeakReference<Calendar> mCalendarReference;
    protected WeakReference<Button> mDateButtonReference;
    protected WeakReference<Button> mTimeButtonReference;


    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.bp_record_editor);

        final Intent intent = getIntent();
        final String action = intent.getAction();

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
        mMAQH = new MyAsyncQueryHandler(this.getContentResolver(), this);
        mMAQH.startQuery(BPRECORDS_TOKEN, this, mUri, PROJECTION, null, null, null);

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

        mNoteText = (EditText) findViewById(R.id.note);

        mNoteViewReference = new WeakReference<EditText>(mNoteText);
        mCalendarReference = new WeakReference<Calendar>(mCalendar);
        mDateButtonReference = new WeakReference<Button>(mDateButton);
        mTimeButtonReference = new WeakReference<Button>(mTimeButton);

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
        if(mCurrentValues != null) {
            long datetime = mCurrentValues.getLong(BPRecord.CREATED_DATE);
            String note = mCurrentValues.getString(BPRecord.NOTE);

            mNoteText.setText(note);
            mCalendar.setTimeInMillis(datetime);
            updateDateTimeDisplay();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
        if (mCurrentValues != null) {
            long created = (Long) mCalendar.getTimeInMillis();
            String note = (String) mNoteText.getText().toString();

            mCurrentValues.putLong(BPRecord.CREATED_DATE, created);
            mCurrentValues.putLong(BPRecord.MODIFIED_DATE, System.currentTimeMillis());
            mCurrentValues.putString(BPRecord.NOTE, note);
        }
    }

    protected void updateFromCurrentValues() {
        ContentValues values = new ContentValues();
        values.put(BPRecord.SYSTOLIC, mCurrentValues.getInt(BPRecord.SYSTOLIC));
        values.put(BPRecord.DIASTOLIC, mCurrentValues.getInt(BPRecord.DIASTOLIC));
        values.put(BPRecord.PULSE, mCurrentValues.getInt(BPRecord.PULSE));
        values.put(BPRecord.CREATED_DATE, mCurrentValues.getLong(BPRecord.CREATED_DATE));
        values.put(BPRecord.MODIFIED_DATE, mCurrentValues.getLong(BPRecord.MODIFIED_DATE));
        values.put(BPRecord.NOTE, mCurrentValues.getString(BPRecord.NOTE));
        getContentResolver().update(mUri, values, null, null);
    }

    @Override
    protected void onDestroy() {
        closeCursors();
        super.onDestroy();
    }

    @Override
    protected void finalize() throws Throwable {
        closeCursors();
        super.finalize();
    }


    /**
    * Update the date and time
    */
    public void updateDateTimeDisplay() {
        Date date = mCalendar.getTime();
        mDateButton.setText(BPTrackerFree.getDateString(date, DateFormat.MEDIUM));
        mTimeButton.setText(BPTrackerFree.getTimeString(date, DateFormat.SHORT));
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
        case R.id.menu_settings:
            startActivity(new Intent(this, BPPreferenceActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void closeRecordCursor() {
        if(mCursor != null) {
            this.stopManagingCursor(mCursor);
            mCursor.close();
            mCursor = null;
        }
    }

    protected void closeCursors() {
        closeRecordCursor();
    }

    protected ContentValues getOriginalContentValues() {
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
    protected final void cancelRecord() {
        if (mCursor != null) {
            if (mState == STATE_EDIT) {
                // Restore the original information we loaded at first.
                mCursor.close();
                // we will end up in onPause() and we don't want it to do anything
                mCursor = null;
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
    protected final void deleteRecord() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
            getContentResolver().delete(mUri, null, null);
        }
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
        long now = new GregorianCalendar().getTimeInMillis();
        if (mCalendar.getTimeInMillis() > now) {
            Toast.makeText(BPRecordEditorBase.this, getString(R.string.msg_future_date), Toast.LENGTH_LONG).show();
            mCalendar.setTimeInMillis(now);
        }
        updateDateTimeDisplay();
    }

    public void onTimeSet(TimePicker view, int hour, int minute) {
        mCalendar.set(Calendar.HOUR_OF_DAY, hour);
        mCalendar.set(Calendar.MINUTE, minute);
        long now = new GregorianCalendar().getTimeInMillis();
        if (mCalendar.getTimeInMillis() > now) {
            Toast.makeText(BPRecordEditorBase.this, getString(R.string.msg_future_date), Toast.LENGTH_LONG).show();
            mCalendar.setTimeInMillis(now);
        }
        updateDateTimeDisplay();
    }

    protected void setMyCursor(Cursor cursor) {
        mCursor = cursor;
    }

    protected Cursor getCursor() {
        return mCursor;
    }

    protected Bundle getOriginalValues() {
        return mOriginalValues;
    }

    protected void setOriginalValues(Bundle originalValues) {
        mOriginalValues = originalValues;
    }

    protected Bundle getCurrentValues() {
        return mCurrentValues;
    }

    protected void setCurrentValues(Bundle currentValues) {
        mCurrentValues = currentValues;
        EditText noteView = mNoteViewReference.get();
        Button dateButton = mDateButtonReference.get();
        Button timeButton = mTimeButtonReference.get();
        Calendar calendar = mCalendarReference.get();

        if(calendar != null) {
            calendar.setTimeInMillis(currentValues.getLong(BPRecord.CREATED_DATE));
        }
        Date date = calendar.getTime();
        if(dateButton != null) {
            dateButton.setText(BPTrackerFree.getDateString(date, DateFormat.MEDIUM));
        }
        if(timeButton != null) {
            timeButton.setText(BPTrackerFree.getTimeString(date, DateFormat.SHORT));
        }
        if(noteView != null) {
            noteView.setText(currentValues.getString(BPRecord.NOTE));
        }
    }

    protected static class MyAsyncQueryHandler extends AsyncQueryHandler {
        WeakReference<BPRecordEditorBase> mActivityReference;

        public MyAsyncQueryHandler(ContentResolver cr, BPRecordEditorBase parent) {
            super(cr);
            mActivityReference = new WeakReference<BPRecordEditorBase>(parent);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            BPRecordEditorBase parent = mActivityReference.get();
            if (parent != null) {
                parent.setMyCursor(cursor);
                if(cursor != null) {
                    parent.startManagingCursor(cursor);
                    if (cursor.moveToFirst()) {
                        Bundle currentValues = parent.getCurrentValues();
                        if (currentValues == null) {
                            currentValues = new Bundle();
                        }
                        currentValues.putInt(BPRecord.SYSTOLIC, cursor.getInt(COLUMN_SYSTOLIC_INDEX));
                        currentValues.putInt(BPRecord.PULSE, cursor.getInt(COLUMN_PULSE_INDEX));
                        currentValues.putInt(BPRecord.DIASTOLIC, cursor.getInt(COLUMN_DIASTOLIC_INDEX));
                        currentValues.putLong(BPRecord.CREATED_DATE, cursor.getLong(COLUMN_CREATED_AT_INDEX));
                        currentValues.putLong(BPRecord.MODIFIED_DATE, cursor.getLong(COLUMN_MODIFIED_AT_INDEX));
                        currentValues.putString(BPRecord.NOTE, cursor.getString(COLUMN_NOTE_INDEX));
                        parent.setCurrentValues(currentValues);

                        // If we hadn't previously retrieved the original text, do so
                        // now. This allows the user to revert their changes.
                        Bundle originalValues = parent.getOriginalValues();
                        if(originalValues == null) {
                            originalValues = new Bundle(currentValues);
                            parent.setOriginalValues(originalValues);
                        }
                    }
                }
            }
        }
    }
}
