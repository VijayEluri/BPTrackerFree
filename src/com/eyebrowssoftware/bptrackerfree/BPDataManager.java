package com.eyebrowssoftware.bptrackerfree;

import android.app.Activity;
import android.os.Bundle;

public class BPDataManager extends Activity implements BPDataManagerFragment.Callback {

	BPDataManagerFragment mDataManagerFragment;

	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.data_manager);
	}
	
	public void onDataManagerComplete(int status) {
		finish();
	}
}
