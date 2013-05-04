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
    @SuppressWarnings("unused")
    private static final String TAG = "AlertDialogFragment";

    public interface AlertDialogButtonListener {
        public void onPositiveButtonClicked();
        public void onNegativeButtonClicked();
    }

    private static final String TITLE_KEY = "title";
    private static final String MESSAGE_KEY = "message";
    private static final String POSITIVE_BUTTON_KEY = "positive";
    private static final String NEGATIVE_BUTTON_KEY = "negative";

    private AlertDialogButtonListener mListener = null;

    /**
     * Return a new instance, with the following settings
     *
     * @param titleRes - title string resource
     * @param positiveRes - positive button string resource
     * @param negativeRes - negative button string resource
     * @return new instance of this class
     */
    public static AlertDialogFragment getNewInstance(int titleRes, int messageRes, int positiveRes, int negativeRes) {

        AlertDialogFragment fragment = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt(TITLE_KEY, titleRes);
        args.putInt(MESSAGE_KEY, messageRes);
        args.putInt(POSITIVE_BUTTON_KEY, positiveRes);
        args.putInt(NEGATIVE_BUTTON_KEY, negativeRes);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (AlertDialogButtonListener) activity;
        } catch (ClassCastException e) {
            // this could happen
            e.printStackTrace();
        }
        if (mListener != null) {
            return;
        }
        try {
            mListener = (AlertDialogButtonListener) this.getTargetFragment();
        } catch (ClassCastException e) {
            throw new RuntimeException("No AlertDialogButtonListener defined in Host Activity/Fragment");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceData) {

        Bundle args = this.getArguments();
        AlertDialogButtonListener callback = (AlertDialogButtonListener) AlertDialogFragment.this.getTargetFragment();
        if(callback == null) {
            callback = (AlertDialogButtonListener) AlertDialogFragment.this.getActivity();
        }
        final AlertDialogButtonListener cb = callback;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
            .setTitle(args.getInt(TITLE_KEY))
            .setMessage(args.getInt(MESSAGE_KEY))
            .setCancelable(false)
            .setPositiveButton(args.getInt(POSITIVE_BUTTON_KEY), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    cb.onPositiveButtonClicked();
                }
            })
            .setNegativeButton(args.getInt(NEGATIVE_BUTTON_KEY), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    cb.onNegativeButtonClicked();
                }
            });
        return builder.create();
    }
}
