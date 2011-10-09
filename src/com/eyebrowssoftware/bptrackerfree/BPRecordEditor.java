package com.eyebrowssoftware.bptrackerfree;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class BPRecordEditor extends Activity {

	// Static constants
	
	@SuppressWarnings("unused")
	private static final String TAG = "BPRecordEditor";

	// Identifiers of our menu items
	private static final int DONE_ID = Menu.FIRST;
	private static final int REVERT_ID = Menu.FIRST + 1;
	private static final int DISCARD_ID = Menu.FIRST + 2;
	private static final int DELETE_ID = Menu.FIRST + 3;
	
	private static final int DELETE_DIALOG_ID = 0;
	
	BPRecordEditorSpinnerFragment mEditorFragment;
	

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.bp_record_editor);
		FragmentManager fMgr = this.getFragmentManager();
		mEditorFragment = (BPRecordEditorSpinnerFragment) fMgr.findFragmentById(R.id.spinner_editor_fragment);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// Modify our overall title depending on the mode we are running in.
		int state = mEditorFragment.getState();
		if (state == BPRecordEditorFragment.STATE_EDIT) {
			setTitle(getText(R.string.title_edit));
		} else if (state == BPRecordEditorFragment.STATE_INSERT) {
			setTitle(getText(R.string.title_create));
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DELETE_DIALOG_ID:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.really_delete))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.label_yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						//TODO: deleteRecord();
						setResult(RESULT_OK);
						finish();
					}
				})
				.setNegativeButton(getString(R.string.label_no), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
			return builder.create();
		default:
			return null;
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle all of the possible menu actions.
		if (item.getItemId() == DELETE_ID) {
			showDialog(DELETE_DIALOG_ID);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	
	
}
