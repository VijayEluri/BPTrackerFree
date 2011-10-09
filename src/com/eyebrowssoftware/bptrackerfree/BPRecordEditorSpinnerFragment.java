package com.eyebrowssoftware.bptrackerfree;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
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
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;

public class BPRecordEditorSpinnerFragment extends BPRecordEditorFragment implements OnItemSelectedListener {

	private static final String TAG = "BPRecordEditorFragment";

	private Spinner[] mSpinners = null;

	private Bundle mOriginalValues = null;

	private static final int SPINNER_ITEM_RESOURCE_ID = R.layout.bp_spinner_item;
	private static final int SPINNER_ITEM_TEXT_VIEW_ID = android.R.id.text1;
	
	private static final String ID_KEY = "_id";
	
	private MyAsyncQueryHandler mMAQH;
	
    public static BPRecordEditorFragment newInstance(long id) {
        BPRecordEditorSpinnerFragment f = new BPRecordEditorSpinnerFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putLong(ID_KEY, id);
        f.setArguments(args);

        return f;
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View layout = super.onCreateView(inflater, container, savedInstanceState);

		// Inflate the layout for this fragment
		// View myView = ((ViewStub) layout.findViewById(R.id.spinner_stub)).inflate();
		ViewStub myViewStub = (ViewStub) layout.findViewById(R.id.spinner_stub);
		View myView = myViewStub.inflate();

		mSpinners = new Spinner[VALUES_ARRAY_SIZE];
		
		mSpinners[SYS_IDX] = (Spinner) myView.findViewById(R.id.systolic_spin);
		mSpinners[SYS_IDX].setPromptId(R.string.label_sys_spinner);
		mSpinners[SYS_IDX].setOnItemSelectedListener(this);
		
		mSpinners[DIA_IDX] = (Spinner) myView.findViewById(R.id.diastolic_spin);
		mSpinners[DIA_IDX].setPromptId(R.string.label_dia_spinner);
		mSpinners[DIA_IDX].setOnItemSelectedListener(this);

		mSpinners[PLS_IDX] = (Spinner) myView.findViewById(R.id.pulse_spin);
		mSpinners[PLS_IDX].setPromptId(R.string.label_pls_spinner);
		mSpinners[PLS_IDX].setOnItemSelectedListener(this);

		return layout;
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