package com.eyebrowssoftware.bptrackerfree;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;

public class BPRecordEditorFragment extends Fragment implements OnDateSetListener,
	OnTimeSetListener, OnItemSelectedListener {

	private static final String TAG = "BPRecordEditorFragment";

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
	
	private static final int SYS_IDX = 0;
	private static final int DIA_IDX = 1;
	private static final int PLS_IDX = 2;
	private static final int SPINNER_ARRAY_SIZE  = PLS_IDX + 1;

	private static final int SPINNER_ITEM_RESOURCE_ID = R.layout.bp_spinner_item;
	private static final int SPINNER_ITEM_TEXT_VIEW_ID = android.R.id.text1;
	
	private static final int[] SYS_VALS = {
		    BPTrackerFree.SYSTOLIC_MAX_DEFAULT,
			RangeAdapter.NO_ZONE,
			RangeAdapter.NO_ZONE,
			RangeAdapter.NO_ZONE,
			BPTrackerFree.SYSTOLIC_MIN_DEFAULT
		};
			
	private static final int[] DIA_VALS = {
			BPTrackerFree.DIASTOLIC_MAX_DEFAULT,
			RangeAdapter.NO_ZONE,
			RangeAdapter.NO_ZONE,
			RangeAdapter.NO_ZONE,
			BPTrackerFree.DIASTOLIC_MIN_DEFAULT
		};

	private static final int[] PLS_VALS = {
			BPTrackerFree.PULSE_MAX_DEFAULT, 
			RangeAdapter.NO_ZONE,
			RangeAdapter.NO_ZONE,
			RangeAdapter.NO_ZONE,
			BPTrackerFree.PULSE_MIN_DEFAULT
		};
		
	// Member Variables
	private int mState;

	private Uri mUri;
	
	private Cursor mCursor;

	private Button mDateButton;
	private Button mTimeButton;
	private EditText mNoteText;

	private Calendar mCalendar;

	private Spinner[] mSpinners = null;

	private Bundle mOriginalValues = null;
	
	private Button mDoneButton;
	
	private Button mCancelButton;
	
	private MyAsyncQueryHandler mMAQH;
	
	private static final int BPRECORDS_TOKEN = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		// Inflate the layout for this fragment
		View layout = inflater.inflate(R.layout.bp_record_editor_fragment, container, false);
		
		mCalendar = new GregorianCalendar();
		
		mDateButton = (Button) layout.findViewById(R.id.date_button);
		mDateButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				DateEditDialogFragment dialog = new DateEditDialogFragment();
				dialog.show(BPRecordEditorFragment.this.getFragmentManager(), "date_edit");
			}
		});

		mTimeButton = (Button) layout.findViewById(R.id.time_button);
		mTimeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				TimeEditDialogFragment dialog  = new TimeEditDialogFragment();
				dialog.show(BPRecordEditorFragment.this.getFragmentManager(), "time_edit");
			}
		});
		
		mDoneButton = (Button) layout.findViewById(R.id.done_button);
		mDoneButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				// XXX: this is highly dubious
				// BPRecordEditorFragment.this.getActivity().finish();
				Log.wtf(TAG, "This needs to do something");
			}
		});
		
		mCancelButton = (Button) layout.findViewById(R.id.revert_button);
		if(mState == STATE_INSERT)
			mCancelButton.setText(R.string.menu_discard);
		mCancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				cancelRecord();
			}
		});
		
		mSpinners = new Spinner[SPINNER_ARRAY_SIZE];
		
		mSpinners[SYS_IDX] = (Spinner) layout.findViewById(R.id.systolic_spin);
		mSpinners[SYS_IDX].setPromptId(R.string.label_sys_spinner);
		mSpinners[SYS_IDX].setOnItemSelectedListener(this);
		
		mSpinners[DIA_IDX] = (Spinner) layout.findViewById(R.id.diastolic_spin);
		mSpinners[DIA_IDX].setPromptId(R.string.label_dia_spinner);
		mSpinners[DIA_IDX].setOnItemSelectedListener(this);

		mSpinners[PLS_IDX] = (Spinner) layout.findViewById(R.id.pulse_spin);
		mSpinners[PLS_IDX].setPromptId(R.string.label_pls_spinner);
		mSpinners[PLS_IDX].setOnItemSelectedListener(this);

		mNoteText = (EditText) layout.findViewById(R.id.note);

		return layout;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
	}
	
	
	/** Called when the fragment is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if(savedInstanceState != null) {
			mOriginalValues = new Bundle(savedInstanceState);
		}
		Activity activity = getActivity();
		Intent intent = activity.getIntent();

		if (intent.getData() == null) {
			intent.setData(BPRecords.CONTENT_URI);
		}

		mSpinners[SYS_IDX].setAdapter(new RangeAdapter(activity, SYS_VALS, true, SPINNER_ITEM_RESOURCE_ID, SPINNER_ITEM_TEXT_VIEW_ID));
		mSpinners[DIA_IDX].setAdapter(new RangeAdapter(activity, DIA_VALS, true, SPINNER_ITEM_RESOURCE_ID, SPINNER_ITEM_TEXT_VIEW_ID));
		mSpinners[PLS_IDX].setAdapter(new RangeAdapter(activity, PLS_VALS, true, SPINNER_ITEM_RESOURCE_ID, SPINNER_ITEM_TEXT_VIEW_ID));

		String action = intent.getAction();
		if (Intent.ACTION_EDIT.equals(action)) {
			mState = STATE_EDIT;
			mUri = intent.getData();
		} else if (Intent.ACTION_INSERT.equals(action)) {
			mState = STATE_INSERT;
			if (savedInstanceState != null)
				mUri = Uri.parse(savedInstanceState.getString(BPTrackerFree.MURI));
			else {
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
		
		mMAQH = new MyAsyncQueryHandler(activity.getContentResolver(), mSpinners, mNoteText, 
				mCalendar, mDateButton, mTimeButton);
		mMAQH.startQuery(BPRECORDS_TOKEN, this, mUri, PROJECTION, null, null, null);
	}
	

	@Override
	public void onResume() {
		super.onResume();
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
	public void onSaveInstanceState(Bundle outState) {
		outState.putAll(mOriginalValues);
		outState.putString(BPTrackerFree.MURI, mUri.toString());
	}

	@Override
	public void onPause() {
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
			getActivity().getContentResolver().update(mUri, values, null, null);
		}
	}
	
	@Override
	public void onDestroy() {
		if(mCursor != null) {
			getActivity().stopManagingCursor(mCursor);
			mCursor.close();
			mCursor = null;
		}
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	private class DateEditDialogFragment extends DialogFragment {
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceData) {
			return new DatePickerDialog(BPRecordEditorFragment.this.getActivity(), 
					BPRecordEditorFragment.this, mCalendar
						.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
						mCalendar.get(Calendar.DAY_OF_MONTH));
		}

	}
	
	private class TimeEditDialogFragment extends DialogFragment {
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceData) {
			return new TimePickerDialog(BPRecordEditorFragment.this.getActivity(), 
					BPRecordEditorFragment.this, mCalendar
					.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE),
					false);
		}

	}
	
	private class MyAsyncQueryHandler extends AsyncQueryHandler {

		private WeakReference<Spinner[]> mSpinners;
		private WeakReference<EditText> mNoteView;
		private WeakReference<Calendar> mCalendar;
		private WeakReference<Button> mDateButton;
		private WeakReference<Button> mTimeButton;
		
		public MyAsyncQueryHandler(ContentResolver cr, Spinner[] spinners, EditText noteView, 
				Calendar calendar, Button dateButton, Button timeButton) {
			super(cr);
			mSpinners = new WeakReference<Spinner[]>(spinners);
			mNoteView = new WeakReference<EditText>(noteView);
			mCalendar = new WeakReference<Calendar>(calendar);
			mDateButton = new WeakReference<Button>(dateButton);
			mTimeButton = new WeakReference<Button>(timeButton);
		}
		
		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			mCursor = cursor;
			if(mCursor != null) {
				getActivity().startManagingCursor(mCursor);
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
					Spinner[] spinners = mSpinners.get();
					EditText noteView = mNoteView.get();
					Button dateButton = mDateButton.get();
					Button timeButton = mTimeButton.get();
					Calendar calendar = mCalendar.get();
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
	
	public void updateDateTimeDisplay() {
		Date date = mCalendar.getTime();
		mDateButton.setText(BPTrackerFree.getDateString(date, DateFormat.MEDIUM));
		mTimeButton.setText(BPTrackerFree.getTimeString(date, DateFormat.SHORT));
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
	private final void deleteRecord() {
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