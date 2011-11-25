package com.eyebrowssoftware.bptrackerfree.fragments;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.eyebrowssoftware.bptrackerfree.BPTrackerFree;
import com.eyebrowssoftware.bptrackerfree.R;
import com.eyebrowssoftware.bptrackerfree.adapters.RangeAdapter;
import com.eyebrowssoftware.bptrackerfree.content.BPRecords;
import com.eyebrowssoftware.bptrackerfree.content.BPRecords.BPRecord;

/**
 * Base class for the two types of editors: spinner-based and text based
 * @author brione
 *
 */
public abstract class BPRecordEditorFragment extends Fragment implements OnDateSetListener, 
		OnTimeSetListener, LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = "BPRecordEditorFragment";

	/**
	 * Editing an existing record 
	 */
	public static final int STATE_EDIT = 0;
	
	/**
	 * Inserting a new record
	 */
	public static final int STATE_INSERT = 1;
	
	protected static final int SYS_IDX = 0;
	protected static final int DIA_IDX = 1;
	protected static final int PLS_IDX = 2;
	protected static final int VALUES_ARRAY_SIZE  = PLS_IDX + 1;

	protected static final int[] SYS_VALS = {
	    BPTrackerFree.SYSTOLIC_MAX_DEFAULT,
		RangeAdapter.NO_ZONE,
		RangeAdapter.NO_ZONE,
		RangeAdapter.NO_ZONE,
		BPTrackerFree.SYSTOLIC_MIN_DEFAULT
	};
			
	protected static final int[] DIA_VALS = {
		BPTrackerFree.DIASTOLIC_MAX_DEFAULT,
		RangeAdapter.NO_ZONE,
		RangeAdapter.NO_ZONE,
		RangeAdapter.NO_ZONE,
		BPTrackerFree.DIASTOLIC_MIN_DEFAULT
	};

	protected static final int[] PLS_VALS = {
		BPTrackerFree.PULSE_MAX_DEFAULT, 
		RangeAdapter.NO_ZONE,
		RangeAdapter.NO_ZONE,
		RangeAdapter.NO_ZONE,
		BPTrackerFree.PULSE_MIN_DEFAULT
	};
	
	protected static final int MENU_GROUP = 0;
	
	// Member Variables
	
	// These are set by reading the arguments that were sent to the Fragment when it was started
	protected int mState = STATE_INSERT;
	protected Uri mUri;
	
	/// and the calendar
	protected Calendar mCalendar;

	protected EditText mNoteText;
	
	protected Button mDateButton;
	
	protected Button mTimeButton;
	
	protected Button mDoneButton;
	
	protected Button mCancelButton;
	
	protected boolean mCompleted = false;
	
	protected static final int BPRECORDS_TOKEN = 0;
	
	protected BPRecordEditorRetainedFragment mStateFragment;

	/**
	 * the name of the _id of the element to work on for editing
	 */
	public static final String DATA_KEY = BPRecord._ID;
	/**
	 * The name action string passed in the bundle arguments
	 */
	public static final String ACTION_KEY = "action";
	
	// This is for restoring from the system-saved state bundle
	private static final String COMPLETED = "completed";
	
	private static final int EDITOR_LOADER_ID = 1;
	
	/**
	 * Must be implemented by Activities or Fragments that use this Fragment
	 * 
	 * @author brione
	 *
	 */
	public interface Callback {
		
		/**
		 * Called when the Fragment is complete, passing an Activity status for status
		 * @param status
		 */
		void onEditComplete(int status);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// Keep weak references to the buttons and edit text in case the query comes back after we're dead
		View layout = inflater.inflate(R.layout.bp_record_editor_fragment, container, false);
		mCalendar = new GregorianCalendar();

		mDateButton = (Button) layout.findViewById(R.id.date_button);
		mDateButton.setOnClickListener(new DateOnClickListener());

		mTimeButton = (Button) layout.findViewById(R.id.time_button);
		mTimeButton.setOnClickListener(new TimeOnClickListener());

		mNoteText = (EditText) layout.findViewById(R.id.note);
		
		mDoneButton = (Button) layout.findViewById(R.id.done_button);
		mDoneButton.setOnClickListener(new DoneButtonListener());
		
		mCancelButton = (Button) layout.findViewById(R.id.revert_button);
		mCancelButton.setOnClickListener(new CancelButtonListener());

		return layout;
	}
	
	/** Called when the fragment is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if(!mCompleted) { // If we're completed, that means we've already made a decision about the record
			saveRecord();
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(COMPLETED, mCompleted);
	}

	private void updateDateTimeDisplay() {
		Date date = mCalendar.getTime();
		mDateButton.setText(BPTrackerFree.getDateString(date, DateFormat.MEDIUM));
		mTimeButton.setText(BPTrackerFree.getTimeString(date, DateFormat.SHORT));
	}
	
	/***
	 * This is called when the asynchronous query completes and subclasses must implement
	 * 
	 * @param systolic
	 * @param diastolic
	 * @param pulse
	 */
	abstract void onQueryComplete(int systolic, int diastolic, int pulse);

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// see if a previous instance left an insert state holder fragment around
        FragmentManager fm = getFragmentManager();
        // Check to see if we have retained the worker fragment.
        mStateFragment = (BPRecordEditorRetainedFragment)fm.findFragmentByTag("work");

        if (mStateFragment != null) {
        	mState = mStateFragment.getState();
        	mUri = mStateFragment.getUri();
        } else {
			Bundle args = this.getArguments();
			String action = args.getString(ACTION_KEY);
			mUri = Uri.parse(args.getString(DATA_KEY));
			// the passed in Uri is generic in the case of ACTION_INSERT
			if (Intent.ACTION_EDIT.equals(action)) {
				mState = STATE_EDIT;
			} else if (Intent.ACTION_INSERT.equals(action)) {
				mState = STATE_INSERT;
				mUri = this.createRecord();
			} else {
				Log.e(TAG, "Unknown action, exiting");
				return;
			}
			// Create a new state holder fragment
	        mStateFragment = BPRecordEditorRetainedFragment.newInstance(mState, mUri);
	        mStateFragment.setTargetFragment(this, 2743);
			fm.beginTransaction().add(mStateFragment, "work").commit();
		}
		if(mState == STATE_INSERT) {
			mCancelButton.setText(R.string.menu_discard);
		}
		this.getLoaderManager().initLoader(EDITOR_LOADER_ID, null, this);
	}
	
	/**
	 * Called when the Loader is created
	 */
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// Create a CursorLoader that will take care of creating a cursor for the data
		CursorLoader loader = new CursorLoader(getActivity(), mUri,	BPTrackerFree.PROJECTION, null, null, null);
		return loader;
	}

	/**
	 * Called when the load of the cursor is finished
	 */
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if(cursor != null && cursor.moveToFirst()) {
			int systolic = cursor.getInt(BPTrackerFree.COLUMN_SYSTOLIC_INDEX);
			int diastolic = cursor.getInt(BPTrackerFree.COLUMN_DIASTOLIC_INDEX);
			int pulse = cursor.getInt(BPTrackerFree.COLUMN_PULSE_INDEX);
			long datetime = cursor.getLong(BPTrackerFree.COLUMN_CREATED_AT_INDEX);
			// long mod_datetime = cursor.getLong(BPTrackerFree.COLUMN_MODIFIED_AT_INDEX);
			String note = cursor.getString(BPTrackerFree.COLUMN_NOTE_INDEX);
			
			if (this.isResumed()) {
				mCalendar.setTimeInMillis(datetime);
				Date date = mCalendar.getTime();
				mDateButton.setText(BPTrackerFree.getDateString(date, DateFormat.MEDIUM));
				mTimeButton.setText(BPTrackerFree.getTimeString(date, DateFormat.SHORT));
				mNoteText.setText(note);
				BPRecordEditorFragment.this.onQueryComplete(systolic, diastolic, pulse);
			}
		}
	}

	/**
	 * Called when the loader is reset
	 */
	public void onLoaderReset(Loader<Cursor> loader) {
		// Nothing to do
	}

	
	/***
	 * Save all the UI information as the current state of the object
	 */
	protected void saveRecord() {
		getActivity().getContentResolver().update(mUri, getCurrentRecordValues(), null, null);
	}
	
	protected Uri createRecord() {
		ContentValues cv = new ContentValues();
		cv.put(BPRecord.SYSTOLIC, BPTrackerFree.SYSTOLIC_DEFAULT);
		cv.put(BPRecord.DIASTOLIC, BPTrackerFree.DIASTOLIC_DEFAULT);
		cv.put(BPRecord.PULSE, BPTrackerFree.PULSE_DEFAULT);
		cv.put(BPRecord.CREATED_DATE, GregorianCalendar.getInstance().getTimeInMillis());
		return this.getActivity().getContentResolver().insert(BPRecords.CONTENT_URI, cv);
	}
	
	protected void revertRecord() {
		getActivity().getContentResolver().update(mUri, this.getOriginalContentValues(), null, null);
	}
	
	protected ContentValues getCurrentRecordValues() {
		ContentValues values = new ContentValues();
		long created = (Long) mCalendar.getTimeInMillis();
		String note = (String) mNoteText.getText().toString();
		
		values.put(BPRecord.CREATED_DATE, created);
		values.put(BPRecord.MODIFIED_DATE, System.currentTimeMillis());
		values.put(BPRecord.NOTE, note);
		return values;
	}
	

	private ContentValues getOriginalContentValues() {
		ContentValues cv = new ContentValues();
		if(mStateFragment != null) {
			Bundle originalValues = mStateFragment.getOriginalValues();
			cv.put(BPRecord.SYSTOLIC, originalValues.getInt(BPRecord.SYSTOLIC));
			cv.put(BPRecord.DIASTOLIC, originalValues.getInt(BPRecord.DIASTOLIC));
			cv.put(BPRecord.PULSE, originalValues.getInt(BPRecord.PULSE));
			cv.put(BPRecord.CREATED_DATE, originalValues.getLong(BPRecord.CREATED_DATE));
			cv.put(BPRecord.MODIFIED_DATE, originalValues.getLong(BPRecord.MODIFIED_DATE));
			cv.put(BPRecord.NOTE, originalValues.getString(BPRecord.NOTE));
		}
		return cv;
	}
	
	private void removeStateFragment() {
        getFragmentManager().beginTransaction().remove(mStateFragment).commit();
        mStateFragment = null;
	}

	/**
	 * Take care of canceling work on a BPRecord. Deletes the record if we had created
	 * it, otherwise reverts to the original record data.
	 */
	protected final void cancelRecord() {
		if (mState == STATE_EDIT) {
			revertRecord();
		} else if (mState == STATE_INSERT) {
			// We inserted an empty record, make sure to delete it
			deleteRecord();
		}
	}

	/**
	 * Take care of deleting a record. Simply close the cursor and deletes the entry.
	 */
	protected final void deleteRecord() {
		getActivity().getContentResolver().delete(mUri, null, null);
	}
	
	public void onDateSet(DatePicker view, int year, int month, int day) {
		mCalendar.set(year, month, day);
		long now = new GregorianCalendar().getTimeInMillis();
		if (mCalendar.getTimeInMillis() > now) {
			Toast.makeText(getActivity(), getString(R.string.msg_future_date), Toast.LENGTH_LONG).show();
			mCalendar.setTimeInMillis(now);
		}
		updateDateTimeDisplay();
	}

	public void onTimeSet(TimePicker view, int hour, int minute) {
		mCalendar.set(Calendar.HOUR_OF_DAY, hour);
		mCalendar.set(Calendar.MINUTE, minute);
		long now = new GregorianCalendar().getTimeInMillis();
		if (mCalendar.getTimeInMillis() > now) {
			Toast.makeText(getActivity(), getString(R.string.msg_future_date), Toast.LENGTH_LONG).show();
			mCalendar.setTimeInMillis(now);
		}
		updateDateTimeDisplay();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Build the menus that are shown when editing.
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.bp_record_editor_fragment_menu, menu);
	}
	
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		int title_id = (mState == STATE_EDIT) ? R.string.menu_revert : R.string.menu_cancel;
		menu.findItem(R.id.menu_cancel).setTitle(title_id);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle all of the possible menu actions.
		switch (item.getItemId()) {
		case R.id.menu_cancel:
			cancelRecord();
			this.complete(Activity.RESULT_CANCELED);
			return true;
		case R.id.menu_done:
			saveRecord();
			this.complete(Activity.RESULT_OK);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void complete(int status) {
		mCompleted = true;
		
		removeStateFragment();
		
		Fragment frag = this.getTargetFragment();
		if(frag != null) {
			((BPRecordEditorFragment.Callback) frag).onEditComplete(status);
		} else {
			((BPRecordEditorFragment.Callback) this.getActivity()).onEditComplete(status);
		}
	}

	// Internal Classes

	private class DoneButtonListener implements OnClickListener {

		public void onClick(View arg0) {
			saveRecord();
			BPRecordEditorFragment.this.complete(Activity.RESULT_OK);
		}
	}
	
	private class CancelButtonListener implements OnClickListener {

		public void onClick(View arg0) {
			cancelRecord();
			BPRecordEditorFragment.this.complete(Activity.RESULT_CANCELED);
		}
	}

	private class DateOnClickListener implements OnClickListener {

		public void onClick(View arg0) {
			DateEditDialogFragment dialog = new DateEditDialogFragment();
			dialog.show(BPRecordEditorFragment.this.getFragmentManager(), "date_edit");
		}
		
	}

	private class TimeOnClickListener implements OnClickListener {

		public void onClick(View v) {
			TimeEditDialogFragment dialog  = new TimeEditDialogFragment();
			dialog.show(BPRecordEditorFragment.this.getFragmentManager(), "time_edit");
		}
		
	}

	protected class DateEditDialogFragment extends DialogFragment {
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceData) {
			return new DatePickerDialog(BPRecordEditorFragment.this.getActivity(), 
				BPRecordEditorFragment.this, mCalendar.get(Calendar.YEAR), 
					mCalendar.get(Calendar.MONTH),
					mCalendar.get(Calendar.DAY_OF_MONTH));
		}
	}
	
	protected class TimeEditDialogFragment extends DialogFragment {
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceData) {
			return new TimePickerDialog(BPRecordEditorFragment.this.getActivity(), 
				BPRecordEditorFragment.this, mCalendar
					.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), false);
		}

	}
}