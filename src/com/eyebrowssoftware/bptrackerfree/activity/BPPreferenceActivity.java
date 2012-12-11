package com.eyebrowssoftware.bptrackerfree.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.eyebrowssoftware.bptrackerfree.R;

/**
 * @author brionemde
 *
 */
public class BPPreferenceActivity extends PreferenceActivity {

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.preferences);
    }
}
