package com.eyebrowssoftware.bptrackerfree.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.eyebrowssoftware.bptrackerfree.R;
import com.eyebrowssoftware.bptrackerfree.fragments.BPRecordEditorFragment;
import com.eyebrowssoftware.bptrackerfree.fragments.BPRecordEditorFragment.BPEditorListener;

public class BPRecordEditor extends FragmentActivity implements BPEditorListener {
    static final String TAG = "BPRecordEditor";

    private static final String FRAGMENT_TAG = "main_editor_fragment";

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.bp_fragment_container);

        Uri uri = null;
        Intent intent = getIntent();
        if (Intent.ACTION_EDIT.equals(intent.getAction())) {
            uri = intent.getData();
        }
        if (icicle == null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment current = fm.findFragmentByTag(FRAGMENT_TAG);
            if (current != null) {
                ft.remove(current);
            }
            ft.add(R.id.editor_container, BPRecordEditorFragment.newInstance(uri), FRAGMENT_TAG);
            ft.commit();
        }
    }

    @Override
    public void finishing() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.getMenuInflater().inflate(R.menu.bp_record_editor_activity_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(this, BPPreferenceActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
