package com.eyebrowssoftware.bptrackerfree.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Semi-generic dialog fragment that displays a simple alert dialog with two buttons.
 *
 * @author brione
 *
 */
public class AlertDialogFragment extends BPDialogFragment {
    @SuppressWarnings("unused")
    private static final String TAG = "AlertDialogFragment";

    // private static final String TYPE_STRING = "type";
    private static final String TITLE_STRING = "title";
    private static final String POSITIVE_BUTTON_STRING = "positive";
    private static final String NEGATIVE_BUTTON_STRING = "negative";

    /**
     * Return a new instance, with the following settings
     *
     * @param title_res - title string resource
     * @param positive_res - positive button string resource
     * @param negative_res - negative button string resource
     * @return new instance of this class
     */
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
        BPDialogFragment.Callback callback = (Callback) AlertDialogFragment.this.getTargetFragment();
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
