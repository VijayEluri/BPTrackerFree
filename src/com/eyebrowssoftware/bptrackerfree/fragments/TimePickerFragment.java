package com.eyebrowssoftware.bptrackerfree.fragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

/**
 * @author brionemde
 *
 */
public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    /**
     * Key for hours argument in the bundle
     */
    public static final String HOUR_KEY = "hour";
    /**
     * Key for minutes argument in the bundle
     */
    public static final String MINUTE_KEY = "minute";

    /**
     * @author brionemde
     *
     */
    public interface Callbacks {
        /**
         * Callback for time has been set
         * @param hours
         * @param minutes
         */
        void setTime(int hours, int minutes);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        Bundle args = this.getArguments();

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, args.getInt(HOUR_KEY), args.getInt(MINUTE_KEY), false);
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        ((Callbacks) this.getActivity()).setTime(hourOfDay, minute);
    }
}
