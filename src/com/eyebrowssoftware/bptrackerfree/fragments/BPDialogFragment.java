package com.eyebrowssoftware.bptrackerfree.fragments;

import android.support.v4.app.DialogFragment;

/**
 * @author brionemde
 *
 */
public class BPDialogFragment extends DialogFragment {
    interface Callback {
        void onPositiveButtonClicked();
        void onNegativeButtonClicked();
    }

}
