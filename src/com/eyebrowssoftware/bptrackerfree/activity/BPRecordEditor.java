package com.eyebrowssoftware.bptrackerfree.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.eyebrowssoftware.bptrackerfree.BPTrackerFree;
import com.eyebrowssoftware.bptrackerfree.R;
import com.eyebrowssoftware.bptrackerfree.fragments.BPRecordEditorFragment;
import com.eyebrowssoftware.bptrackerfree.fragments.BPRecordEditorSpinnerFragment;

/**
 * Activity for Spinner-based version of the editor.
 * 
 * @author brione
 *
 */
public class BPRecordEditor extends FragmentActivity  implements BPRecordEditorFragment.Callback {
	private static final String TAG = "BPRecordEditor";

	private boolean mDualPane = false;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Intent intent = getIntent();
		Uri uri = intent.getData();
		String action = intent.getAction();
		
		mDualPane = intent.getBooleanExtra(BPTrackerFree.DUAL_PANE, false);

        if (mDualPane && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // If the screen is now in landscape mode, we can show the
            // dialog in-line so we don't need this activity.
            Log.v(TAG, "onCreate: Should we finish here? Doesn't seem to work.");
        	// finish();
            // return;
        }

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
		BPRecordEditorFragment mEditorFragment = BPRecordEditorSpinnerFragment.newInstance(uri, action);
		// Pass the action and data Uri to the fragment via fragment arguments
		this.getSupportFragmentManager().beginTransaction().add(R.id.editor_fragment_container, mEditorFragment).commit();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Log.v(TAG, "onStart()");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.v(TAG, "onResume()");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.v(TAG, "onPause()");
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.v(TAG, "onStop()");
	}

	public void onEditComplete(int status) {
		Log.i(TAG, "Got an onEditComplete(" + status + ")");
		finish();
	}
}