/*
 * Copyright 2010 - Brion Noble Emde
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.eyebrowssoftware.bptrackerfree.fragments;

import junit.framework.Assert;
import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;
import com.eyebrowssoftware.bptrackerfree.BPTrackerFree;
import com.eyebrowssoftware.bptrackerfree.R;
import com.eyebrowssoftware.bptrackerfree.RangeAdapter;
import com.eyebrowssoftware.bptrackerfree.fragments.BPRecordEditorFragment.EditorPlugin;

/**
 * @author brionemde
 *
 */
public class EditorSpinnerFragment extends Fragment implements EditorPlugin{
    static final String TAG = "BPRecordEditor";

    private static final int[] SYSTOLIC_RANGE_SETUP = {
        BPTrackerFree.SYSTOLIC_MAX_DEFAULT,
        RangeAdapter.NO_ZONE,
        RangeAdapter.NO_ZONE,
        RangeAdapter.NO_ZONE,
        BPTrackerFree.SYSTOLIC_MIN_DEFAULT
    };

    private static final int[] DIASTOLIC_RANGE_SETUP = {
        BPTrackerFree.DIASTOLIC_MAX_DEFAULT,
        RangeAdapter.NO_ZONE,
        RangeAdapter.NO_ZONE,
        RangeAdapter.NO_ZONE,
        BPTrackerFree.DIASTOLIC_MIN_DEFAULT
    };

    private static final int[] PULSE_RANGE_SETUP = {
        BPTrackerFree.PULSE_MAX_DEFAULT,
        RangeAdapter.NO_ZONE,
        RangeAdapter.NO_ZONE,
        RangeAdapter.NO_ZONE,
        BPTrackerFree.PULSE_MIN_DEFAULT
    };

    private Spinner mSystolic;
    private Spinner mDiastolic;
    private Spinner mPulse;

    protected static final int SPINNER_ITEM_RESOURCE_ID = R.layout.bp_spinner_item;
    protected static final int SPINNER_ITEM_TEXT_VIEW_ID = android.R.id.text1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mySpinners = inflater.inflate(R.layout.spinners_fragment, container, false);
        Activity activity = this.getActivity();

        mSystolic = (Spinner) mySpinners.findViewById(R.id.systolic_spinner);
        mSystolic.setPromptId(R.string.label_sys_spinner);
        mSystolic.setAdapter(new RangeAdapter(activity, SYSTOLIC_RANGE_SETUP, true, SPINNER_ITEM_RESOURCE_ID, SPINNER_ITEM_TEXT_VIEW_ID));

        mDiastolic = (Spinner) mySpinners.findViewById(R.id.diastolic_spinner);
        mDiastolic.setPromptId(R.string.label_dia_spinner);
        mDiastolic.setAdapter(new RangeAdapter(activity, DIASTOLIC_RANGE_SETUP, true, SPINNER_ITEM_RESOURCE_ID, SPINNER_ITEM_TEXT_VIEW_ID));

        mPulse = (Spinner) mySpinners.findViewById(R.id.pulse_spinner);
        mPulse.setPromptId(R.string.label_pls_spinner);
        mPulse.setAdapter(new RangeAdapter(activity, PULSE_RANGE_SETUP, true, SPINNER_ITEM_RESOURCE_ID, SPINNER_ITEM_TEXT_VIEW_ID));

        return mySpinners;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void setSpinner(Spinner s, int value) {
        RangeAdapter sa = (RangeAdapter) s.getAdapter();
        Assert.assertNotNull(sa);
        s.setSelection(sa.getPosition(value));
        sa.notifyDataSetChanged();
    }

    @Override
    public void setCurrentValues(ContentValues values) {
        this.setSpinner(mSystolic, values.getAsInteger(BPRecord.SYSTOLIC));
        this.setSpinner(mDiastolic, values.getAsInteger(BPRecord.DIASTOLIC));
        this.setSpinner(mPulse, values.getAsInteger(BPRecord.PULSE));
    }

    @Override
    public ContentValues getCurrentValues() {
        ContentValues values = new ContentValues();
        values.put(BPRecord.SYSTOLIC, (Integer) mSystolic.getSelectedItem());
        values.put(BPRecord.DIASTOLIC, (Integer) mDiastolic.getSelectedItem());
        values.put(BPRecord.PULSE, (Integer) mPulse.getSelectedItem());
        return values;
    }

}
