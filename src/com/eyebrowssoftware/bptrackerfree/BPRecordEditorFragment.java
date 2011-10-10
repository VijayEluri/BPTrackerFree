package com.eyebrowssoftware.bptrackerfree;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;

public abstract class BPRecordEditorFragment extends Fragment implements OnDateSetListener, OnTimeSetListener {

	private static final String TAG = "BPRecordEditorFragment";

	// The different distinct states the activity can be run in.
	public static final int STATE_EDIT = 0;
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
	
	// These are set by reading the arguments that were sent to the Fragement when it was started
	protected int mState = STATE_INSERT;
	protected Uri mUri;
	
	/// This fragment manages the cursor for the sub-fragments
	protected Cursor mCursor;

	/// and the calendar
	protected Calendar mCalendar;

	//// and the original values TODO: decide whether to create the element after commit
	protected Bundle mOriginalValues = null;
	
	protected EditText mNoteText;
	
	protected Button mDateButton;
	
	protected Button mTimeButton;
	
	protected Button mDoneButton;
	
	protected Button mCancelButton;
	
	protected boolean mCompleted = false;
	
	private WeakReference<EditText> mWeakNoteView;
	private WeakReference<Calendar> mWeakCalendar;
	private WeakReference<Button> mWeakDateButton;
	private WeakReference<Button> mWeakTimeButton;

	
	protected static final int BPRECORDS_TOKEN = 0;

	// These are for arguments passed in the Arguments Bundle
	protected static final String DATA_KEY = BPRecord._ID;
	protected static final String ACTION_KEY = "action";
	
	// This is for restoring from the system-saved state bundle
	private static final String MURI = "sUri";
	private static final String COMPLETED = "completed";
	
	private MyAsyncQueryHandler mMAQH;

	public interface CompleteCallback {
		
		void onEditComplete(int status);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// Keep weak references to the buttons and edit text in case the query comes back after we're dead
		View layout = inflater.inflate(R.layout.bp_record_editor_fragment, container, false);
		mCalendar = new GregorianCalendar();
		mWeakCalendar = new WeakReference<Calendar>(mCalendar);
		
		mDateButton = (Button) layout.findViewById(R.id.date_button);
		mDateButton.setOnClickListener(new DateOnClickListener());
		mWeakDateButton = new WeakReference<Button>(mDateButton);

		mTimeButton = (Button) layout.findViewById(R.id.time_button);
		mTimeButton.setOnClickListener(new TimeOnClickListener());
		mWeakTimeButton = new WeakReference<Button>(mTimeButton);

		mNoteText = (EditText) layout.findViewById(R.id.note);
		mWeakNoteView = new WeakReference<EditText>(mNoteText);
		
		mDoneButton = (Button) layout.findViewById(R.id.done_button);
		mDoneButton.setOnClickListener(new DoneButtonListener());
		
		mCancelButton = (Button) layout.findViewById(R.id.revert_button);
		mCancelButton.setOnClickListener(new CancelButtonListener());
		if(mState == STATE_INSERT) {
			mCancelButton.setText(R.string.menu_discard);
		}
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
		
		outState.putAll(mOriginalValues);
		outState.putString(MURI, mUri.toString());
		outState.putBoolean(COMPLETED, mCompleted);
	}

	public void updateDateTimeDisplay() {
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

		if(savedInstanceState != null) {
			mOriginalValues = new Bundle(savedInstanceState);
			mUri = Uri.parse(savedInstanceState.getString(MURI));
			mCompleted = savedInstanceState.getBoolean(COMPLETED);
		}

		Bundle args = this.getArguments();
		String action = args.getString(ACTION_KEY);
		Uri data = Uri.parse(args.getString(DATA_KEY));

		if (Intent.ACTION_EDIT.equals(action)) {
			mState = STATE_EDIT;
			mUri = data;
		} else if (Intent.ACTION_INSERT.equals(action)) {
			mState = STATE_INSERT;
			if (mUri == null) {
				mUri = createRecord();
			}
		} else {
			Log.e(TAG, "Unknown action, exiting");
			return;
		}

		mMAQH = new MyAsyncQueryHandler(this.getActivity().getContentResolver());
		mMAQH.startQuery(BPRECORDS_TOKEN, TAG, mUri, BPTrackerFree.PROJECTION, null, null, null);
	}
	
