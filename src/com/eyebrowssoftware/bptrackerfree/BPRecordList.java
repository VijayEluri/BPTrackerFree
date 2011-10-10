package com.eyebrowssoftware.bptrackerfree;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

public class BPRecordList extends FragmentActivity 
		implements BPSendFragment.Callback, BPRecordEditorFragment.Callback {
	
	private static final String TAG = "BPRecordList";

	private BPRecordListFragment mListFragment;
	
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		if (intent.getData() == null) {
			intent.setData(BPRecords.CONTENT_URI);
		}
		setContentView(R.layout.bp_record_list);
		
		setTitle(R.string.title_list);
		FragmentManager mgr = this.getSupportFragmentManager();
		mListFragment = (BPRecordListFragment) mgr.findFragmentById(R.id.list_fragment);
	}
	
	public void onSendComplete(int status) {
		Log.i(TAG, "onSendComplete called with status: " + status);
		BPSendFragment.Callback callback = (BPSendFragment.Callback) mListFragment;
		callback.onSendComplete(status);
	}

	public void onEditComplete(int status) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onEditComplete called with status: " + status);
		BPRecordEditorFragment.Callback callback = (BPRecordEditorFragment.Callback) mListFragment;
		callback.onEditComplete(status);
	}

}