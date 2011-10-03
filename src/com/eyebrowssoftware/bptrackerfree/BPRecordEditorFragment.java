package com.eyebrowssoftware.bptrackerfree;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;

public abstract class BPRecordEditorFragment extends Fragment implements OnDateSetListener, OnTimeSetListener {

	private static final String TAG = "BPRecordEditorFragment";

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
	
	protected static final int SYS_IDX = 0;
	protected static final int DIA_IDX = 1;
	protected static final int PLS_IDX = 2;
	protected static final int VALUES_ARRAY_SIZE  = PLS_IDX + 1;

	protected static final int SPINNER_ITEM_RESOURCE_ID = R.layout.bp_spinner_item;
	protected static final int SPINNER_ITEM_TEXT_VIEW_ID = android.R.id.text1;
	
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
		
	// Member Variables
	protected int mState;

	protected Uri mUri;
	
	protected Cursor mCursor;

	protected Calendar mCalendar;

	protected Bundle mOriginalValues = null;
	
	protected static final int BPRECORDS_TOKEN = 0;
	protected static final String ID_KEY = BPRecord._ID;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View layout = inflater.inflate(R.layout.bp_record_editor_fragment, container, false);
		mCalendar = new GregorianCalendar();

		return layout;
	}

	/** Called when the fragment is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

    protected long getShownID() {
        return getArguments().getLong(ID_KEY, 0L);
    }
    
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if(savedInstanceState != null) {
			mOriginalValues = new Bundle(savedInstanceState);
			mUri = Uri.parse(savedInstanceState.getString(BPTrackerFree.MURI));
		}
		Activity activity = getActivity();
		Intent intent = activity.getIntent();

		if (intent.getData() == null) {
			intent.setData(BPRecords.CONTENT_URI);
		}

		String action = intent.getAction();
		if (Intent.ACTION_EDIT.equals(action)) {
			mState = STATE_EDIT;
			mUri = intent.getData();
		} else if (Intent.ACTION_INSERT.equals(action)) {
			mState = STATE_INSERT;
			if (mUri == null) {
				ContentValues cv = new ContentValues();
				cv.put(BPRecord.SYSTOLIC, BPTrackerFree.SYSTOLIC_DEFAULT);
				cv.put(BPRecord.DIASTOLIC, BPTrackerFree.DIASTOLIC_DEFAULT);
				cv.put(BPRecord.PULSE, BPTrackerFree.PULSE_DEFAULT);
				cv.put(BPRecord.CREATED_DATE, GregorianCalendar.getInstance().getTimeInMillis());
				mUri = activity.getContentResolver().insert(intent.getData(), cv);
			}
		} else {
			Log.e(TAG, "Unknown action, exiting");
			return;
		}
	}
	
	protected class DateOnClickListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			DateEditDialogFragment dialog = new DateEditDialogFragment();
			dialog.show(BPRecordEditorFragment.this.getFragmentManager(), "date_edit");
		}
		
	}

	protected class TimeOnClickListener implements OnClickListener {

		@Override
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
				getActivity().getContentResolver().update(mUri, getOriginalContentValues(), null, null);
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
	
	protected abstract void updateDateTimeDisplay();
	
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

	
}