package com.eyebrowssoftware.bptrackerfree.activity;


import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.eyebrowssoftware.bptrackerfree.BPTrackerFree;
import com.eyebrowssoftware.bptrackerfree.R;
import com.eyebrowssoftware.bptrackerfree.fragments.BPRecordEditorFragment;
import com.eyebrowssoftware.bptrackerfree.fragments.BPRecordEditorTextFragment;

/**
 * Activity class for the EditText version of the editor
 * 
 * @author brione
 *
 */
public class BPRecordEditorText extends FragmentActivity implements BPRecordEditorFragment.Callback {
	private static final String TAG = "BPRecordEditorText";
	
	private boolean mDualPane = false;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		final Intent intent = getIntent();
		
		mDualPane = intent.getBooleanExtra(BPTrackerFree.DUAL_PANE, false);

        if (mDualPane && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // If the screen is now in landscape mode, we can show the
            // dialog in-line so we don't need this activity.
            finish();
            return;
        }

		
		final String action = intent.getAction();

		setContentView(R.layout.bp_record_editor);
		
		if (Intent.ACTION_EDIT.equals(action)) {
			setTitle(getText(R.string.title_edit));
		} else if (Intent.ACTION_INSERT.equals(action)) {
			setTitle(getText(R.string.title_create));
		} else {
			Log.e(TAG, "Unknown action, exiting");
			finish();
			return;
		}
		BPRecordEditorTextFragment editorFragment = new BPRecordEditorTextFragment();
		Bundle args = new Bundle();
		args.putString(BPRecordEditorFragment.DATA_KEY, intent.getDataString());
		args.putString(BPRecordEditorFragment.ACTION_KEY, action);
		editorFragment.setArguments(args);
		
		this.getSupportFragmentManager().beginTransaction().add(R.id.editor_fragment_container, editorFragment).commit();
	}
	
	public void onEditComplete(int status) {
		Log.e(TAG, "Got an onEditComplete(" + status + ")");
		finish();
	}
}
