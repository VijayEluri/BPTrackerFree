package com.eyebrowssoftware.bptrackerfree;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

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
