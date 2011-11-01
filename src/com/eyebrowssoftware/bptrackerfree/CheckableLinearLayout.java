package com.eyebrowssoftware.bptrackerfree;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Checkable;
import android.widget.LinearLayout;

/**
 * Adds checkable behavior to a linear layout, for some reason :-)
 * 
 * @author brionemde
 *
 */
public class CheckableLinearLayout extends LinearLayout implements Checkable {
	private static final String TAG = "CheckableLinearLayout";
	
	/**
	 * Vanilla constructor
	 * 
	 * @param context
	 */
	public CheckableLinearLayout(Context context) {
		super(context);
	}


	/**
	 * Macho Constructor
	 * 
	 * @param context
	 * @param attrs
	 */
	public CheckableLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Muy Macho Constructor
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public CheckableLinearLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public boolean isChecked() {
		Log.i(TAG, "isChecked()");
		return false;
	}

	public void setChecked(boolean checked) {
		Log.i(TAG, "setChecked(" + (checked ? "true" : "false") + ")");

	}

	public void toggle() {
		Log.i(TAG, "toggle()");
	}

}
