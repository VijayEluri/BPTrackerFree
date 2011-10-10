package com.eyebrowssoftware.bptrackerfree;

import java.text.DateFormat;
import java.util.Date;

import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.widget.Spinner;
import android.widget.Toast;

public class BPTrackerFree extends Application {
	
	@SuppressWarnings("unused")
	private static final String TAG = "BPTrackerFree";
	
	public static final boolean FREE_VERSION = true;
	
	public static final int SYSTOLIC_MAX_DEFAULT = 280;
	public static final int SYSTOLIC_DEFAULT = 120;
	public static final int SYSTOLIC_MIN_DEFAULT = 20;
	public static final int DIASTOLIC_MAX_DEFAULT = 280;
	public static final int DIASTOLIC_DEFAULT = 70;
	public static final int DIASTOLIC_MIN_DEFAULT = 20;
	public static final int PULSE_MAX_DEFAULT = 200;
	public static final int PULSE_DEFAULT = 75;
	public static final int PULSE_MIN_DEFAULT = 40;
	
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
	public static final int COLUMN_ID_INDEX = 0;
	public static final int COLUMN_SYSTOLIC_INDEX = 1;
	public static final int COLUMN_DIASTOLIC_INDEX = 2;
	public static final int COLUMN_PULSE_INDEX = 3;
	public static final int COLUMN_CREATED_AT_INDEX = 4;
	public static final int COLUMN_MODIFIED_AT_INDEX = 5;
	public static final int COLUMN_NOTE_INDEX = 6;

	// Menu item ids
	public static final int MENU_ITEM_DELETE = Menu.FIRST;
	public static final int MENU_ITEM_EDIT = Menu.FIRST + 1;
	public static final int MENU_ITEM_SEND = Menu.FIRST + 2;
	public static final int MENU_DATA_MANAGER = Menu.FIRST + 3;
	public static final int MENU_ITEM_REVERT = Menu.FIRST + 4;
	public static final int MENU_ITEM_DONE = Menu.FIRST + 5;
	public static final int MENU_ITEM_CANCEL = Menu.FIRST + 6;
	public static final int MENU_ITEM_DISCARD = Menu.FIRST + 7;
	
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
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mShortDateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
		mMediumDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
		mShortTimeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
		mMediumTimeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);
	}
	
	public static void logErrorAndToast(Context context, String tag, int strResource) {
		Toast.makeText(context, strResource, Toast.LENGTH_LONG).show();
		Log.e(tag, context.getString(strResource));
	}

	public static void logErrorAndToast(Context context, String tag, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
		Log.e(tag, msg);
	}


}
