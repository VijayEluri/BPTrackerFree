/*
 * Copyright 2010 - Brion Noble Emde
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.eyebrowssoftware.bptrackerfree;

import java.text.DateFormat;
import java.util.Date;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * @author brionemde
 *
 */
public class BPTrackerFree extends Application {

    @SuppressWarnings("unused")
    private static final String TAG = "BPTrackerFree";

    /**
     *
     */
    public static final boolean FREE_VERSION = true;

    /**
     *
     */
    public static final String MURI = "sUri";

    /**
     *
     */
    public static final int SYSTOLIC_MAX_DEFAULT = 280;
    /**
     *
     */
    public static final int SYSTOLIC_DEFAULT = 120;
    /**
     *
     */
    public static final int SYSTOLIC_MIN_DEFAULT = 20;
    /**
     *
     */
    public static final int DIASTOLIC_MAX_DEFAULT = 280;
    /**
     *
     */
    public static final int DIASTOLIC_DEFAULT = 70;
    /**
     *
     */
    public static final int DIASTOLIC_MIN_DEFAULT = 20;
    /**
     *
     */
    public static final int PULSE_MAX_DEFAULT = 200;
    /**
     *
     */
    public static final int PULSE_DEFAULT = 75;
    /**
     *
     */
    public static final int PULSE_MIN_DEFAULT = 40;

    /**
     * Min difference between Systolic and Diastolic or between Max and Min
     * values of anything
     */
    public static final int MIN_RANGE = 5;

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
     * @param s
     * @param value
     */
    public static void setSpinner(Spinner s, int value) {
        RangeAdapter sa = (RangeAdapter) s.getAdapter();
        s.setSelection(sa.getPosition(value));
        sa.notifyDataSetChanged();
    }

    /**
     *
     * @param date
     * @param length
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
     * @param date
     * @param length
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

    /**
     * @param datetime
     * @param length
     * @return date string
     */
    public static String getDateString(long datetime, int length) {
        return getDateString(new Date(datetime), length);
    }

    /**
     * @param datetime
     * @param length
     * @return time string
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
     * @param context
     * @param tag
     * @param strResource
     */
    public static void logErrorAndToast(Context context, String tag, int strResource) {
        Toast.makeText(context, strResource, Toast.LENGTH_LONG).show();
        Log.e(tag, context.getString(strResource));
    }

    /**
     * @param context
     * @param tag
     * @param msg
     */
    public static void logErrorAndToast(Context context, String tag, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        Log.e(tag, msg);
    }

    /**
     * Shared Preferences Key
     */
    public static final String DEFAULT_SYSTOLIC_KEY = "systolic_default";
    /**
     * Shared Preferences Key
     */
    public static final String DEFAULT_DIASTOLIC_KEY = "diastolic_default";
    /**
     * Shared Preferences Key
     */
    public static final String DEFAULT_PULSE_KEY = "pulse_default";
    /**
     * Shared Preferences Key
     */
    public static final String AVERAGE_VALUES_KEY = "average_values_checkbox";
}
