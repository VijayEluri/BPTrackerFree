package com.eyebrowssoftware.bptrackerfree.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.eyebrowssoftware.bptrackerfree.BPTrackerFree;
import com.eyebrowssoftware.bptrackerfree.R;

/**
 * @author brionemde
 *
 */
public class BPPreferenceActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    EditTextPreference mSysETP, mDiaETP, mPlsETP;
    SharedPreferences mPreferences;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.preferences);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mSysETP = (EditTextPreference) this.findPreference(BPTrackerFree.DEFAULT_SYSTOLIC_KEY);
        mDiaETP = (EditTextPreference) this.findPreference(BPTrackerFree.DEFAULT_DIASTOLIC_KEY);
        mPlsETP = (EditTextPreference) this.findPreference(BPTrackerFree.DEFAULT_PULSE_KEY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPreferences.registerOnSharedPreferenceChangeListener(this);

        setSystolicSummary();
        setDiastolicSummary();
        setPulseSummary();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setSystolicSummary() {
        String string = mPreferences.getString(BPTrackerFree.DEFAULT_SYSTOLIC_KEY, BPTrackerFree.SYSTOLIC_DEFAULT_STRING);
        String summary = String.format(this.getString(R.string.systolic_initial_format), string);
        mSysETP.setSummary(summary);
    }

    private void setDiastolicSummary() {
        String string = mPreferences.getString(BPTrackerFree.DEFAULT_DIASTOLIC_KEY, BPTrackerFree.DIASTOLIC_DEFAULT_STRING);
        String summary = String.format(this.getString(R.string.diastolic_initial_format), string);
        mDiaETP.setSummary(summary);
    }

    private void setPulseSummary() {
        String string = mPreferences.getString(BPTrackerFree.DEFAULT_PULSE_KEY, BPTrackerFree.PULSE_DEFAULT_STRING);
        String summary = String.format(this.getString(R.string.pulse_initial_format), string);
        mPlsETP.setSummary(summary);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(BPTrackerFree.DEFAULT_SYSTOLIC_KEY)) {
            setSystolicSummary();
        } else if (key.equals(BPTrackerFree.DEFAULT_DIASTOLIC_KEY)) {
            setDiastolicSummary();
        } else if (key.equals(BPTrackerFree.DEFAULT_PULSE_KEY)) {
            setPulseSummary();
        }
        return;
    }
}
