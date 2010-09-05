package com.eyebrowssoftware.bptrackerfree;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class BPDataManager extends Activity implements OnClickListener {

	private Button mDeleteButton;
	
	private static final int DELETE_DIALOG_ID = 0;

	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.data_manager);
		
		mDeleteButton = (Button) findViewById(R.id.delete_button);
		mDeleteButton.setOnClickListener(this);
	}
	
	public void onClick(View v) {
		if(v.equals(mDeleteButton)) {
			showDialog(DELETE_DIALOG_ID);
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DELETE_DIALOG_ID:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.msg_delete))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.label_yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						deleteHistory();
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

	private void deleteHistory() {
		int deleted = getContentResolver().delete(BPRecords.CONTENT_URI, null, null);
		Toast.makeText(this, String.format(getString(R.string.msg_deleted), deleted), Toast.LENGTH_LONG).show();
	}
}
