package com.eyebrowssoftware.bptrackerfree;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class AlertDialogFragment extends DialogFragment {
	
	interface Callback {
		void onPositiveButtonClicked();
		void onNegativeButtonClicked();
	}
	
	private static final String TITLE_STRING = "title";
	private static final String POSITIVE_BUTTON_STRING = "positive";
	private static final String NEGATIVE_BUTTON_STRING = "negative";
	
	public static AlertDialogFragment getNewInstance(int title_res, int positive_res, int negative_res) {
		AlertDialogFragment fragment = new AlertDialogFragment();
		Bundle args = new Bundle();
		args.putInt(TITLE_STRING, title_res);
		args.putInt(POSITIVE_BUTTON_STRING, positive_res);
		args.putInt(NEGATIVE_BUTTON_STRING, negative_res);
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceData) {

		Bundle args = this.getArguments();
		Callback callback = (Callback) AlertDialogFragment.this.getTargetFragment();
		if(callback == null) {
			callback = (Callback) AlertDialogFragment.this.getActivity(); 
		}
		final Callback cb = callback; 
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(args.getInt(TITLE_STRING))
			.setCancelable(false)
			.setPositiveButton(args.getInt(POSITIVE_BUTTON_STRING), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					cb.onPositiveButtonClicked();
				}
			})
			.setNegativeButton(args.getInt(NEGATIVE_BUTTON_STRING), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					cb.onNegativeButtonClicked();
				}
			});
		return builder.create();
	}
}
