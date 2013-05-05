package com.eyebrowssoftware.bptrackerfree.fragments;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.Assert;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
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
import android.widget.Button;
import android.widget.EditText;

import com.eyebrowssoftware.bptrackerfree.BPRecords;
import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;
import com.eyebrowssoftware.bptrackerfree.BPTrackerFree;
import com.eyebrowssoftware.bptrackerfree.R;
import com.eyebrowssoftware.bptrackerfree.fragments.AlertDialogFragment.AlertDialogButtonListener;

/**
 * @author brionemde
 *
 */
public class BPRecordEditorFragment extends DialogFragment implements LoaderCallbacks<Cursor>, AlertDialogButtonListener {
    static final String TAG = "BPRecordEditorFragment";

    public static final String URI_STRING_KEY = "uri_string_key";

    public static BPRecordEditorFragment newInstance(Uri existingRecordUri) {
        BPRecordEditorFragment fragment = new BPRecordEditorFragment();
        Bundle args = new Bundle();
        if (existingRecordUri != null) {
            args.putString(URI_STRING_KEY, existingRecordUri.toString());
        }
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * All Editor Plugins must comply
     *
     */
    public interface EditorPlugin {
        public void updateCurrentValues(ContentValues currentValues);
    }

    /**
     * All hosting activities must comply
     */
    public interface BPEditorListener {
        public void finishing();
    }

    private static final String[] AVERAGE_PROJECTION = {
        BPRecord.AVERAGE_SYSTOLIC,
        BPRecord.AVERAGE_DIASTOLIC,
        BPRecord.AVERAGE_PULSE };

    // The different distinct states the activity can be run in.
    public static final int STATE_EDIT = 0;
    public static final int STATE_INSERT = 1;

    // Member Variables
    private int mState;

    private Uri mUri;

    private Button mDateButton;
    private Button mTimeButton;
    private Button mDoneButton;
    private Button mRevertButton;

    private EditText mNoteText;

    private Calendar mCalendar;

    private Bundle mOriginalValues = null;

    private ContentValues mCurrentValues = new ContentValues();

    private SharedPreferences mSharedPreferences;

    private static final int EDITOR_LOADER_ID = 80331;

    private EditorPlugin mEditorPlugin;

    private BPEditorListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.bp_record_editor_fragment, container, false);

