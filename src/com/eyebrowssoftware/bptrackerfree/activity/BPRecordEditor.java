package com.eyebrowssoftware.bptrackerfree.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.eyebrowssoftware.bptrackerfree.R;

/**
 * @author brionemde
 *
 */
public class BPRecordEditor extends FragmentActivity {

    // Static constants

    static final String TAG = BPRecordEditor.class.toString();


    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.bp_record_editor);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
