package com.eyebrowssoftware.bptrackerfree;


import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;

public class BPRecordEditorTextFragment extends BPRecordEditorFragment {

	// Static constants
	
	private static final String TAG = "BPRecordEditorText";


	private Cursor mCursor;

	private Button mDateButton;
	private Button mTimeButton;

	private Calendar mCalendar = GregorianCalendar.getInstance();

	private TextView mSysLabel;
	private TextView mDiaLabel;
	private TextView mPlsLabel;
	
	private EditText[] mEditValues = new EditText[VALUES_ARRAY_SIZE];
	
	private EditText mNote;

	private Bundle mOriginalValues = null;
	
    public static BPRecordEditorFragment newInstance(long id) {
        BPRecordEditorTextFragment f = new BPRecordEditorTextFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putLong(ID_KEY, id);
        f.setArguments(args);

        return f;
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View layout = inflater.inflate(R.layout.bp_record_editor_text, container, false);
		
		mDateButton = (Button) layout.findViewById(R.id.date_button);
		mDateButton.setOnClickListener(new DateOnClickListener());

		mTimeButton = (Button) layout.findViewById(R.id.time_button);
		mTimeButton.setOnClickListener(new TimeOnClickListener());

		Button button;
		
		button = (Button) layout.findViewById(R.id.done_button);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				// TODO: Figure this out / onBackPressed();
			}
		});
		
		button = (Button) layout.findViewById(R.id.revert_button);
		if(mState == STATE_INSERT)
			button.setText(R.string.menu_discard);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				cancelRecord();
			}
		});
		
		mSysLabel = (TextView) layout.findViewById(R.id.systolic_label);
		mSysLabel.setText(R.string.label_systolic);
		
		mEditValues[SYS_IDX] = (EditText) layout.findViewById(R.id.systolic_edit_text);

		mDiaLabel = (TextView) layout.findViewById(R.id.diastolic_label);
		mDiaLabel.setText(R.string.label_diastolic);
		
		mEditValues[DIA_IDX] = (EditText) layout.findViewById(R.id.diastolic_edit_text);

		mPlsLabel = (TextView) layout.findViewById(R.id.pulse_label);
		mPlsLabel.setText(R.string.label_pulse);
		
		mEditValues[PLS_IDX] = (EditText) layout.findViewById(R.id.pulse_edit_text);
		
		mNote = (EditText) layout.findViewById(R.id.note);
		
		return layout;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mCursor = getActivity().managedQuery(mUri, PROJECTION, null, null, null);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// If we didn't have any trouble retrieving the data, it is now
		// time to get at the stuff.
		if (mCursor != null && mCursor.moveToFirst()) {

			// Modify our overall title depending on the mode we are running in.
			// TODO: Dubious stuff here
			if (mState == STATE_EDIT) {
				getActivity().setTitle(getText(R.string.title_edit));
			} else if (mState == STATE_INSERT) {
				getActivity().setTitle(getText(R.string.title_create));
			}
			int systolic = mCursor.getInt(COLUMN_SYSTOLIC_INDEX);
			mEditValues[SYS_IDX].setText(String.valueOf(systolic));

			int diastolic = mCursor.getInt(COLUMN_DIASTOLIC_INDEX);
			mEditValues[DIA_IDX].setText(String.valueOf(diastolic));

			int pulse = mCursor.getInt(COLUMN_PULSE_INDEX);
			mEditValues[PLS_IDX].setText(String.valueOf(pulse));

			long datetime = mCursor.getLong(COLUMN_CREATED_AT_INDEX);
			long mod_datetime = mCursor.getLong(COLUMN_MODIFIED_AT_INDEX);
			mCalendar.setTimeInMillis(datetime);
			updateDateTimeDisplay();
			
			String note = mCursor.getString(COLUMN_NOTE_INDEX);
			mNote.setText(note);

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
		}
	}

	public void updateDateTimeDisplay() {
		mDateButton.setText(BPTrackerFree.getDateString(mCalendar.getTime(),
				DateFormat.MEDIUM));
		mTimeButton.setText(BPTrackerFree.getTimeString(mCalendar.getTime(),
				DateFormat.SHORT));
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putAll(mOriginalValues);
		outState.putString(BPTrackerFree.MURI, mUri.toString());
	}

	@Override
	public void onPause() {
		super.onPause();

		// The user is going somewhere else, so make sure their current
		// changes are safely saved away in the provider. We don't need
		// to do this if only editing.
		if (mCursor != null) {
			int systolic = Integer.valueOf(mEditValues[SYS_IDX].getText().toString());
			int diastolic = Integer.valueOf(mEditValues[DIA_IDX].getText().toString());
			int pulse = Integer.valueOf(mEditValues[PLS_IDX].getText().toString());
			long created = (Long) mCalendar.getTimeInMillis();
			String note = mNote.getText().toString();
			
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
		closeCursors();
		super.onDestroy();
	}
	
	@Override
	protected void finalize() {
		try {
			closeCursors();
			super.finalize();
		} catch (Throwable e) {
			Log.e(TAG, "Finalize error", e);
			e.printStackTrace();
		}
	}
	
	private void closeRecordCursor() {
		if(mCursor != null) {
			mCursor.close();
			mCursor = null;
		}
	}
	
	private void closeCursors() {
		closeRecordCursor();
	}
	
}
