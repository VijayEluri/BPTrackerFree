package com.eyebrowssoftware.bptrackerfree;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class BPRecordEditor extends FragmentActivity  implements BPRecordEditorFragment.CompleteCallback {
	private static final String TAG = "BPRecordEditor";

	private BPRecordEditorSpinnerFragment mEditorFragment;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		final Intent intent = getIntent();
		final String action = intent.getAction();
		
		setContentView(R.layout.bp_record_editor);

		if (Intent.ACTION_EDIT.equals(action)) {
			// TODO: Create the right type of fragment mState = STATE_EDIT;
			setTitle(getText(R.string.title_edit));
		} else if (Intent.ACTION_INSERT.equals(action)) {
			// TODO: Create the right intent for STATE_INSERT
			setTitle(getText(R.string.title_create));
		} else {
			Log.e(TAG, "Unknown action, exiting");
			finish();
			return;
		}
		mEditorFragment = (BPRecordEditorSpinnerFragment) this.getFragmentManager().findFragmentById(R.id.spinner_editor_fragment);
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	public void onEditComplete(int status) {
		// TODO Auto-generated method stub
		Log.e(TAG, "Got an onEditComplete(" + status + ")");
	}
}
