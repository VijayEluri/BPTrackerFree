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

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Spinner;

import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;
import com.eyebrowssoftware.bptrackerfree.BPTrackerFree;
import com.eyebrowssoftware.bptrackerfree.R;
import com.eyebrowssoftware.bptrackerfree.RangeAdapter;

/**
 * @author brionemde
 *
 */
public class BPRecordEditorSpinnerFragment extends BPRecordEditorBaseFragment {

    // Static constants

    protected static final String TAG = "BPRecordEditor";

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
    protected static final int SYS_IDX = 0;
    protected static final int DIA_IDX = 1;
    protected static final int PLS_IDX = 2;
    protected static final int SPINNER_ARRAY_SIZE  = PLS_IDX + 1;

    protected static final int SPINNER_ITEM_RESOURCE_ID = R.layout.bp_spinner_item;
    protected static final int SPINNER_ITEM_TEXT_VIEW_ID = android.R.id.text1;

    // Member Variables
    protected Spinner[] mSpinners = null;

    protected WeakReference<Spinner[]> mSpinnersReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        ViewStub myStub = (ViewStub) v.findViewById(R.id.spinners_stub);
        View mySpinners = myStub.inflate();

        mSpinners = new Spinner[SPINNER_ARRAY_SIZE];
        mSpinners[SYS_IDX] = (Spinner) mySpinners.findViewById(R.id.systolic_spinner);
        mSpinners[SYS_IDX].setPromptId(R.string.label_sys_spinner);

        mSpinners[DIA_IDX] = (Spinner) mySpinners.findViewById(R.id.diastolic_spinner);
        mSpinners[DIA_IDX].setPromptId(R.string.label_dia_spinner);

        mSpinners[PLS_IDX] = (Spinner) mySpinners.findViewById(R.id.pulse_spinner);
        mSpinners[PLS_IDX].setPromptId(R.string.label_pls_spinner);

        return v;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mSpinnersReference = new WeakReference<Spinner[]>(mSpinners);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = this.getActivity();
        mSpinners[SYS_IDX].setAdapter(new RangeAdapter(activity, SYSTOLIC_RANGE_SETUP, true, SPINNER_ITEM_RESOURCE_ID, SPINNER_ITEM_TEXT_VIEW_ID));
        mSpinners[DIA_IDX].setAdapter(new RangeAdapter(activity, DIASTOLIC_RANGE_SETUP, true, SPINNER_ITEM_RESOURCE_ID, SPINNER_ITEM_TEXT_VIEW_ID));
        mSpinners[PLS_IDX].setAdapter(new RangeAdapter(activity, PULSE_RANGE_SETUP, true, SPINNER_ITEM_RESOURCE_ID, SPINNER_ITEM_TEXT_VIEW_ID));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        // The user is going somewhere else, so make sure their current
        // changes are safely saved away in the provider. We don't need
        // to do this if only editing.
        if (mCurrentValues != null) {
            mCurrentValues.put(BPRecord.SYSTOLIC, (Integer) mSpinners[SYS_IDX].getSelectedItem());
            mCurrentValues.put(BPRecord.DIASTOLIC, (Integer) mSpinners[DIA_IDX].getSelectedItem());
            mCurrentValues.put(BPRecord.PULSE, (Integer) mSpinners[PLS_IDX].getSelectedItem());

            updateFromCurrentValues();
        }
    }

    @Override
    protected void setUIState() {
        super.setUIState();

        if(mCurrentValues != null) {
            BPTrackerFree.setSpinner(mSpinners[SYS_IDX], mCurrentValues.getAsInteger(BPRecord.SYSTOLIC));
            BPTrackerFree.setSpinner(mSpinners[DIA_IDX], mCurrentValues.getAsInteger(BPRecord.DIASTOLIC));
            BPTrackerFree.setSpinner(mSpinners[PLS_IDX], mCurrentValues.getAsInteger(BPRecord.PULSE));
        }
    }

}
