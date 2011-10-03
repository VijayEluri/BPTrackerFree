package com.eyebrowssoftware.bptrackerfree;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class BPSend extends Activity {

	@SuppressWarnings("unused")
	private static final String TAG = "BPSend";

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		this.setContentView(R.layout.bp_send);
	}
	// Identifiers of our menu items
	private static final int SEND_ID = Menu.FIRST;
	private static final int CANCEL_ID = Menu.FIRST + 1;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// Build the menus that are shown when editing.
		menu.add(Menu.NONE, SEND_ID, 0, R.string.menu_send);
		menu.add(Menu.NONE, CANCEL_ID, 1, R.string.menu_cancel);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle all of the possible menu actions.
		switch (item.getItemId()) {
		case CANCEL_ID:
			finish();
			return true;
		case SEND_ID:
			// TODO: if(sendData())
			//	finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	

}
