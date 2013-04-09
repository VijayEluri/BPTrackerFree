package com.eyebrowssoftware.bptrackerfree.fragments;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.eyebrowssoftware.bptrackerfree.BPRecords;
import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;
import com.eyebrowssoftware.bptrackerfree.BPTrackerFree;
import com.eyebrowssoftware.bptrackerfree.R;
import com.eyebrowssoftware.bptrackerfree.activity.BPPreferenceActivity;
import com.eyebrowssoftware.bptrackerfree.fragments.AlertDialogFragment.AlertDialogListener;
import com.eyebrowssoftware.bptrackerfree.fragments.DatePickerFragment.DatePickerListener;
import com.eyebrowssoftware.bptrackerfree.fragments.TimePickerFragment.TimePickerListener;

/**
 * @author brionemde
 *
 */
public class BPRecordEditorFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        AlertDialogListener, TimePickerListener, DatePickerListener {
    static final String TAG = "BPRecordEditorBase";

    /**
     * All Editor Plugins must comply
     *
     */
    public interface EditorPlugin {
        public void setCurrentValues(ContentValues values);
        public ContentValues getCurrentValues();
    }

    private static final String[] AVERAGE_PROJECTION = { BPRecord.AVERAGE_SYSTOLIC, BPRecord.AVERAGE_DIASTOLIC,
        BPRecord.AVERAGE_PULSE };

    // The different distinct states the activity can be run in.
    public static final int STATE_EDIT = 0;
    public static final int STATE_INSERT = 1;

    // Member Variables
    private int mState;

    private Uri mUri;

    private Button mDateButton;
    private Button mTimeButton;
    private EditText mNoteText;
    private View mProgressContainer;
    private View mContentContainer;
    private boolean mContentShown = true;

    private Calendar mCalendar;

    private Bundle mOriginalValues = null;

    private ContentValues mCurrentValues;

    private Button mDoneButton;

    private Button mCancelButton;

    private SharedPreferences mSharedPreferences;

    private static final int EDITOR_LOADER_ID = 1;

