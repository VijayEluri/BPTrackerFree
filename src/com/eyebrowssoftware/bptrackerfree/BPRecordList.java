package com.eyebrowssoftware.bptrackerfree;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

public class BPRecordList extends FragmentActivity {
	
	@SuppressWarnings("unused")
	private static final String TAG = "BPRecordList";

	public static final int MENU_ITEM_SEND = Menu.FIRST + 2;
	public static final int MENU_DATA_MANAGER = Menu.FIRST + 3;

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
	}
}