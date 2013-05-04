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
        void onPositiveButtonClicked(AlertDialogFragment dialog);
        void onNegativeButtonClicked(AlertDialogFragment dialog);
    }

    // private static final String TYPE_STRING = "type";
    private static final String TITLE_KEY = "title";
    private static final String MESSAGE_KEY = "message";
    private static final String POSITIVE_BUTTON_KEY = "positive";
    private static final String NEGATIVE_BUTTON_KEY = "negative";
    private AlertDialogListener mListener;

    public static AlertDialogFragment getNewInstance(int titleResource, int messageResource,
            int positiveResource, int negativeResource) {
        AlertDialogFragment fragment = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt(TITLE_KEY, titleResource);
        args.putInt(MESSAGE_KEY, messageResource);
        args.putInt(POSITIVE_BUTTON_KEY, positiveResource);
        args.putInt(NEGATIVE_BUTTON_KEY, negativeResource);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity listener) {
        mListener = (AlertDialogListener) listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceData) {
        Bundle args = this.getArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
            .setTitle(args.getInt(TITLE_KEY))
            .setMessage(args.getInt(MESSAGE_KEY))
            .setCancelable(false)
            .setPositiveButton(args.getInt(POSITIVE_BUTTON_KEY), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mListener.onPositiveButtonClicked(AlertDialogFragment.this);
                }
            })
            .setNegativeButton(args.getInt(NEGATIVE_BUTTON_KEY), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    mListener.onNegativeButtonClicked(AlertDialogFragment.this);
                }
            });
        return builder.create();
    }
}
