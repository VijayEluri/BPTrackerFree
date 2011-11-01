package com.eyebrowssoftware.bptrackerfree.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.eyebrowssoftware.bptrackerfree.BPTrackerFree;
import com.eyebrowssoftware.bptrackerfree.R;
import com.eyebrowssoftware.bptrackerfree.fragments.BPDataManagerFragment;

/**
 * Activity for cleaning out the database
 * @author brione
 *
 */
public class BPDataManager extends FragmentActivity implements BPDataManagerFragment.Callback {

	private boolean mDualPane = false;

	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		mDualPane = getIntent().getBooleanExtra(BPTrackerFree.DUAL_PANE, false);

        if (mDualPane && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // If the screen is now in landscape mode, we can show the
            // dialog in-line so we don't need this activity.
            finish();
            return;
        }

		setContentView(R.layout.bp_data_manager);
	}
	
	public void onDataManagerComplete(int status) {
		finish();
	}
}