        mCalendar = new GregorianCalendar();
        mDateButton = (Button) v.findViewById(R.id.date_button);
        mDateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                DatePickerFragment dateFrag = DatePickerFragment.newInstance(
                        mCalendar.get(Calendar.DAY_OF_MONTH),
                        mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.YEAR));
                dateFrag.show(BPRecordEditorFragment.this.getActivity().getSupportFragmentManager(), "date");
            }
        });

        mTimeButton = (Button) v.findViewById(R.id.time_button);
        mTimeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                TimePickerFragment timeFrag = TimePickerFragment.newInstance(
                        mCalendar.get(Calendar.HOUR),
                        mCalendar.get(Calendar.MINUTE));
                timeFrag.show(BPRecordEditorFragment.this.getActivity().getSupportFragmentManager(), "time");
            }
        });

        mDoneButton = (Button) v.findViewById(R.id.done_button);
        mDoneButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mListener.finishing();
            }
        });

        mRevertButton = (Button) v.findViewById(R.id.revert_button);
        if(mState == STATE_INSERT) {
            mRevertButton.setText(R.string.menu_discard);
        }
        mRevertButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                cancelRecord();

            }
        });

        mNoteText = (EditText) v.findViewById(R.id.note);
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (BPEditorListener) getActivity();
    }


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.setHasOptionsMenu(true);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

        if (icicle != null) {
            mOriginalValues = new Bundle(icicle);
        }

        Bundle args = this.getArguments();
        if (args.containsKey(URI_STRING_KEY)) {
            mUri = Uri.parse(args.getString(URI_STRING_KEY));
            mState = STATE_EDIT;
        } else {
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
                mUri = this.getActivity().getContentResolver().insert(BPRecords.CONTENT_URI, cv);
            }
        }
        this.getActivity().getSupportLoaderManager().initLoader(EDITOR_LOADER_ID, null, this);

        boolean isText = mSharedPreferences.getBoolean(BPTrackerFree.IS_TEXT_EDITOR_KEY, false);
        loadEditorFragment(isText);
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
    }

    @Override
    public void onPause() {
        super.onPause();
        saveUIState();
    }

    private static final String TEXT_KEY = "text";
    private static final String SPINNER_KEY = "spinner";

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putAll(mOriginalValues);
        outState.putString(BPTrackerFree.MURI, mUri.toString());
    }

    private void loadEditorFragment(boolean isText) {

        FragmentManager fm = this.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        String key = isText ? TEXT_KEY : SPINNER_KEY;

        Fragment current = fm.findFragmentByTag(key);
        if (current != null) {
            ft.remove(current);
        }
        Fragment fragment = (isText) ? EditorTextFragment.newInstance(mUri) : EditorSpinnerFragment.newInstance(mUri);
        this.mEditorPlugin = (EditorPlugin) fragment;
        ft.replace(R.id.editor, fragment, key);
        ft.commit();
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onNegativeButtonClicked() {
        // Nada
    }

    @Override
    public void onPositiveButtonClicked() {
        getActivity().getContentResolver().delete(mUri, null, null);
        getActivity().setResult(Activity.RESULT_OK);
        mListener.finishing();
    }

    // Lint is complaining, but according to the documentation, show() does a commit on the transaction
    // http://developer.android.com/reference/android/app/DialogFragment.html#show(android.app.FragmentTransaction,%20java.lang.String)
    @SuppressLint("CommitTransaction")
    void showDeleteConfirmationDialog() {
        final String DELETE = "delete";
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(DELETE);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(DELETE);

        // Create and show the dialog.
        DialogFragment newFragment = AlertDialogFragment.getNewInstance(
                R.string.label_delete_history, R.string.msg_delete, R.string.label_yes, R.string.label_no);
        newFragment.show(ft, DELETE);
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
        mListener.finishing();
    }

    private void saveUIState() {
        mCurrentValues.put(BPRecord.CREATED_DATE, mCalendar.getTimeInMillis());
        mCurrentValues.put(BPRecord.MODIFIED_DATE, System.currentTimeMillis());
        mCurrentValues.put(BPRecord.NOTE, mNoteText.getText().toString());
        if (mEditorPlugin != null) {
            mEditorPlugin.updateCurrentValues(mCurrentValues);
        }
        this.getActivity().getContentResolver().update(mUri, mCurrentValues, null, null);
    }

    public void setUIState() {
        mCalendar.setTimeInMillis(mCurrentValues.getAsLong(BPRecord.CREATED_DATE));
        mNoteText.setText(mCurrentValues.getAsString(BPRecord.NOTE));
        Date date = mCalendar.getTime();
        mDateButton.setText(BPTrackerFree.getDateString(date, DateFormat.MEDIUM));
        mTimeButton.setText(BPTrackerFree.getTimeString(date, DateFormat.SHORT));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        Assert.assertNotNull(mUri);
        return new CursorLoader(this.getActivity(), mUri, BPTrackerFree.PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Assert.assertNotNull(cursor);
        if (loader.getId() == EDITOR_LOADER_ID) {
            if (cursor.moveToFirst()) {
                mCurrentValues.put(BPRecord.SYSTOLIC, cursor.getInt(BPTrackerFree.COLUMN_SYSTOLIC_INDEX));
                mCurrentValues.put(BPRecord.PULSE, cursor.getInt(BPTrackerFree.COLUMN_PULSE_INDEX));
                mCurrentValues.put(BPRecord.DIASTOLIC, cursor.getInt(BPTrackerFree.COLUMN_DIASTOLIC_INDEX));
                mCurrentValues.put(BPRecord.CREATED_DATE, cursor.getLong(BPTrackerFree.COLUMN_CREATED_AT_INDEX));
                mCurrentValues.put(BPRecord.MODIFIED_DATE, cursor.getLong(BPTrackerFree.COLUMN_MODIFIED_AT_INDEX));
                mCurrentValues.put(BPRecord.NOTE, cursor.getString(BPTrackerFree.COLUMN_NOTE_INDEX));
                setUIState();
                // If we hadn't previously retrieved the original values, do so
                // now. This allows the user to revert their changes.
                if (mOriginalValues == null) {
                    setOriginalValuesBundle();
                }
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
}
