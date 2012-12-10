package com.eyebrowssoftware.bptrackerfree.activity;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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

import com.eyebrowssoftware.bptrackerfree.BPRecords;
import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;
import com.eyebrowssoftware.bptrackerfree.BPTrackerFree;
import com.eyebrowssoftware.bptrackerfree.R;

/**
 * @author brionemde
 *
 */
public abstract class BPRecordEditorBase extends FragmentActivity
    implements OnDateSetListener, OnTimeSetListener, LoaderManager.LoaderCallbacks<Cursor> {

    // Static constants

    protected static final String TAG = "BPRecordEditorBase";

    private static final String[] AVERAGE_PROJECTION = {
        BPRecord.AVERAGE_SYSTOLIC,
        BPRecord.AVERAGE_DIASTOLIC,
        BPRecord.AVERAGE_PULSE
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

    protected Button mDateButton;
    protected Button mTimeButton;
    protected EditText mNoteText;

    protected Calendar mCalendar;

    protected Bundle mOriginalValues = null;
    protected ContentValues mCurrentValues;

    protected Button mDoneButton;

    protected Button mCancelButton;

    protected static final int BPRECORDS_TOKEN = 0;

    protected SharedPreferences mSharedPreferences;

    private static final int EDITOR_LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.bp_record_editor);

        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (icicle != null) {
            mOriginalValues = new Bundle(icicle);
        }
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (Intent.ACTION_EDIT.equals(action)) {
            mState = STATE_EDIT;
            mUri = intent.getData();
        } else if (Intent.ACTION_INSERT.equals(action)) {
            mState = STATE_INSERT;
            if (icicle != null)
                mUri = Uri.parse(icicle.getString(BPTrackerFree.MURI));
            else {
                ContentValues cv = null;
                if (mSharedPreferences.getBoolean(BPTrackerFree.AVERAGE_VALUES_KEY, false)) {
                    cv = setAverageValues(mSharedPreferences);
                } else {
                    cv = setDefaultValues(mSharedPreferences);
                }
                cv.put(BPRecord.CREATED_DATE, GregorianCalendar.getInstance().getTimeInMillis());
                mUri = this.getContentResolver().insert(intent.getData(), cv);
            }
        } else {
            Log.e(TAG, "Unknown action, exiting");
            finish();
            return;
        }
        this.getSupportLoaderManager().initLoader(EDITOR_LOADER_ID, null, this);

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
        setUIState();
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

        // The user is going somewhere else, so make sure their current
        // changes are safely saved away in the provider.
        //
        // The leaf subclass is responsible for persisting the current values
        if(mCurrentValues != null) {
            mCurrentValues.put(BPRecord.CREATED_DATE, mCalendar.getTimeInMillis());
            mCurrentValues.put(BPRecord.MODIFIED_DATE, System.currentTimeMillis());
            mCurrentValues.put(BPRecord.NOTE, mNoteText.getText().toString());
        }
    }

    protected void updateFromCurrentValues() {
        getContentResolver().update(mUri, mCurrentValues, null, null);
    }

    private ContentValues setAverageValues(SharedPreferences prefs) {
        Cursor c = this.getContentResolver().query(BPRecords.CONTENT_URI, AVERAGE_PROJECTION, null, null, null);
        ContentValues cv = new ContentValues();
        if (c != null && c.moveToFirst() && !c.isNull(0) && !c.isNull(1) && !c.isNull(2)) {
            cv = setContentValues((int) c.getFloat(0), (int) c.getFloat(1), (int) c.getFloat(2));
        } else {
            cv = setDefaultValues(mSharedPreferences);
        }
        c.close();
        return cv;
    }

    private ContentValues setDefaultValues(SharedPreferences prefs) {
        return setContentValues(
            Integer.valueOf(prefs.getString(BPTrackerFree.DEFAULT_SYSTOLIC_KEY, BPTrackerFree.SYSTOLIC_DEFAULT_STRING)),
            Integer.valueOf(prefs.getString(BPTrackerFree.DEFAULT_DIASTOLIC_KEY, BPTrackerFree.DIASTOLIC_DEFAULT_STRING)),
            Integer.valueOf(prefs.getString(BPTrackerFree.DEFAULT_PULSE_KEY, BPTrackerFree.PULSE_DEFAULT_STRING)));
    }

    private ContentValues setContentValues(int systolic, int diastolic, int pulse) {
        ContentValues cv = new ContentValues();
        cv.put(BPRecord.SYSTOLIC, systolic);
        cv.put(BPRecord.DIASTOLIC, diastolic);
        cv.put(BPRecord.PULSE, pulse);
        return cv;
    }

    /**
    * Update the date and time
    */
    private void updateDateTimeDisplay() {
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

    /**
    * Take care of canceling work on a BPRecord. Deletes the record if we had created
    * it, otherwise reverts to the original record data.
    */
    protected final void cancelRecord() {
        if (mState == STATE_EDIT) {
            // Restore the original information we loaded at first.
            ContentValues cv = new ContentValues();
            cv.put(BPRecord.SYSTOLIC, mOriginalValues.getInt(BPRecord.SYSTOLIC));
            cv.put(BPRecord.DIASTOLIC, mOriginalValues.getInt(BPRecord.DIASTOLIC));
            cv.put(BPRecord.PULSE, mOriginalValues.getInt(BPRecord.PULSE));
            cv.put(BPRecord.NOTE, mOriginalValues.getString(BPRecord.NOTE));
            cv.put(BPRecord.CREATED_DATE, mOriginalValues.getLong(BPRecord.CREATED_DATE));
            cv.put(BPRecord.MODIFIED_DATE, mOriginalValues.getLong(BPRecord.MODIFIED_DATE));
            getContentResolver().update(mUri, cv, null, null);
        } else if (mState == STATE_INSERT) {
            // We inserted an empty record, make sure to delete it
            getContentResolver().delete(mUri, null, null);
        }
        setResult(RESULT_CANCELED);
        finish();
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
                        getContentResolver().delete(mUri, null, null);
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

    protected Bundle getOriginalValues() {
        return mOriginalValues;
    }

    protected ContentValues getCurrentValues() {
        return mCurrentValues;
    }

    protected void setUIState() {
        if (mCurrentValues != null) {
            mCalendar.setTimeInMillis(mCurrentValues.getAsLong(BPRecord.CREATED_DATE));
            mNoteText.setText(mCurrentValues.getAsString(BPRecord.NOTE));
            updateDateTimeDisplay();
        }
    }

    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        return new CursorLoader(this, mUri, BPTrackerFree.PROJECTION, null, null, null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            if (mCurrentValues == null) {
                mCurrentValues = new ContentValues();
            }
            mCurrentValues.put(BPRecord.SYSTOLIC, cursor.getInt(COLUMN_SYSTOLIC_INDEX));
            mCurrentValues.put(BPRecord.PULSE, cursor.getInt(COLUMN_PULSE_INDEX));
            mCurrentValues.put(BPRecord.DIASTOLIC, cursor.getInt(COLUMN_DIASTOLIC_INDEX));
            mCurrentValues.put(BPRecord.CREATED_DATE, cursor.getLong(COLUMN_CREATED_AT_INDEX));
            mCurrentValues.put(BPRecord.MODIFIED_DATE, cursor.getLong(COLUMN_MODIFIED_AT_INDEX));
            mCurrentValues.put(BPRecord.NOTE, cursor.getString(COLUMN_NOTE_INDEX));
            setUIState();

            // If we hadn't previously retrieved the original values, do so
            // now. This allows the user to revert their changes.
            if(mOriginalValues == null) {
                setOriginalValuesBundle();
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        // Not doing anything right now

    }

    private void setOriginalValuesBundle() {
        mOriginalValues = new Bundle();
        mOriginalValues.putInt(BPRecord.SYSTOLIC, mCurrentValues.getAsInteger(BPRecord.SYSTOLIC));
        mOriginalValues.putInt(BPRecord.DIASTOLIC, mCurrentValues.getAsInteger(BPRecord.DIASTOLIC));
        mOriginalValues.putInt(BPRecord.PULSE, mCurrentValues.getAsInteger(BPRecord.PULSE));
        mOriginalValues.putString(BPRecord.NOTE, mCurrentValues.getAsString(BPRecord.NOTE));
        mOriginalValues.putLong(BPRecord.CREATED_DATE, mCurrentValues.getAsLong(BPRecord.CREATED_DATE));
        mOriginalValues.putLong(BPRecord.MODIFIED_DATE, mCurrentValues.getAsLong(BPRecord.MODIFIED_DATE));
    }
}
