package com.eyebrowssoftware.bptrackerfree;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class BPRecordList extends Activity {
	
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(Menu.NONE, MENU_ITEM_SEND, 0, R.string.menu_send);
		menu.add(Menu.NONE, MENU_DATA_MANAGER, 1, R.string.menu_data);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case MENU_ITEM_SEND:
			startActivity(new Intent(Intent.ACTION_SEND, BPRecords.CONTENT_URI, this, BPSend.class));
			return true;
		case MENU_DATA_MANAGER:
			startActivity(new Intent(this, BPDataManager.class));
			return true;
		default:
			return false;
		}
	}

}