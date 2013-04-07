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


import android.annotation.TargetApi;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.EditText;

import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;
import com.eyebrowssoftware.bptrackerfree.R;

/**
 * @author brionemde
 *
 */
public class BPRecordEditorTextFragment extends BPRecordEditorBaseFragment {

    // Static constants

    protected static final String TAG = "BPRecordEditorText";

    private static final int SYS_IDX = 0;
    private static final int DIA_IDX = 1;
    private static final int PLS_IDX = 2;
    private static final int VALUES_ARRAY_SIZE  = PLS_IDX + 1;

    private EditText[] mEditValues = new EditText[VALUES_ARRAY_SIZE];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        ViewStub myStub = (ViewStub) v.findViewById(R.id.edit_texts_stub);
        View myEditTexts = myStub.inflate();

        mEditValues[SYS_IDX] = (EditText) myEditTexts.findViewById(R.id.systolic_edit_text);

        mEditValues[DIA_IDX] = (EditText) myEditTexts.findViewById(R.id.diastolic_edit_text);

        mEditValues[PLS_IDX] = (EditText) myEditTexts.findViewById(R.id.pulse_edit_text);

        return v;
    }

    @TargetApi(5)
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

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
            mCurrentValues.put(BPRecord.SYSTOLIC, Integer.valueOf(mEditValues[SYS_IDX].getText().toString()));
            mCurrentValues.put(BPRecord.DIASTOLIC, Integer.valueOf(mEditValues[DIA_IDX].getText().toString()));
            mCurrentValues.put(BPRecord.PULSE, Integer.valueOf(mEditValues[PLS_IDX].getText().toString()));

            updateFromCurrentValues();
        }
    }

    @Override
    protected void setUIState() {
        super.setUIState();
        if (mCurrentValues != null) {
            mEditValues[SYS_IDX].setText(String.valueOf(mCurrentValues.getAsInteger(BPRecord.SYSTOLIC)));
            mEditValues[DIA_IDX].setText(String.valueOf(mCurrentValues.getAsInteger(BPRecord.DIASTOLIC)));
            mEditValues[PLS_IDX].setText(String.valueOf(mCurrentValues.getAsInteger(BPRecord.PULSE)));
        }
    }
}
