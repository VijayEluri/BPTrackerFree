package com.eyebrowssoftware.bptrackerfree.fragments;

import junit.framework.Assert;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Semi-generic dialog fragment that displays a simple alert dialog with two buttons.
 *
 * @author brione
 *
 */
public class AlertDialogFragment extends DialogFragment {
    static final String TAG = "AlertDialogFragment";

    /**
     * Callback interface
     *
     **/
    public interface AlertDialogListener {
        /**
         * Callback for positive button clicked
         */
        void onPositiveButtonClicked(AlertDialogFragment dialog);
        /**
         * Callback for negative button clicked
         */
        void onNegativeButtonClicked(AlertDialogFragment dialog);
    }

    // private static final String TYPE_STRING = "type";
    private static final String TITLE_STRING = "title";
    private static final String POSITIVE_BUTTON_STRING = "positive";
    private static final String NEGATIVE_BUTTON_STRING = "negative";
    private AlertDialogListener mListener;

    public static AlertDialogFragment getNewInstance(int title_res, int positive_res, int negative_res, AlertDialogListener listener) {
        Assert.assertNotNull(listener);
        AlertDialogFragment fragment = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt(TITLE_STRING, title_res);
        args.putInt(POSITIVE_BUTTON_STRING, positive_res);
        args.putInt(NEGATIVE_BUTTON_STRING, negative_res);
        fragment.setArguments(args);
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(AlertDialogListener listener) {
        mListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceData) {
        Bundle args = this.getArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(args.getInt(TITLE_STRING))
            .setCancelable(false)
            .setPositiveButton(args.getInt(POSITIVE_BUTTON_STRING), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mListener.onPositiveButtonClicked(AlertDialogFragment.this);
                }
            })
            .setNegativeButton(args.getInt(NEGATIVE_BUTTON_STRING), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    mListener.onNegativeButtonClicked(AlertDialogFragment.this);
                }
            });
        return builder.create();
    }
}
