package com.eyebrowssoftware.bptrackerfree;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class CheckableLinearLayout extends LinearLayout implements Checkable {
	private static final String TAG = "CheckableLinearLayout";
	
	public CheckableLinearLayout(Context context) {
		super(context);
	}


	public CheckableLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

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
