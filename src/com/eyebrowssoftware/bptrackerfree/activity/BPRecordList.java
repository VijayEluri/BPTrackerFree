package com.eyebrowssoftware.bptrackerfree.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.eyebrowssoftware.bptrackerfree.R;
import com.eyebrowssoftware.bptrackerfree.content.BPRecords;
import com.eyebrowssoftware.bptrackerfree.fragments.BPRecordListFragment;

public class BPRecordList extends FragmentActivity {
	
	@SuppressWarnings("unused")
	private static final String TAG = "BPRecordList";

	@SuppressWarnings("unused")
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
	
}