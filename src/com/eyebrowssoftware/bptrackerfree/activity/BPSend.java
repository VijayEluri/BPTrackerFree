package com.eyebrowssoftware.bptrackerfree.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.eyebrowssoftware.bptrackerfree.BPTrackerFree;
import com.eyebrowssoftware.bptrackerfree.R;
import com.eyebrowssoftware.bptrackerfree.fragments.BPSendFragment;

/**
 * Activity class for sharing the bp records via email or sms/mms
 * 
 * @author brione
 *
 */
public class BPSend extends FragmentActivity implements BPSendFragment.Callback {
	private static final String TAG = "BPSend";
	
	@SuppressWarnings("unused")
	private BPSendFragment mSendFragment;

	private boolean mDualPane;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		mDualPane = getIntent().getBooleanExtra(BPTrackerFree.DUAL_PANE, false);

        if (mDualPane && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // If the screen is now in landscape mode, we can show the
            // dialog in-line so we don't need this activity.
            Log.v(TAG, "onCreate: Should we finish here? Doesn't seem to work.");
        	// finish();
            // return;
        }

		this.setContentView(R.layout.bp_send);
		
		mSendFragment = (BPSendFragment) this.getSupportFragmentManager().findFragmentById(R.id.bpsend_fragment);
	}

	public void onSendComplete(int status) {
		Log.v(TAG, "onComplete called with status: " + status);
		finish();
	}

}
