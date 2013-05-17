/*
 * Copyright 2013 - Brion Noble Emde
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.eyebrowssoftware.bptrackerfree.BPRecords;
import com.eyebrowssoftware.bptrackerfree.R;
import com.eyebrowssoftware.bptrackerfree.fragments.AlertDialogFragment.AlertDialogButtonListener;

/**
 * @author brionemde
 *
 */
public class BPDataManagerFragment extends DialogFragment implements AlertDialogButtonListener {

    public static BPDataManagerFragment newInstance() {
        return new BPDataManagerFragment();
    }

    public interface BPDataListener {
        public void finishing();
    }

    private Button mDeleteButton;
    private BPDataListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        View v = inflater.inflate(R.layout.data_manager_fragment, container, false);

        mDeleteButton = (Button) v.findViewById(R.id.delete_button);
        mDeleteButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }

        });

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (BPDataListener) activity;
    }

    @Override
    public void onNegativeButtonClicked() {
        // Nada
    }

    @Override
    public void onPositiveButtonClicked() {
        deleteHistory();
        getActivity().setResult(Activity.RESULT_OK);
        mListener.finishing();
    }

    // Lint is complaining, but according to the documentation, show() does a commit on the transaction
    // http://developer.android.com/reference/android/app/DialogFragment.html#show(android.app.FragmentTransaction,%20java.lang.String)
    @SuppressLint("CommitTransaction")
    void showDeleteConfirmationDialog() {
        final String DELETE = "delete";
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(DELETE);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(DELETE);

        // Create and show the dialog.
        DialogFragment newFragment = AlertDialogFragment.getNewInstance(
                R.string.label_delete_history, R.string.msg_delete, R.string.label_yes, R.string.label_no);
        newFragment.show(ft, DELETE);
    }

    private void deleteHistory() {
        Activity activity = this.getActivity();
        int deleted = activity.getContentResolver().delete(BPRecords.CONTENT_URI, null, null);
        Toast.makeText(activity, getString(R.string.msg_deleted, deleted), Toast.LENGTH_LONG).show();
    }
}
