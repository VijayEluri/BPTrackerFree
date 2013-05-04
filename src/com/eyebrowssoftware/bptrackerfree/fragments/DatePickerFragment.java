package com.eyebrowssoftware.bptrackerfree.fragments;

import com.eyebrowssoftware.bptrackerfree.fragments.TimePickerFragment.OnTimeChangeListener;

import android.app.Activity;
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

    public interface OnDateChangeListener {
        /**
         * Callback for time has been set
         * @param year
         * @param month
         * @param day
         */
        void setDate(int year, int month, int day);
    }

    public static DatePickerFragment newInstance(int day, int month, int year) {
        DatePickerFragment fragment = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putInt(DAY_KEY, day);
        args.putInt(MONTH_KEY, month);
        args.putInt(YEAR_KEY, year);
        fragment.setArguments(args);
        return fragment;
    }

    private static final String DAY_KEY = "day";
    private static final String MONTH_KEY = "month";
    private static final String YEAR_KEY = "year";

    private OnDateChangeListener mListener = null;


    /**
     * @author brionemde
     *
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = this.getArguments();

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, args.getInt(YEAR_KEY), args.getInt(MONTH_KEY), args.getInt(DAY_KEY));
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDateChangeListener) activity;
        } catch (ClassCastException e) {
            // this could happen
            e.printStackTrace();
        }
        if (mListener != null) {
            return;
        }
        try {
            mListener = (OnDateChangeListener) this.getTargetFragment();
        } catch (ClassCastException e) {
            throw new RuntimeException("No OnTimeChangeListener defined in Host Activity/Fragment");
        }
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        mListener.setDate(year, month, day);
    }
}
