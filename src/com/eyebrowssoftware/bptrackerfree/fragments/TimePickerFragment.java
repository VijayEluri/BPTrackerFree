package com.eyebrowssoftware.bptrackerfree.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

/**
 * @author brionemde
 *
 */
public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    public interface OnTimeChangeListener {
        /**
         * Callback for time has been set
         * @param hours
         * @param minutes
         */
        void setTime(int hours, int minutes);
    }

    public static TimePickerFragment newInstance(int hour, int minute) {
        TimePickerFragment fragment = new TimePickerFragment();
        Bundle args = new Bundle();
        args.putInt(HOUR_KEY, hour);
        args.putInt(MINUTE_KEY, minute);
        fragment.setArguments(args);
        return fragment;
    }

    private static final String HOUR_KEY = "hour";
    private static final String MINUTE_KEY = "minute";

    private OnTimeChangeListener mListener = null;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnTimeChangeListener) activity;
        } catch (ClassCastException e) {
            // this could happen
            e.printStackTrace();
        }
        if (mListener != null) {
            return;
        }
        try {
            mListener = (OnTimeChangeListener) this.getTargetFragment();
        } catch (ClassCastException e) {
            throw new RuntimeException("No OnTimeChangeListener defined in Host Activity/Fragment");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        Bundle args = this.getArguments();

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, args.getInt(HOUR_KEY), args.getInt(MINUTE_KEY), false);
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mListener.setTime(hourOfDay, minute);
    }
}
