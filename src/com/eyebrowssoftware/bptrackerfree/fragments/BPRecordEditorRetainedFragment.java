package com.eyebrowssoftware.bptrackerfree.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.eyebrowssoftware.bptrackerfree.BPTrackerFree;
import com.eyebrowssoftware.bptrackerfree.content.BPRecords.BPRecord;

/**
 * Provide state transition continuity between insert instances of the editor fragment across discrete activities.
 * @author Brion Emde
 *
 */
public class BPRecordEditorRetainedFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	@SuppressWarnings("unused")
	private static final String TAG = "BPRecordInsertRetainedFragment";
	
	private int mState;
	private Uri mUri;
	private Bundle mOriginalValues;
	
	static BPRecordEditorRetainedFragment newInstance(int state, Uri uri) {
		return new BPRecordEditorRetainedFragment(state, uri);
	}
	
	/**
	 * Constructor
	 * 
	 * @param state 
	 * @param uri
	 * @param originalValues
	 *  
	 */
	public BPRecordEditorRetainedFragment(int state, Uri uri) {
		super();

		mState = state;
		mUri = uri;
	}

    /**
     * Fragment initialization.  We want to be retained.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        setRetainInstance(true);
    }
    
    private static final int EDITOR_STATE_LOADER_ID = 8235;

    /**
     * Confirm that our Uri is a real record by querying it
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
		this.getLoaderManager().initLoader(EDITOR_STATE_LOADER_ID, null, this);
    }

    /**
     * This is called when the fragment is going away.  It is NOT called
     * when the fragment is being propagated between activity instances.
     */
    @Override
    public void onDestroy() {
    	setUri(null);
    	setOriginalValues(null);
    	setState(-1);
        super.onDestroy();
    }

	/**
	 * Called when the Loader is created
	 */
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// Create a CursorLoader that will take care of creating a cursor for the data
		CursorLoader loader = new CursorLoader(getActivity(), mUri,	BPTrackerFree.PROJECTION, null, null, null);
		return loader;
	}

	/**
	 * Called when the load of the cursor is finished
	 */
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mOriginalValues = new Bundle();
		if(cursor != null && cursor.moveToFirst()) {
			mOriginalValues.putInt(BPRecord.SYSTOLIC, cursor.getInt(BPTrackerFree.COLUMN_SYSTOLIC_INDEX));
			mOriginalValues.putInt(BPRecord.DIASTOLIC, cursor.getInt(BPTrackerFree.COLUMN_DIASTOLIC_INDEX));
			mOriginalValues.putInt(BPRecord.PULSE, cursor.getInt(BPTrackerFree.COLUMN_PULSE_INDEX));
			mOriginalValues.putLong(BPRecord.CREATED_DATE, cursor.getLong(BPTrackerFree.COLUMN_CREATED_AT_INDEX));
			mOriginalValues.putLong(BPRecord.MODIFIED_DATE, cursor.getLong(BPTrackerFree.COLUMN_MODIFIED_AT_INDEX));
			mOriginalValues.putString(BPRecord.NOTE, cursor.getString(BPTrackerFree.COLUMN_NOTE_INDEX));
		}
	}

	/**
	 * Called when the loader is reset
	 */
	public void onLoaderReset(Loader<Cursor> loader) {
		// Nothing to do
	}
	

	/**
	 * Return the retained state
	 * 
	 * @return the retrained state
	 */
	public int getState() {
		return mState;
	}

	/**
	 * Set the retained Uri
	 * 
	 * @param state
	 */
	public void setState(int state) {
		this.mState = state;
	}
	
	/**
	 * Return the retained Uri
	 * 
	 * @return the retrained Uri
	 */
	public Uri getUri() {
		return this.mUri;
	}

	/**
	 * Set the retained Uri
	 * 
	 * @param uri
	 */
	public void setUri(Uri uri) {
		this.mUri = uri;
	}
	
	
	/**
	 * Return the retained Bundle
	 * 
	 * @return the retrained Bundle
	 */
	public Bundle getOriginalValues() {
		return this.mOriginalValues;
	}

	/**
	 * Set the retained Bundle
	 * 
	 * @param originalValues
	 */
	public void setOriginalValues(Bundle originalValues) {
		this.mOriginalValues = originalValues;
	}
}
