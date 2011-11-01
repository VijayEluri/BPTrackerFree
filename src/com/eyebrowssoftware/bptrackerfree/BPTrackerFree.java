package com.eyebrowssoftware.bptrackerfree;

import java.text.DateFormat;
import java.util.Date;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.widget.Spinner;
import android.widget.Toast;

import com.eyebrowssoftware.bptrackerfree.adapters.RangeAdapter;
import com.eyebrowssoftware.bptrackerfree.content.BPRecords.BPRecord;

/**
 * Application class for the BPTrackerFree application
 * @author brione
 *
 */
public class BPTrackerFree extends Application {
	
	@SuppressWarnings("unused")
	private static final String TAG = "BPTrackerFree";
	
	/**
	 * Not sure this is used
	 */
	public static final boolean FREE_VERSION = true;
	
	/**
	 * Constant max systolic; derived from a bluetooth bp machine
	 */
	public static final int SYSTOLIC_MAX_DEFAULT = 280;
        /**
         * Constant default systolic; derived from a bluetooth bp machine
         */
	public static final int SYSTOLIC_DEFAULT = 120;
	/**
         * Constant min systolic; derived from a bluetooth bp machine
         */
	public static final int SYSTOLIC_MIN_DEFAULT = 20;
        /**
         * Constant max diastolic; derived from a bluetooth bp machine
         */
	public static final int DIASTOLIC_MAX_DEFAULT = 280;
        /**
         * Constant default diastolic; derived from a bluetooth bp machine
         */
	public static final int DIASTOLIC_DEFAULT = 70;
        /**
         * Constant min diastolic; derived from a bluetooth bp machine
         */
	public static final int DIASTOLIC_MIN_DEFAULT = 20;
        /**
         * Constant max pulse; derived from a bluetooth bp machine
         */
	public static final int PULSE_MAX_DEFAULT = 200;
        /**
         * Constant default pulse; derived from a bluetooth bp machine
         */
	public static final int PULSE_DEFAULT = 75;
        /**
         * Constant min pulse; derived from a bluetooth bp machine
         */
	public static final int PULSE_MIN_DEFAULT = 40;
	
	/**
	 * Standard query projection for most activities
	 */
	public static final String[] PROJECTION = { 
		BPRecord._ID,
		BPRecord.SYSTOLIC, 
		BPRecord.DIASTOLIC, 
		BPRecord.PULSE,
		BPRecord.CREATED_DATE,
		BPRecord.MODIFIED_DATE,
		BPRecord.NOTE 
	};

	// BP Record Indices
	/**
	 * _ID column index in the projection
	 */
	public static final int COLUMN_ID_INDEX = 0;
	/**
	 * Systolic column index in the projection
	 */
	public static final int COLUMN_SYSTOLIC_INDEX = 1;
        /**
         * Diastolic column index in the projection
         */
	public static final int COLUMN_DIASTOLIC_INDEX = 2;
        /**
         * Pulse column index in the projection
         */
	public static final int COLUMN_PULSE_INDEX = 3;
        /**
         * Created At column index in the projection
         */
	public static final int COLUMN_CREATED_AT_INDEX = 4;
        /**
         * Modified At column index in the projection
         */
	public static final int COLUMN_MODIFIED_AT_INDEX = 5;
        /**
         * Note column index in the projection
         */
	public static final int COLUMN_NOTE_INDEX = 6;
	
	/**
	 * Defines the extras string name that defines dual pane mode, if present
	 */
	public static final String DUAL_PANE = "dual_pane";

	/**
	 *  Min difference between Systolic and Diastolic or between Max and Min
	 *  values of anything
	 */
	public static final int MIN_RANGE = 10;

	private static DateFormat mShortDateFormat = DateFormat
			.getDateInstance(DateFormat.SHORT);
	private static DateFormat mMediumDateFormat = DateFormat
			.getDateInstance(DateFormat.MEDIUM);
	private static DateFormat mShortTimeFormat = DateFormat
			.getTimeInstance(DateFormat.SHORT);
	private static DateFormat mMediumTimeFormat = DateFormat
			.getTimeInstance(DateFormat.MEDIUM);

	public void onCreate() {
		super.onCreate();
	}

	/**
	 * Helper function for setting the spinners to the right value
	 * @param s - the spinner to affect
	 * @param value - the value to set
	 */
	public static void setSpinner(Spinner s, int value) {
		RangeAdapter sa = (RangeAdapter) s.getAdapter();
		s.setSelection(sa.getPosition(value));
		sa.notifyDataSetChanged();
	}

	/**
	 * Get a formatted date string
	 * @param date - the data object to query
	 * @param length - The length from DateFormat class ( SHORT, MEDIUM, LONG, FULL )
	 * @return String : formatted date
	 */
	public static String getDateString(Date date, int length) {
		String ret = null;
		if (date == null)
			return null;
		switch (length) {
		case DateFormat.SHORT:
			ret = mShortDateFormat.format(date);
			break;
		case DateFormat.MEDIUM:
			ret = mMediumDateFormat.format(date);
			break;
		default:
			throw new IllegalArgumentException(
					"Unknown time/date format in BPTracker");
		}
		return ret;
	}

	/**
	 * Return a formated time string of the requested length 
	 * @param date - the Date object that contains the date to be formated
	 * @param length - The length from DateFormat class ( SHORT, MEDIUM, LONG, FULL )
	 * @return String - formatted time
	 */
	public static String getTimeString(Date date, int length) {
		String ret = null;
		if (date == null)
			return null;
		switch (length) {
		case DateFormat.SHORT:
			ret = mShortTimeFormat.format(date);
			break;
		case DateFormat.MEDIUM:
			ret = mMediumTimeFormat.format(date);
			break;
		default:
			throw new IllegalArgumentException(
					"Unknown time/date format in BPTracker");
		}
		return ret;
	}

	/**
	 * Overload of the above. Get the date string from the long datetime value
	 * @param datetime - long seconds-since-the-epoch value
	 * @param length - The length from DateFormat class ( SHORT, MEDIUM, LONG, FULL )
	 * @return formatted date string
	 */
	public static String getDateString(long datetime, int length) {
		return getDateString(new Date(datetime), length);
	}

        /**
         * Overload of the above. Get the time string from the long datetime value
         * @param datetime - long seconds-since-the-epoch value
         * @param length - The length from DateFormat class ( SHORT, MEDIUM, LONG, FULL )
         * @return formatted time string
         */
	public static String getTimeString(long datetime, int length) {
		return getTimeString(new Date(datetime), length);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mShortDateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
		mMediumDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
		mShortTimeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
		mMediumTimeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);
	}
	
	/**
	 * Log an error with the tag and the string resource and display a Toast with the message
	 * @param context - Context object
	 * @param tag - Tag for the log
	 * @param strResource - string resource for text of the log and the Toast message
	 */
	public static void logErrorAndToast(Context context, String tag, int strResource) {
		Toast.makeText(context, strResource, Toast.LENGTH_LONG).show();
		Log.e(tag, context.getString(strResource));
	}

        /**
         * Log an error with the tag and the string resource and display a Toast with the message
         * @param context - Context object
         * @param tag - Tag for the log
         * @param msg - string for text of the log and the Toast message
         */
	public static void logErrorAndToast(Context context, String tag, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
		Log.e(tag, msg);
	}


}
