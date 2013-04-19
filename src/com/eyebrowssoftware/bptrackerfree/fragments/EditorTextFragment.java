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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.text_fragment, container, false);

        mSystolic = (EditText) v.findViewById(R.id.systolic_edit_text);
        mDiastolic = (EditText) v.findViewById(R.id.diastolic_edit_text);
        mPulse = (EditText) v.findViewById(R.id.pulse_edit_text);

        return v;
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
    }

    @Override
    public void setCurrentValues(ContentValues values) {
        mSystolic.setText(String.valueOf(values.getAsInteger(BPRecord.SYSTOLIC)));
        mDiastolic.setText(String.valueOf(values.getAsInteger(BPRecord.DIASTOLIC)));
        mPulse.setText(String.valueOf(values.getAsInteger(BPRecord.PULSE)));
    }

    @Override
    public void updateCurrentValues(ContentValues values) {
        String textValue = mSystolic.getText().toString();
        if (textValue.length() > 0) {
            values.put(BPRecord.SYSTOLIC, Integer.valueOf(textValue));
        }
        textValue = mDiastolic.getText().toString();
        if (textValue.length() > 0) {
            values.put(BPRecord.DIASTOLIC, Integer.valueOf(mDiastolic.getText().toString()));
        }
        textValue = mPulse.getText().toString();
        if (textValue.length() > 0) {
            values.put(BPRecord.PULSE, Integer.valueOf(mPulse.getText().toString()));
        }
    }
}