    private EditorPlugin mEditorPlugin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.bp_record_editor_fragment, container, false);

        mProgressContainer = v.findViewById(R.id.progress_container);
        mContentContainer = v.findViewById(R.id.content_container);

        mCalendar = new GregorianCalendar();
        mDateButton = (Button) v.findViewById(R.id.date_button);
        mDateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                DatePickerFragment dateFrag = new DatePickerFragment();
                Bundle b = new Bundle();
                b.putInt(DatePickerFragment.YEAR_KEY, mCalendar.get(Calendar.YEAR));
                b.putInt(DatePickerFragment.MONTH_KEY, mCalendar.get(Calendar.MONTH));
                b.putInt(DatePickerFragment.DAY_KEY, mCalendar.get(Calendar.DAY_OF_MONTH));
                dateFrag.setArguments(b);
                dateFrag.show(BPRecordEditorFragment.this.getActivity().getSupportFragmentManager(), "date");
            }
        });

        mTimeButton = (Button) v.findViewById(R.id.time_button);
        mTimeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                TimePickerFragment timeFrag = new TimePickerFragment();
                Bundle b = new Bundle();
                b.putInt(TimePickerFragment.HOUR_KEY, mCalendar.get(Calendar.HOUR));
                b.putInt(TimePickerFragment.MINUTE_KEY, mCalendar.get(Calendar.MINUTE));
                timeFrag.setArguments(b);
                timeFrag.show(BPRecordEditorFragment.this.getActivity().getSupportFragmentManager(), "time");
            }
        });

        mDoneButton = (Button) v.findViewById(R.id.done_button);
        mDoneButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                BPRecordEditorFragment.this.getActivity().finish();
            }
        });

        mCancelButton = (Button) v.findViewById(R.id.revert_button);
        mCancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                cancelRecord();
            }
        });

        mNoteText = (EditText) v.findViewById(R.id.note);
        return v;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle icicle) {
        super.onActivityCreated(icicle);
        final Intent intent = this.getActivity().getIntent();
        final String action = intent.getAction();

        if (icicle != null) {
            mOriginalValues = new Bundle(icicle);
        }
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

        boolean isText = mSharedPreferences.getBoolean(BPTrackerFree.IS_TEXT_EDITOR_KEY, false);
        Log.d(TAG, "onCreate: isText: " + (isText ? "true" : "false"));
        if (Intent.ACTION_EDIT.equals(action)) {
            mState = STATE_EDIT;
            mUri = intent.getData();
        }
        else if (Intent.ACTION_INSERT.equals(action)) {
            mState = STATE_INSERT;
            if (icicle != null)
                mUri = Uri.parse(icicle.getString(BPTrackerFree.MURI));
            else {
                ContentValues cv = null;
                if (mSharedPreferences.getBoolean(BPTrackerFree.AVERAGE_VALUES_KEY, false)) {
                    cv = setAverageValues(mSharedPreferences);
                }
                else {
                    cv = setDefaultValues(mSharedPreferences);
                }
                cv.put(BPRecord.CREATED_DATE, GregorianCalendar.getInstance().getTimeInMillis());
                mUri = this.getActivity().getContentResolver().insert(intent.getData(), cv);
            }
        }
        else {
            Log.e(TAG, "Unknown action, exiting");
            this.getActivity().finish();
            return;
        }
        if (mState == STATE_INSERT) {
            mCancelButton.setText(R.string.menu_discard);
        }
        this.setShown(false, true);
        this.getActivity().getSupportLoaderManager().initLoader(EDITOR_LOADER_ID, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Modify our overall title depending on the mode we are running in.
        if (mState == STATE_EDIT) {
            this.getActivity().setTitle(getText(R.string.title_edit));
        }
        else if (mState == STATE_INSERT) {
            this.getActivity().setTitle(getText(R.string.title_create));
        }
        boolean isText = mSharedPreferences.getBoolean(BPTrackerFree.IS_TEXT_EDITOR_KEY, false);
        Log.d(TAG, "onResume: isText: " + (isText ? "true" : "false"));
        loadEditorFragment(isText);
        setUIState();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putAll(mOriginalValues);
        outState.putString(BPTrackerFree.MURI, mUri.toString());
    }

    @Override
    public void onPause() {
        super.onPause();

        // The user is going somewhere else, so make sure their current
        // changes are safely saved away in the provider.
        //
        // The leaf subclass is responsible for persisting the current values
        if (mCurrentValues != null && mEditorPlugin != null) {
            mCurrentValues.put(BPRecord.CREATED_DATE, mCalendar.getTimeInMillis());
            mCurrentValues.put(BPRecord.MODIFIED_DATE, System.currentTimeMillis());
            mCurrentValues.put(BPRecord.NOTE, mNoteText.getText().toString());
            ContentValues pluginValues = mEditorPlugin.getCurrentValues();
            mCurrentValues.put(BPRecord.SYSTOLIC, pluginValues.getAsInteger(BPRecord.SYSTOLIC));
            mCurrentValues.put(BPRecord.DIASTOLIC, pluginValues.getAsInteger(BPRecord.DIASTOLIC));
            mCurrentValues.put(BPRecord.PULSE, pluginValues.getAsInteger(BPRecord.PULSE));
            this.updateFromCurrentValues();
        } else {
            // This means the query never returned before we're pausing
            // Some might think this cause for celebration; methinks it means
            // things are happening a might too quickly.
            if (mCurrentValues == null) {
                Log.e(TAG, "onPause() called with no mCurrentValues returned from query");
            }
            if (mEditorPlugin == null) {
                Log.e(TAG, "onPause() called with no editor plugin present");
            }
        }
    }

    private void loadEditorFragment(boolean isText) {

        FragmentManager fm = this.getFragmentManager();
        String key = isText ? "text" : "spinner";
        Fragment current = fm.findFragmentByTag(key);

        if (current != null) {
            Log.i(TAG, "There is a fragment of tag: " + key);
            this.mEditorPlugin = null;
        } else {
            Fragment fragment = (isText) ? new EditorTextFragment()
                    : new EditorSpinnerFragment();
            this.mEditorPlugin = (EditorPlugin) fragment;
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.editor, fragment, key);
            ft.commit();
        }
    }

    public void updateFromCurrentValues() {
        this.getActivity().getContentResolver().update(mUri, mCurrentValues, null, null);
    }

    private ContentValues setAverageValues(SharedPreferences prefs) {
        Cursor c = this.getActivity().getContentResolver()
            .query(BPRecords.CONTENT_URI, AVERAGE_PROJECTION, null, null, null);
        ContentValues cv = new ContentValues();
        if (c != null && c.moveToFirst() && !c.isNull(0) && !c.isNull(1) && !c.isNull(2)) {
            cv = setContentValues((int) c.getFloat(0), (int) c.getFloat(1), (int) c.getFloat(2));
        }
        else {
            cv = setDefaultValues(mSharedPreferences);
        }
        c.close();
        return cv;
    }

    private ContentValues setDefaultValues(SharedPreferences prefs) {
        return setContentValues(Integer.valueOf(prefs.getString(
            BPTrackerFree.DEFAULT_SYSTOLIC_KEY,
            BPTrackerFree.SYSTOLIC_DEFAULT_STRING)), Integer.valueOf(prefs.getString(
            BPTrackerFree.DEFAULT_DIASTOLIC_KEY,
            BPTrackerFree.DIASTOLIC_DEFAULT_STRING)), Integer.valueOf(prefs.getString(
            BPTrackerFree.DEFAULT_PULSE_KEY,
            BPTrackerFree.PULSE_DEFAULT_STRING)));
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.bp_record_editor_options_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // Build the menus that are shown when editing.
        if (mState == STATE_EDIT) {
            menu.setGroupVisible(R.id.edit_menu_group, true);
            menu.setGroupVisible(R.id.create_menu_group, false);
        }
        else if (mState == STATE_INSERT) {
            menu.setGroupVisible(R.id.edit_menu_group, false);
            menu.setGroupVisible(R.id.create_menu_group, true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle all of the possible menu actions.
        switch (item.getItemId()) {
            case R.id.menu_delete:
                showDeleteConfirmationDialog();
                return true;
            case R.id.menu_discard:
                cancelRecord();
                return true;
            case R.id.menu_revert:
                cancelRecord();
                return true;
            case R.id.menu_done:
                this.getActivity().finish();
                return true;
            case R.id.menu_settings:
                this.startActivity(new Intent(this.getActivity(), BPPreferenceActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialogFragment diagFrag = AlertDialogFragment.getNewInstance(
            R.string.msg_delete,
            R.string.label_yes,
            R.string.label_no,
            this);
        diagFrag.show(this.getActivity().getSupportFragmentManager(), "delete");
    }

    @Override
    public void onNegativeButtonClicked(AlertDialogFragment dialog) {
        // nothing to do, dialog is cancelled already
    }

    @Override
    public void onPositiveButtonClicked(AlertDialogFragment dialog) {
        this.getActivity().getContentResolver().delete(mUri, null, null);
        this.getActivity().setResult(FragmentActivity.RESULT_OK);
        this.getActivity().finish();
    }

    /**
    * Take care of canceling work on a BPRecord. Deletes the record if we had created
    * it, otherwise reverts to the original record data.
    */
    private final void cancelRecord() {
        FragmentActivity activity = this.getActivity();
        if (mState == STATE_EDIT) {
            // Restore the original information we loaded at first.
            ContentValues cv = new ContentValues();
            cv.put(BPRecord.SYSTOLIC, mOriginalValues.getInt(BPRecord.SYSTOLIC));
            cv.put(BPRecord.DIASTOLIC, mOriginalValues.getInt(BPRecord.DIASTOLIC));
            cv.put(BPRecord.PULSE, mOriginalValues.getInt(BPRecord.PULSE));
            cv.put(BPRecord.NOTE, mOriginalValues.getString(BPRecord.NOTE));
            cv.put(BPRecord.CREATED_DATE, mOriginalValues.getLong(BPRecord.CREATED_DATE));
            cv.put(BPRecord.MODIFIED_DATE, mOriginalValues.getLong(BPRecord.MODIFIED_DATE));
            activity.getContentResolver().update(mUri, cv, null, null);
        }
        else if (mState == STATE_INSERT) {
            // We inserted an empty record, make sure to delete it
            activity.getContentResolver().delete(mUri, null, null);
        }
        activity.setResult(FragmentActivity.RESULT_CANCELED);
        activity.finish();
    }

    @Override
    public void setDate(int year, int month, int day) {
        mCalendar.set(year, month, day);
        long now = new GregorianCalendar().getTimeInMillis();
        if (mCalendar.getTimeInMillis() > now) {
            Toast.makeText(BPRecordEditorFragment.this.getActivity(), getString(R.string.msg_future_date), Toast.LENGTH_LONG).show();
            mCalendar.setTimeInMillis(now);
        }
        updateDateTimeDisplay();
    }

    @Override
    public void setTime(int hour, int minute) {
        mCalendar.set(Calendar.HOUR_OF_DAY, hour);
        mCalendar.set(Calendar.MINUTE, minute);
        long now = new GregorianCalendar().getTimeInMillis();
        if (mCalendar.getTimeInMillis() > now) {
            Toast.makeText(BPRecordEditorFragment.this.getActivity(), getString(R.string.msg_future_date), Toast.LENGTH_LONG).show();
            mCalendar.setTimeInMillis(now);
        }
        updateDateTimeDisplay();
    }

    public Bundle getOriginalValues() {
        return mOriginalValues;
    }

    public ContentValues getCurrentValues() {
        return mCurrentValues;
    }

    public void setUIState() {
        if (mCurrentValues != null) {
            if (mEditorPlugin != null) {
                mEditorPlugin.setCurrentValues(mCurrentValues);
            }
            mCalendar.setTimeInMillis(mCurrentValues.getAsLong(BPRecord.CREATED_DATE));
            mNoteText.setText(mCurrentValues.getAsString(BPRecord.NOTE));
            updateDateTimeDisplay();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        return new CursorLoader(this.getActivity(), mUri, BPTrackerFree.PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            if (mCurrentValues == null) {
                mCurrentValues = new ContentValues();
            }
            mCurrentValues.put(BPRecord.SYSTOLIC, cursor.getInt(BPTrackerFree.COLUMN_SYSTOLIC_INDEX));
            mCurrentValues.put(BPRecord.PULSE, cursor.getInt(BPTrackerFree.COLUMN_PULSE_INDEX));
            mCurrentValues.put(BPRecord.DIASTOLIC, cursor.getInt(BPTrackerFree.COLUMN_DIASTOLIC_INDEX));
            mCurrentValues.put(BPRecord.CREATED_DATE, cursor.getLong(BPTrackerFree.COLUMN_CREATED_AT_INDEX));
            mCurrentValues.put(BPRecord.MODIFIED_DATE, cursor.getLong(BPTrackerFree.COLUMN_MODIFIED_AT_INDEX));
            mCurrentValues.put(BPRecord.NOTE, cursor.getString(BPTrackerFree.COLUMN_NOTE_INDEX));
            setUIState();
            if (this.isResumed()) {
                this.setShown(true, true);
            }
            else {
                this.setShown(true, false);
            }

            // If we hadn't previously retrieved the original values, do so
            // now. This allows the user to revert their changes.
            if (mOriginalValues == null) {
                setOriginalValuesBundle();
            }
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

    /**
     * Control whether the content is being displayed. You can make it not displayed if you are waiting for the initial
     * data to show in it. During this time an indeterminant progress indicator will be shown instead.
     *
     * @param shown
     *            If true, the list view is shown; if false, the progress indicator. The initial value is true.
     * @param animate
     *            If true, an animation will be used to transition to the new state.
     */
    private void setShown(boolean shown, boolean animate) {
        if (mProgressContainer == null) {
            throw new IllegalStateException("Can't be used with a custom content view");
        }
        if (mContentShown == shown) {
            return;
        }
        mContentShown = shown;
        if (shown) {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
                mContentContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
            }
            else {
                mProgressContainer.clearAnimation();
                mContentContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.GONE);
            mContentContainer.setVisibility(View.VISIBLE);
        }
        else {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
                mContentContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
            }
            else {
                mProgressContainer.clearAnimation();
                mContentContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.VISIBLE);
            mContentContainer.setVisibility(View.GONE);
        }
    }
}
