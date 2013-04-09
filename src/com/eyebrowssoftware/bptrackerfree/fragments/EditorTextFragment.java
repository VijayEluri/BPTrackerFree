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


import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;
import com.eyebrowssoftware.bptrackerfree.R;
import com.eyebrowssoftware.bptrackerfree.fragments.BPRecordEditorFragment.EditorPlugin;

/**
 * @author brionemde
 *
 */
public class EditorTextFragment extends Fragment implements EditorPlugin {
    static final String TAG = "BPRecordEditorText";

    private EditText mSystolic;
    private EditText mDiastolic;
    private EditText mPulse;

    private ContentValues mCurrentValues;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View myEditTexts = inflater.inflate(R.layout.spinners_fragment, container, false);

        mSystolic = (EditText) myEditTexts.findViewById(R.id.systolic_edit_text);

        mDiastolic = (EditText) myEditTexts.findViewById(R.id.diastolic_edit_text);

        mPulse = (EditText) myEditTexts.findViewById(R.id.pulse_edit_text);

        return myEditTexts;
    }

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
        // changes are safely saved away cache object
        if (mCurrentValues != null) {
            mCurrentValues.put(BPRecord.SYSTOLIC, Integer.valueOf(mSystolic.getText().toString()));
            mCurrentValues.put(BPRecord.DIASTOLIC, Integer.valueOf(mDiastolic.getText().toString()));
            mCurrentValues.put(BPRecord.PULSE, Integer.valueOf(mPulse.getText().toString()));
        }
    }

    @Override
    public void setCurrentValues(ContentValues values) {
        mCurrentValues = new ContentValues(values);
        mSystolic.setText(String.valueOf(mCurrentValues.getAsInteger(BPRecord.SYSTOLIC)));
        mDiastolic.setText(String.valueOf(mCurrentValues.getAsInteger(BPRecord.DIASTOLIC)));
        mPulse.setText(String.valueOf(mCurrentValues.getAsInteger(BPRecord.PULSE)));
    }

    @Override
    public ContentValues getCurrentValues() {
        return mCurrentValues;
    }
}
