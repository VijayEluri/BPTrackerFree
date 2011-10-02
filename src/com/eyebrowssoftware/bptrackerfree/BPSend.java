package com.eyebrowssoftware.bptrackerfree;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.text.DateFormat;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.csvreader.CsvWriter;
import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;

public class BPSend extends Activity {

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
