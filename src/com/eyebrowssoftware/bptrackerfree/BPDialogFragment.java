package com.eyebrowssoftware.bptrackerfree;

import android.support.v4.app.DialogFragment;

public class BPDialogFragment extends DialogFragment {

	interface Callback {
		void onPositiveButtonClicked();
		void onNegativeButtonClicked();
	}
}
