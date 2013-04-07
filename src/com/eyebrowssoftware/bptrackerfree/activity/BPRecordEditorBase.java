package com.eyebrowssoftware.bptrackerfree.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.eyebrowssoftware.bptrackerfree.BPTrackerFree;
import com.eyebrowssoftware.bptrackerfree.R;
import com.eyebrowssoftware.bptrackerfree.fragments.BPRecordEditorSpinnerFragment;
import com.eyebrowssoftware.bptrackerfree.fragments.BPRecordEditorTextFragment;
import com.eyebrowssoftware.bptrackerfree.fragments.EditorSelectionDialogFragment;

/**
 * @author brionemde
 *
 */
public class BPRecordEditorBase extends FragmentActivity implements EditorSelectionDialogFragment.EditorSelectionDialogListener {

    // Static constants

    static final String TAG = BPRecordEditorBase.class.toString();

    private static final String DIALOG_SHOWN_KEY = "dialog_shown";
    private SharedPreferences mSharedPreferences;
    private boolean mWasEditorSelectionDialogShown = false;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.bp_record_editor);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isText = mSharedPreferences.getBoolean(BPTrackerFree.IS_TEXT_EDITOR_KEY, false);
        Log.d(TAG, "onCreate: isText: " + (isText ? "true" : "false"));
        boolean isUserSet = mSharedPreferences.getBoolean(BPTrackerFree.USER_SELECTED_EDITOR_KEY, false);
        Log.d(TAG, "onCreate: isUserSet: " + (isUserSet ? "true" : "false"));
        if (icicle != null) {
            this.mWasEditorSelectionDialogShown = icicle.getBoolean(DIALOG_SHOWN_KEY);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isText = mSharedPreferences.getBoolean(BPTrackerFree.IS_TEXT_EDITOR_KEY, false);
        Log.d(TAG, "onResume: isText: " + (isText ? "true" : "false"));
        loadEditorFragment(isText, false);
        boolean isUserSet = mSharedPreferences.getBoolean(BPTrackerFree.USER_SELECTED_EDITOR_KEY, false);
        Log.d(TAG, "onResume: isText: " + (isText ? "true" : "false"));
        if (!isUserSet && !this.mWasEditorSelectionDialogShown) {
            EditorSelectionDialogFragment dialog = EditorSelectionDialogFragment.getNewInstance();
            dialog.show(this.getSupportFragmentManager(), "editor");
            this.mWasEditorSelectionDialogShown = true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(DIALOG_SHOWN_KEY, this.mWasEditorSelectionDialogShown);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void loadEditorFragment(boolean isText, boolean persist) {
        FragmentManager fm = this.getSupportFragmentManager();
        String key = isText ? "text" : "spinner";
        Fragment current = fm.findFragmentByTag(key);
        if (current == null) {
            FragmentTransaction ft = fm.beginTransaction();
            Fragment fragment = (isText) ? new BPRecordEditorTextFragment() : new BPRecordEditorSpinnerFragment();
            ft.replace(R.id.container, fragment, key);
            ft.commit();
        }
        if (persist) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(BPTrackerFree.USER_SELECTED_EDITOR_KEY, true);
            editor.commit();
        }
    }

    @Override
    public void onNegativeButtonClicked(EditorSelectionDialogFragment dialog) {
        boolean isText = dialog.getIsText();
        loadEditorFragment(isText, false);
    }

    @Override
    public void onPositiveButtonClicked(EditorSelectionDialogFragment dialog) {
        boolean isText = dialog.getIsText();
        loadEditorFragment(isText, true);
    }

}
