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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * @author brionemde
 *
 */
public class RangeAdapter extends BaseAdapter {
    private static final String TAG = "RangeAdapter";

    /**
     *
     */
    public static final int MAX_IDX = 0;
    /**
     *
     */
    public static final int RED_IDX = 1;
    /**
     *
     */
    public static final int ORANGE_IDX = 2;
    /**
     *
     */
    public static final int BLUE_IDX = 3;
    /**
     *
     */
    public static final int MIN_IDX = 4;
    /**
     *
     */
    public static final int ARRAY_SIZE = MIN_IDX + 1;

    private Context mContext;

    private int mUpper;
    private int mRed;
    private int mOrange;
    private int mBlue;
    private int mLower;

    private int mResId = -1;
    private int mTextId = -1;

    private LayoutInflater mLI;

    /**
     *
     */
    public static final int NO_ZONE = -1;

    private boolean mReverse = false;

    /**
     * @param context
     *            - the enclosing context
     *
     *            The values array and corresponding ranges go like this:
     *            max --> red --> orange --> blue --> min
     * @param values
     *            - values[MIN_IDX]: minimum value values[ORANGE_IDX]: value that starts the
     *            orange zone, which goes up to the red zone, if == NO_ZONE then
     *            no orange zone values[2]: value that starts the red zone,
     *            which stretches to the max value, if == NO_ZONE then no red
     *            zone values[3]: maximum value
     * @param reverse
     *            - if true, reverse the order the range is displayed
     * @param textViewResourceId
     *            - the resource Id of the TextView to display the result
     */
    public RangeAdapter(Context context, int[] values, boolean reverse,
            int textViewResourceId) {
        super();
        init(context, values, reverse);
        if (textViewResourceId > 0)
            mTextId = textViewResourceId;
        else
            throw new IllegalArgumentException("Resource must be real");
    }

    /**
     * @param context
     *            - the enclosing context
     *
     *            The values array and corresponding ranges go like this:
     *            max --> red --> orange --> blue --> min
     * @param values
     *            - values[MIN_IDX]: minimum value values[ORANGE_IDX]: value that starts the
     *            orange zone, which goes up to the red zone, if == NO_ZONE then
     *            no orange zone values[2]: value that starts the red zone,
     *            which stretches to the max value, if == NO_ZONE then no red
     *            zone values[3]: maximum value
     * @param reverse
     *            - if true, reverse the order the range is displayed
     * @param resourceId
     *            - the resource Id of the view layout that holds the text view
     * @param textViewResourceId
     *            - the resource Id of the TextView to display the result
     */
    public RangeAdapter(Context context, int[] values, boolean reverse,
            int resourceId, int textViewResourceId) {
        super();
        init(context, values, reverse);
        if (resourceId > 0)
            mResId = resourceId;
        else
            throw new IllegalArgumentException("Resource must be real");
        if (textViewResourceId > 0)
            mTextId = textViewResourceId;
        else
            throw new IllegalArgumentException("Resource must be real");
    }

    private void init(Context context, int[] values, boolean reverse) {
        mContext = context;
        mReverse = reverse;
        setValues(values);
        mLI = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * @return the array of values
     */
    public int[] getValues() {
        int values[] = new int[ARRAY_SIZE];
        values[MIN_IDX] = mLower;
        values[BLUE_IDX] = mBlue;
        values[ORANGE_IDX] = mOrange;
        values[RED_IDX] = mRed;
        values[MAX_IDX] = mUpper;
        return values;
    }

    /**
     * @param values
     * @return this
     */
    public RangeAdapter setValues(int[] values) {
        if(values != null && values.length == ARRAY_SIZE) {
            mLower = values[MIN_IDX];
            mBlue = values[BLUE_IDX];
            mOrange = values[ORANGE_IDX];
            mRed = values[RED_IDX];
            mUpper = values[MAX_IDX];
        } else {
            mUpper = mRed = mOrange = 100;
            mBlue = mLower = 0;
        }
        return this;
    }

    /**
     * @param max
     * @param red
     * @param orange
     * @param blue
     * @param min
     * @return this
     */
    public RangeAdapter setValues(int max, int red, int orange, int blue, int min) {
        mLower = min;
        mBlue = blue;
        mOrange = orange;
        mRed = red;
        mUpper = max;
        return this;
    }

    /**
     * @return true if reversed, false if normal
     */
    public boolean isReverse() {
        return mReverse;
    }

    /**
     * @param mReverse
     */
    public void setIsReverse(boolean mReverse) {
        this.mReverse = mReverse;
    }

    /**
     * @param value
     * @return return the current position
     */
    public int getPosition(int value) {
        if(value < mLower || value > mUpper) {
            throw new IllegalArgumentException(TAG + ": Value out of range");
        }
        return (!mReverse) ? value - mLower : mUpper - value;
    }

    public int getCount() {
        return mUpper - mLower + 1;
    }

    public Integer getItem(int pos) {
        return (!mReverse) ? Integer.valueOf(pos + mLower) : Integer
                .valueOf(mUpper - pos);
    }

    public long getItemId(int pos) {
        return (!mReverse) ? pos + mLower : mUpper - pos;
    }

    public View getView(int position, View convertView, ViewGroup parentView) {

        TextView tv;

        if (convertView == null) {
            if (mResId <= 0) {
                convertView = tv = new TextView(mContext);
                convertView.setId(mTextId);
            } else {
                convertView = mLI.inflate(mResId, null);
                tv = (TextView) convertView.findViewById(mTextId);
            }
        } else {
            if (mResId <= 0) {
                tv = (TextView) convertView;
            } else {
                tv = (TextView) convertView.findViewById(mTextId);
            }
        }
        if (tv != null) {
            int value = (Integer) this.getItem(position);
            int color;
            if (mRed != NO_ZONE && value > mRed) {
                color = R.color.red_text_background;
            } else if (mOrange != NO_ZONE && value > mOrange) {
                color = R.color.orange_text_background;
            } else if (mBlue != NO_ZONE && value < mBlue) {
                color = R.color.blue_text_background;
            } else {
                color = R.color.normal_text_background;
            }
            tv.setText(String.valueOf(this.getItem(position)));
            tv.setBackgroundResource(color);
        }
        return convertView;
    }

}
