package com.eyebrowssoftware.bptrackerfree;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;

/**
 * Adds checkable behavior to a linear layout, for some reason :-)
 * 
 * @author brionemde
 *
 */
public class CheckableLinearLayout extends LinearLayout implements Checkable {
	@SuppressWarnings("unused")
	private static final String TAG = "CheckableLinearLayout";
	
	private boolean mChecked = false;
	
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
		return mChecked;
	}

	public void setChecked(boolean checked) {
		mChecked = checked;
        setBackgroundDrawable(checked ? new ColorDrawable(0xff0000a0) : null);
	}

	public void toggle() {
		setChecked(!mChecked);
	}

}
