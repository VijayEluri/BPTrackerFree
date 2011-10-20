package com.eyebrowssoftware.bptrackerfree.fragments;

import android.support.v4.app.DialogFragment;

/**
 * Base class of the AlertDialogFragment that defines the callback interface
 * 
 * @author brione
 *
 */
public class BPDialogFragment extends DialogFragment {

	interface Callback {
		void onPositiveButtonClicked();
		void onNegativeButtonClicked();
	}
}
