/*
 * Copyright 2012 - Brion Noble Emde
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
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

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

    public static final String DUAL_PANE_TAG = "dual_pane";


    @SuppressWarnings("unused")
    private BPRecordListFragment mListFragment;
    private FrameLayout mDetailsLayout;
    private boolean mDualPane = false;

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
        mDetailsLayout = (FrameLayout) this.findViewById(R.id.details);
        mDualPane = mDetailsLayout != null && mDetailsLayout.getVisibility() == View.VISIBLE;
    }

    public boolean isDualPane() {
        return mDualPane;
    }

    private void doSendAction() {
        Intent intent = new Intent(Intent.ACTION_SEND, BPRecords.CONTENT_URI, this, BPSend.class);
        intent.putExtra(DUAL_PANE_TAG, mDualPane);
        startActivity(intent);
    }

    private void doDataManagerAction() {
        Intent intent = new Intent(this, BPDataManager.class);
        intent.putExtra(DUAL_PANE_TAG, mDualPane);
        startActivity(intent);
    }

    private void doSettingsAction() {
        Intent intent = new Intent(this, BPPreferenceActivity.class);
        intent.putExtra(DUAL_PANE_TAG, mDualPane);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.getMenuInflater().inflate(R.menu.bp_list_options_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
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
    public void newItem(Uri uri) {
        startActivity(new Intent(Intent.ACTION_INSERT, uri));
    }


    @Override
    public void deleteItem(Uri uri) {
        getContentResolver().delete(uri, null, null);
    }


    @Override
    public void sendItem(Uri uri) {
        startActivity(new Intent(Intent.ACTION_SEND, uri, this, BPSend.class));
        // End of code to be moved
    }


    @Override
    public void editItem(Uri uri) {
        startActivity(new Intent(Intent.ACTION_EDIT, uri));
    }


}
