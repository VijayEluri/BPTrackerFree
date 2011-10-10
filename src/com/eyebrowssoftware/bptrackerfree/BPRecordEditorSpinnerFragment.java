package com.eyebrowssoftware.bptrackerfree;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;

public class BPRecordEditorSpinnerFragment extends BPRecordEditorFragment implements OnItemSelectedListener {

	@SuppressWarnings("unused")
	private static final String TAG = "BPRecordEditorFragment";

	private Spinner[] mSpinners = null;

	private static final int SPINNER_ITEM_RESOURCE_ID = R.layout.bp_spinner_item;
	private static final int SPINNER_ITEM_TEXT_VIEW_ID = android.R.id.text1;
	
	private WeakReference<Spinner[]> mWeakSpinners;
	
    public static BPRecordEditorFragment newInstance(Uri uri, String action) {
        BPRecordEditorSpinnerFragment f = new BPRecordEditorSpinnerFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();

        args.putString(BPRecordEditorFragment.DATA_KEY, uri.toString());
        args.putString(ACTION_KEY, action);
        
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
		
		mWeakSpinners = new WeakReference<Spinner[]>(mSpinners);

		return layout;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Activity activity = getActivity();

		mSpinners[SYS_IDX].setAdapter(new RangeAdapter(activity, SYS_VALS, true, SPINNER_ITEM_RESOURCE_ID, SPINNER_ITEM_TEXT_VIEW_ID));
		mSpinners[DIA_IDX].setAdapter(new RangeAdapter(activity, DIA_VALS, true, SPINNER_ITEM_RESOURCE_ID, SPINNER_ITEM_TEXT_VIEW_ID));
		mSpinners[PLS_IDX].setAdapter(new RangeAdapter(activity, PLS_VALS, true, SPINNER_ITEM_RESOURCE_ID, SPINNER_ITEM_TEXT_VIEW_ID));
	}
	

	@Override
	protected ContentValues getCurrentRecordValues() {
		ContentValues values = super.getCurrentRecordValues();
		int systolic = (Integer) mSpinners[SYS_IDX].getSelectedItem();
		int diastolic = (Integer) mSpinners[DIA_IDX].getSelectedItem();
		int pulse = (Integer) mSpinners[PLS_IDX].getSelectedItem();
		
		values.put(BPRecord.SYSTOLIC, systolic);
		values.put(BPRecord.DIASTOLIC, diastolic);
		values.put(BPRecord.PULSE, pulse);
		return values;
	}
	
	@Override
	void onQueryComplete(int systolic, int diastolic, int pulse) {
		Spinner[] spinners = mWeakSpinners.get();
		if(spinners != null) {
			BPTrackerFree.setSpinner(spinners[SYS_IDX], systolic);
			BPTrackerFree.setSpinner(spinners[DIA_IDX], diastolic);
			BPTrackerFree.setSpinner(spinners[PLS_IDX], pulse);
		}
	}
	
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
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