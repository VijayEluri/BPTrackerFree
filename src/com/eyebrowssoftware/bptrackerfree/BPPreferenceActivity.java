package com.eyebrowssoftware.bptrackerfree;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * @author brionemde
 *
 */
public class BPPreferenceActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.preferences);
    }
}
