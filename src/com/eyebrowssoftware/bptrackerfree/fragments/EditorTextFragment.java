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
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;
import com.eyebrowssoftware.bptrackerfree.BPTrackerFree;
import com.eyebrowssoftware.bptrackerfree.R;
import com.eyebrowssoftware.bptrackerfree.fragments.BPRecordEditorFragment.EditorPlugin;

public class EditorTextFragment extends Fragment implements EditorPlugin, LoaderCallbacks<Cursor> {
    static final String TAG = "EditorTextFragment";

    private static final String URI_KEY = "uri_key";
    private static final int TEXT_EDITOR_LOADER_ID = 4782;

    public static EditorTextFragment newInstance(Uri uri) {
        EditorTextFragment fragment = new EditorTextFragment();
        Bundle args = new Bundle();
        args.putString(URI_KEY, uri.toString());
        fragment.setArguments(args);
        return fragment;
    }

    private EditText mSystolic;
    private EditText mDiastolic;
    private EditText mPulse;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.text_fragment, container, false);

        mSystolic = (EditText) v.findViewById(R.id.systolic_edit_text);
        mDiastolic = (EditText) v.findViewById(R.id.diastolic_edit_text);
        mPulse = (EditText) v.findViewById(R.id.pulse_edit_text);
        Log.d(TAG, "onCreateView");

        return v;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        this.getActivity().getSupportLoaderManager().initLoader(TEXT_EDITOR_LOADER_ID, null, this);
        Log.d(TAG, "onCreate");

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

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        Uri uri = Uri.parse(getArguments().getString(URI_KEY));
        return new CursorLoader(this.getActivity(), uri, BPTrackerFree.PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == TEXT_EDITOR_LOADER_ID && cursor.moveToFirst()) {
            Log.d(TAG, "onLoadFinished");
            Assert.assertNotNull(cursor);
            Assert.assertNotNull(mSystolic);
            Assert.assertNotNull(mDiastolic);
            Assert.assertNotNull(mPulse);
            mSystolic.setText(String.valueOf(cursor.getInt(BPTrackerFree.COLUMN_SYSTOLIC_INDEX)));
            mDiastolic.setText(String.valueOf(cursor.getInt(BPTrackerFree.COLUMN_DIASTOLIC_INDEX)));
            mPulse.setText(String.valueOf(cursor.getInt(BPTrackerFree.COLUMN_PULSE_INDEX)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
    }
}
