package com.eyebrowssoftware.bptrackerfree.fragments;

import android.app.Activity;
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
    private AlertDialogListener mCallback;

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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (AlertDialogListener) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceData) {

        Bundle args = this.getArguments();
        final AlertDialogListener cb = mCallback;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(args.getInt(TITLE_STRING))
            .setCancelable(false)
            .setPositiveButton(args.getInt(POSITIVE_BUTTON_STRING), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    cb.onPositiveButtonClicked(AlertDialogFragment.this);
                }
            })
            .setNegativeButton(args.getInt(NEGATIVE_BUTTON_STRING), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    cb.onNegativeButtonClicked(AlertDialogFragment.this);
                }
            });
        return builder.create();
    }
}
