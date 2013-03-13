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
package com.eyebrowssoftware.bptrackerfree.activity;

import java.lang.ref.WeakReference;

import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.Toast;

import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;
import com.eyebrowssoftware.bptrackerfree.BPTrackerFree;
import com.eyebrowssoftware.bptrackerfree.R;
import com.eyebrowssoftware.bptrackerfree.RangeAdapter;

/**
 * @author brionemde
 *
 */
public class BPRecordEditor extends BPRecordEditorBase implements OnItemSelectedListener {

    // Static constants

    protected static final String TAG = "BPRecordEditor";

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
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        int[] sys_vals = {
            BPTrackerFree.SYSTOLIC_MAX_DEFAULT,
            RangeAdapter.NO_ZONE,
            RangeAdapter.NO_ZONE,
            RangeAdapter.NO_ZONE,
            BPTrackerFree.SYSTOLIC_MIN_DEFAULT
        };

        int[] dia_vals = {
            BPTrackerFree.DIASTOLIC_MAX_DEFAULT,
            RangeAdapter.NO_ZONE,
            RangeAdapter.NO_ZONE,
            RangeAdapter.NO_ZONE,
            BPTrackerFree.DIASTOLIC_MIN_DEFAULT
        };

        int[] pls_vals = {
            BPTrackerFree.PULSE_MAX_DEFAULT,
            RangeAdapter.NO_ZONE,
            RangeAdapter.NO_ZONE,
            RangeAdapter.NO_ZONE,
            BPTrackerFree.PULSE_MIN_DEFAULT
        };
        ViewStub myStub = (ViewStub) findViewById(R.id.spinners_stub);
        View mySpinners = myStub.inflate();

        mSpinners = new Spinner[SPINNER_ARRAY_SIZE];

        mSpinners[SYS_IDX] = (Spinner) mySpinners.findViewById(R.id.systolic_spinner);
        mSpinners[SYS_IDX].setPromptId(R.string.label_sys_spinner);
        mSpinners[SYS_IDX].setOnItemSelectedListener(this);
        mSpinners[SYS_IDX].setAdapter(new RangeAdapter(this, sys_vals, true, SPINNER_ITEM_RESOURCE_ID, SPINNER_ITEM_TEXT_VIEW_ID));

        mSpinners[DIA_IDX] = (Spinner) mySpinners.findViewById(R.id.diastolic_spinner);
        mSpinners[DIA_IDX].setPromptId(R.string.label_dia_spinner);
        mSpinners[DIA_IDX].setOnItemSelectedListener(this);
        mSpinners[DIA_IDX].setAdapter(new RangeAdapter(this, dia_vals, true, SPINNER_ITEM_RESOURCE_ID, SPINNER_ITEM_TEXT_VIEW_ID));

        mSpinners[PLS_IDX] = (Spinner) mySpinners.findViewById(R.id.pulse_spinner);
        mSpinners[PLS_IDX].setPromptId(R.string.label_pls_spinner);
        mSpinners[PLS_IDX].setOnItemSelectedListener(this);
        mSpinners[PLS_IDX].setAdapter(new RangeAdapter(this, pls_vals, true, SPINNER_ITEM_RESOURCE_ID, SPINNER_ITEM_TEXT_VIEW_ID));

        mSpinnersReference = new WeakReference<Spinner[]>(mSpinners);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
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
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Spinner sp = (Spinner) parent;
        if (sp.equals(mSpinners[SYS_IDX])) {
            int systolic = ((RangeAdapter) mSpinners[SYS_IDX].getAdapter()).getItem(pos);
            int diastolic = (Integer) mSpinners[DIA_IDX].getSelectedItem();
            if ((systolic - diastolic) < BPTrackerFree.MIN_RANGE) {
                Toast.makeText(this, R.string.check_values, Toast.LENGTH_SHORT).show();
            }
        } else if (sp.equals(mSpinners[DIA_IDX])) {
            int systolic = (Integer) mSpinners[SYS_IDX].getSelectedItem();
            int diastolic = ((RangeAdapter) mSpinners[DIA_IDX].getAdapter()).getItem(pos);
            if ((systolic - diastolic) < BPTrackerFree.MIN_RANGE) {
                Toast.makeText(this, R.string.check_values, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
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
