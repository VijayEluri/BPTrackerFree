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

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.eyebrowssoftware.bptrackerfree.BPRecords;
import com.eyebrowssoftware.bptrackerfree.R;
import com.eyebrowssoftware.bptrackerfree.fragments.AlertDialogFragment;
import com.eyebrowssoftware.bptrackerfree.fragments.BPDialogFragment;

/**
 * @author brionemde
 *
 */
public class BPDataManager extends FragmentActivity implements OnClickListener, BPDialogFragment.Callback {

    private Button mDeleteButton;

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.data_manager);

        mDeleteButton = (Button) findViewById(R.id.delete_button);
        mDeleteButton.setOnClickListener(this);
    }

    public void onClick(View v) {
        if(v.equals(mDeleteButton)) {
            showDeleteConfirmationDialog();
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialogFragment diagFrag = AlertDialogFragment.getNewInstance(R.string.msg_delete, R.string.label_yes, R.string.label_no);
        diagFrag.show(this.getSupportFragmentManager(), "delete");
    }

    @Override
    public void onNegativeButtonClicked() {
        // nothing to do, dialog is cancelled already
    }

    @Override
    public void onPositiveButtonClicked() {
        deleteHistory();
        finish();
    }


    private void deleteHistory() {
        int deleted = getContentResolver().delete(BPRecords.CONTENT_URI, null, null);
        Toast.makeText(this, String.format(getString(R.string.msg_deleted), deleted), Toast.LENGTH_LONG).show();
    }
}
