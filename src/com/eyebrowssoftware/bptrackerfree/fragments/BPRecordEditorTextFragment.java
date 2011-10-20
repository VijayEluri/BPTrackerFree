package com.eyebrowssoftware.bptrackerfree.fragments;


import java.lang.ref.WeakReference;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.TextView;

import com.eyebrowssoftware.bptrackerfree.R;
import com.eyebrowssoftware.bptrackerfree.content.BPRecords.BPRecord;

public class BPRecordEditorTextFragment extends BPRecordEditorFragment {
	@SuppressWarnings("unused")
	private static final String TAG = "BPRecordEditorText";

	private TextView mSysLabel;
	private TextView mDiaLabel;
	private TextView mPlsLabel;
	
	private WeakReference<EditText[]> mWeakEditValues;
	
	private EditText[] mEditValues = new EditText[VALUES_ARRAY_SIZE];
	
    public static BPRecordEditorFragment newInstance(Uri uri, String action) {
        BPRecordEditorTextFragment f = new BPRecordEditorTextFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString(DATA_KEY, uri.toString());
        args.putString(ACTION_KEY, action);
        f.setArguments(args);
        return f;
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState) {
		View layout = super.onCreateView(inflater, container, savedInstanceState);

		View myView = ((ViewStub) layout.findViewById(R.id.text_stub)).inflate();
		
		mSysLabel = (TextView) myView.findViewById(R.id.systolic_label);
		mSysLabel.setText(R.string.label_systolic);
		
		mEditValues[SYS_IDX] = (EditText) layout.findViewById(R.id.systolic_edit_text);

		mDiaLabel = (TextView) myView.findViewById(R.id.diastolic_label);
		mDiaLabel.setText(R.string.label_diastolic);
		
		mEditValues[DIA_IDX] = (EditText) layout.findViewById(R.id.diastolic_edit_text);

		mPlsLabel = (TextView) myView.findViewById(R.id.pulse_label);
		mPlsLabel.setText(R.string.label_pulse);
		
		mEditValues[PLS_IDX] = (EditText) layout.findViewById(R.id.pulse_edit_text);
		
		mWeakEditValues = new WeakReference<EditText[]>(mEditValues);
		
		return layout;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	
	@Override
	void onQueryComplete(int systolic, int diastolic, int pulse) {
		EditText[] editValues = mWeakEditValues.get();
		if(editValues != null) {
			mEditValues[SYS_IDX].setText(String.valueOf(systolic));
			mEditValues[DIA_IDX].setText(String.valueOf(diastolic));
			mEditValues[PLS_IDX].setText(String.valueOf(pulse));
		}
		
	}

	@Override
	protected ContentValues getCurrentRecordValues() {
		ContentValues values = super.getCurrentRecordValues();
		int systolic = Integer.valueOf(mEditValues[SYS_IDX].getText().toString());
		int diastolic = Integer.valueOf(mEditValues[DIA_IDX].getText().toString());
		int pulse = Integer.valueOf(mEditValues[PLS_IDX].getText().toString());
		
		values.put(BPRecord.SYSTOLIC, systolic);
		values.put(BPRecord.DIASTOLIC, diastolic);
		values.put(BPRecord.PULSE, pulse);
		return values;
	}
}
