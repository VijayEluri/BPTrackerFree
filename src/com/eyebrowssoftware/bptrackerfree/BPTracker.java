package com.eyebrowssoftware.bptrackerfree;

import java.text.DateFormat;
import java.util.Date;

import android.app.Application;
import android.content.res.Configuration;
import android.widget.Spinner;

public class BPTracker extends Application {
	
	@SuppressWarnings("unused")
	private static final String TAG = "BPTracker";
	
	public static final boolean FREE_VERSION = true;

	public static final int SYSTOLIC_MAX_DEFAULT = 280;
	public static final int SYSTOLIC_RED_DEFAULT = 150;
	public static final int SYSTOLIC_ORANGE_DEFAULT = 135;
	public static final int SYSTOLIC_DEFAULT = 120;
	public static final int SYSTOLIC_BLUE_DEFAULT = 80;
	public static final int SYSTOLIC_MIN_DEFAULT = 20;
	public static final int DIASTOLIC_MAX_DEFAULT = 280;
	public static final int DIASTOLIC_RED_DEFAULT = 100;
	public static final int DIASTOLIC_ORANGE_DEFAULT = 90;
	public static final int DIASTOLIC_DEFAULT = 70;
	public static final int DIASTOLIC_BLUE_DEFAULT = 40;
	public static final int DIASTOLIC_MIN_DEFAULT = 20;
	public static final int PULSE_MAX_DEFAULT = 200;
	public static final int PULSE_RED_DEFAULT = 105;
	public static final int PULSE_ORANGE_DEFAULT = 95;
	public static final int PULSE_DEFAULT = 75;
	public static final int PULSE_BLUE_DEFAULT = 55;
	public static final int PULSE_MIN_DEFAULT = 40;

	// Min difference between Systolic and Diastolic or between Max and Min
	// values of anything
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

	public static void setSpinner(Spinner s, int value) {
		RangeAdapter sa = (RangeAdapter) s.getAdapter();
		s.setSelection(sa.getPosition(value));
		sa.notifyDataSetChanged();
	}

	/**
	 * 
	 * @param: date: the Date object that contains the date to be formated
	 *         length: The length from DateFormat class ( SHORT, MEDIUM, LONG,
	 *         FULL )
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
	 * 
	 * @param: date: the Date object that contains the date to be formated
	 *         length: The length from DateFormat class ( SHORT, MEDIUM, LONG,
	 *         FULL )
	 * @return String : formatted time
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

	public static String getDateString(long datetime, int length) {
		return getDateString(new Date(datetime), length);
	}

	public static String getTimeString(long datetime, int length) {
		return getTimeString(new Date(datetime), length);
	}
	
	public static final int BLUE_RANGE = 0;
	public static final int NORMAL_RANGE = BLUE_RANGE + 1;
	public static final int ORANGE_RANGE = NORMAL_RANGE + 1;
	public static final int RED_RANGE = ORANGE_RANGE + 1;

	public static int getLimit(int val, int rlimit, int olimit, int blimit) {		
		if (val < blimit) {
			return  BLUE_RANGE;
		} else if(val > rlimit) {
			return RED_RANGE;
		} else if (val > olimit) {
			return ORANGE_RANGE;
		} else {
			return NORMAL_RANGE;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mShortDateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
		mMediumDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
		mShortTimeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
		mMediumTimeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);
	}
}