	/***
	 * Save all the UI information as the current state of the object
	 */
	protected void saveRecord() {
		mMAQH.startUpdate(BPRecordEditorFragment.BPRECORDS_TOKEN, TAG + "_SAVE", mUri, getCurrentRecordValues(), null, null);
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
		mMAQH.startUpdate(BPRecordEditorFragment.BPRECORDS_TOKEN, TAG + "_REVERT", mUri, getOriginalContentValues(), null, null);
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
				revertRecord();
			} else if (mState == STATE_INSERT) {
				// We inserted an empty record, make sure to delete it
				deleteRecord();
			}
		}
		Activity activity = getActivity();
		activity.setResult(Activity.RESULT_CANCELED);
		// XXX: More dubiosity
		// activity.finish();
		Log.wtf(TAG, "Something needs to happen here");
	}

	/**
	 * Take care of deleting a record. Simply close the cursor and deletes the entry.
	 */
	protected final void deleteRecord() {
		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
			getActivity().getContentResolver().delete(mUri, null, null);
		}
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
		if(mState == STATE_EDIT) {
			menu.add(MENU_GROUP, BPTrackerFree.MENU_ITEM_DONE, 0, R.string.menu_done);
			menu.add(MENU_GROUP, BPTrackerFree.MENU_ITEM_REVERT, 1, R.string.menu_revert);
		} else {
			menu.add(MENU_GROUP, BPTrackerFree.MENU_ITEM_DONE, 0, R.string.menu_done);
			menu.add(MENU_GROUP, BPTrackerFree.MENU_ITEM_DISCARD, 1, R.string.menu_discard);
		}

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle all of the possible menu actions.
		switch (item.getItemId()) {
		case BPTrackerFree.MENU_ITEM_DISCARD:
			// FALLTHROUGH
		case BPTrackerFree.MENU_ITEM_REVERT:
			cancelRecord();
			this.complete(Activity.RESULT_CANCELED);
			return true;
		case BPTrackerFree.MENU_ITEM_DONE:
			saveRecord();
			this.complete(Activity.RESULT_OK);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void complete(int status) {
		mCompleted = true;
		CompleteCallback callback = (CompleteCallback) getActivity();
		callback.onEditComplete(status);
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
					BPRecordEditorFragment.this, mCalendar
						.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
						mCalendar.get(Calendar.DAY_OF_MONTH));
		}
	}
	
	protected class TimeEditDialogFragment extends DialogFragment {
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceData) {
			return new TimePickerDialog(BPRecordEditorFragment.this.getActivity(), 
					BPRecordEditorFragment.this, mCalendar
					.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE),
					false);
		}

	}
	
	private class MyAsyncQueryHandler extends AsyncQueryHandler {

		
		public MyAsyncQueryHandler(ContentResolver cr) {
			super(cr);
		}
		
		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			mCursor = cursor;
			if(mCursor != null) {
				getActivity().startManagingCursor(mCursor);
				if (mCursor.moveToFirst()) {
					int systolic = mCursor.getInt(BPTrackerFree.COLUMN_SYSTOLIC_INDEX);
					int diastolic = mCursor.getInt(BPTrackerFree.COLUMN_DIASTOLIC_INDEX);
					int pulse = mCursor.getInt(BPTrackerFree.COLUMN_PULSE_INDEX);
					long datetime = mCursor.getLong(BPTrackerFree.COLUMN_CREATED_AT_INDEX);
					long mod_datetime = mCursor.getLong(BPTrackerFree.COLUMN_MODIFIED_AT_INDEX);
					String note = mCursor.getString(BPTrackerFree.COLUMN_NOTE_INDEX);
					
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
					EditText noteView = mWeakNoteView.get();
					Button dateButton = mWeakDateButton.get();
					Button timeButton = mWeakTimeButton.get();
					Calendar calendar = mWeakCalendar.get();
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
					BPRecordEditorFragment.this.onQueryComplete(systolic, diastolic, pulse);
					
				}
			}
		}
		
		@Override
		protected void onUpdateComplete(int token, Object cookie, int result) {
			Log.v(TAG, "Update completed with result: " + result);
		}
		
	}

}