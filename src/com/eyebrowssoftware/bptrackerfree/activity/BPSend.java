package com.eyebrowssoftware.bptrackerfree.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

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

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		this.setContentView(R.layout.bp_send);
		
		mSendFragment = (BPSendFragment) this.getSupportFragmentManager().findFragmentById(R.id.bpsend_fragment);
	}

	public void onSendComplete(int status) {
		Log.v(TAG, "onComplete called with status: " + status);
		finish();
	}

}
