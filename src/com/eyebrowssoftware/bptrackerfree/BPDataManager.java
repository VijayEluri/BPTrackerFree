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
package com.eyebrowssoftware.bptrackerfree;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * @author brionemde
 *
 */
public class BPDataManager extends Activity implements OnClickListener {

    private Button mDeleteButton;

    private static final int DELETE_DIALOG_ID = 0;

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.data_manager);

        mDeleteButton = (Button) findViewById(R.id.delete_button);
        mDeleteButton.setOnClickListener(this);
    }

    public void onClick(View v) {
        if(v.equals(mDeleteButton)) {
            showDialog(DELETE_DIALOG_ID);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DELETE_DIALOG_ID:
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.msg_delete))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.label_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteHistory();
                    }
                })
                .setNegativeButton(getString(R.string.label_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
            return builder.create();
        default:
            return null;
        }
    }

    private void deleteHistory() {
        int deleted = getContentResolver().delete(BPRecords.CONTENT_URI, null, null);
        Toast.makeText(this, String.format(getString(R.string.msg_deleted), deleted), Toast.LENGTH_LONG).show();
    }
}
