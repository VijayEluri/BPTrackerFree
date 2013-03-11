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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.eyebrowssoftware.bptrackerfree.BPRecords;
import com.eyebrowssoftware.bptrackerfree.R;
import com.eyebrowssoftware.bptrackerfree.fragments.BPRecordListFragment;

/**
 * @author brionemde
 *
 */
public class BPRecordList extends FragmentActivity implements BPRecordListFragment.Listener {

    @SuppressWarnings("unused")
    private static final String TAG = "BPRecordList";


    @SuppressWarnings("unused")
    private BPRecordListFragment mListFragment;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(BPRecords.CONTENT_URI);
        }
        setContentView(R.layout.bp_record_list);

        setTitle(R.string.title_list);

        mListFragment = (BPRecordListFragment) this.getSupportFragmentManager().findFragmentById(R.id.list_fragment);
    }


    private void doSendAction() {
        startActivity(new Intent(Intent.ACTION_SEND, BPRecords.CONTENT_URI, this, BPSend.class));
    }

    private void doDataManagerAction() {
        startActivity(new Intent(this, BPDataManager.class));
    }

    private void doSettingsAction() {
        startActivity(new Intent(this, BPPreferenceActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.getMenuInflater().inflate(R.menu.bp_list_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case R.id.menu_send:
            doSendAction();
            return true;
        case R.id.menu_data:
            doDataManagerAction();
            return true;
        case R.id.menu_settings:
            doSettingsAction();
            return true;
        default:
            return false;
        }
    }


    @Override
    public void newItem() {
        // TODO Auto-generated method stub

    }


    @Override
    public void deleteItem(long id) {
        // TODO Auto-generated method stub

    }


    @Override
    public void sendItem(long id) {
        // TODO Auto-generated method stub

    }


    @Override
    public void editItem(long id) {
        // TODO Auto-generated method stub

    }
}
