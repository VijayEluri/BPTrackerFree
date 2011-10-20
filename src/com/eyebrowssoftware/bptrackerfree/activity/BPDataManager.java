package com.eyebrowssoftware.bptrackerfree.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.eyebrowssoftware.bptrackerfree.R;
import com.eyebrowssoftware.bptrackerfree.fragments.BPDataManagerFragment;

/**
 * Activity for cleaning out the database
 * @author brione
 *
 */
public class BPDataManager extends FragmentActivity implements BPDataManagerFragment.Callback {

	BPDataManagerFragment mDataManagerFragment;

	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.bp_data_manager);
	}
	
	public void onDataManagerComplete(int status) {
		finish();
	}
}
