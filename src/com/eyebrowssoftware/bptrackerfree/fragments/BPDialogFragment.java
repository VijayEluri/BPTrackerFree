package com.eyebrowssoftware.bptrackerfree.fragments;

import android.support.v4.app.DialogFragment;

/**
 * @author brionemde
 *
 */
public class BPDialogFragment extends DialogFragment {
    /**
     * Callback interface
     *
     * @author brionemde
     *
     */
    public interface Callback {
        /**
         * Callback for positive button clicked
         */
        void onPositiveButtonClicked();
        /**
         * Callback for negative button clicked
         */
        void onNegativeButtonClicked();
    }

}
