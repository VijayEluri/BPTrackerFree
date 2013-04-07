package com.eyebrowssoftware.bptrackerfree.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;

import com.eyebrowssoftware.bptrackerfree.BPTrackerFree;
import com.eyebrowssoftware.bptrackerfree.R;

public class EditorSelectionDialogFragment extends DialogFragment {
    static final String TAG = EditorSelectionDialogFragment.class.toString();

    public interface EditorSelectionDialogListener {
        public void onPositiveButtonClicked(EditorSelectionDialogFragment dialog);
        public void onNegativeButtonClicked(EditorSelectionDialogFragment dialog);
    }

    public static EditorSelectionDialogFragment getNewInstance() {
        return new EditorSelectionDialogFragment();
    }
    private EditorSelectionDialogListener mCallback;
    private boolean mIsText = false;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (EditorSelectionDialogListener) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        mIsText = prefs.getBoolean(BPTrackerFree.IS_TEXT_EDITOR_KEY, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.editor_selection);
        builder.setSingleChoiceItems(R.array.editor_options, mIsText ? 1 : 0, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setIsText(which == 1 ? true : false);
            }
        });
        builder.setPositiveButton(R.string.forever, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCallback.onPositiveButtonClicked(EditorSelectionDialogFragment.this);
            }
        });
        builder.setNegativeButton(R.string.now, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCallback.onNegativeButtonClicked(EditorSelectionDialogFragment.this);
            }
        });
        return builder.create();
    }

    public boolean getIsText() {
        return mIsText;
    }

    private void setIsText(boolean isText) {
        this.mIsText = isText;
    }
}
