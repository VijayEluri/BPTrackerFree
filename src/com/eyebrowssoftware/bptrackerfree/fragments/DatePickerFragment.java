package com.eyebrowssoftware.bptrackerfree.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

/**
 * @author brionemde
 *
 */
public class DatePickerFragment extends DialogFragment
    implements DatePickerDialog.OnDateSetListener {

    /**
     * Key for day argument in the bundle
     */
    public static final String DAY_KEY = "day";
    /**
     * Key for month argument in the bundle
     */
    public static final String MONTH_KEY = "month";

    /**
     * Key for month argument in the bundle
     */
    public static final String YEAR_KEY = "year";

    /**
     * @author brionemde
     *
     */
    public interface Callbacks {
        /**
         * Callback for time has been set
         * @param year
         * @param month
         * @param day
         */
        void setDate(int year, int month, int day);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = this.getArguments();

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, args.getInt(YEAR_KEY), args.getInt(MONTH_KEY), args.getInt(DAY_KEY));
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        ((Callbacks) this.getActivity()).setDate(year, month, day);
    }
}
