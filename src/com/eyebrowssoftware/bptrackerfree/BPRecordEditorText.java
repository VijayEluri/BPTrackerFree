package com.eyebrowssoftware.bptrackerfree;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class BPRecordEditorText extends Activity {

	// Static constants
	
	private static final String TAG = "BPRecordEditorText";

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		final Intent intent = getIntent();
		final String action = intent.getAction();

		setContentView(R.layout.bp_record_editor_text);
		
		if (Intent.ACTION_EDIT.equals(action)) {
			// TODO: Create the right type of fragment mState = STATE_EDIT;
		} else if (Intent.ACTION_INSERT.equals(action)) {
			// TODO: Create the right intent for STATE_INSERT
		} else {
			Log.e(TAG, "Unknown action, exiting");
			finish();
			return;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
