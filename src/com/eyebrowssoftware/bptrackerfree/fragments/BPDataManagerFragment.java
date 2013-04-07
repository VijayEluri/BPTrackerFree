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

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.eyebrowssoftware.bptrackerfree.BPRecords;
import com.eyebrowssoftware.bptrackerfree.R;

/**
 * @author brionemde
 *
 */
public class BPDataManagerFragment extends Fragment implements OnClickListener, AlertDialogFragment.AlertDialogListener {

    private Button mDeleteButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        View v = inflater.inflate(R.layout.data_manager_fragment, container, false);

        mDeleteButton = (Button) v.findViewById(R.id.delete_button);
        mDeleteButton.setOnClickListener(this);

        return v;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

    }

    public void onClick(View v) {
        if(v.equals(mDeleteButton)) {
            showDeleteConfirmationDialog();
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialogFragment diagFrag = AlertDialogFragment.getNewInstance(R.string.msg_delete, R.string.label_yes, R.string.label_no, this);
        diagFrag.show(this.getFragmentManager(), "delete");
    }

    @Override
    public void onNegativeButtonClicked(AlertDialogFragment dialog) {
        this.getActivity().finish();
    }

    @Override
    public void onPositiveButtonClicked(AlertDialogFragment dialog) {
        deleteHistory();
        this.getActivity().finish();
    }


    private void deleteHistory() {
        Activity activity = this.getActivity();
        int deleted = activity.getContentResolver().delete(BPRecords.CONTENT_URI, null, null);
        Toast.makeText(activity, String.format(getString(R.string.msg_deleted), deleted), Toast.LENGTH_LONG).show();
    }
}
