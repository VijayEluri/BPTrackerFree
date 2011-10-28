package com.eyebrowssoftware.bptrackerfree.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.eyebrowssoftware.bptrackerfree.R;
import com.eyebrowssoftware.bptrackerfree.fragments.BPRecordEditorFragment;
import com.eyebrowssoftware.bptrackerfree.fragments.BPRecordEditorSpinnerFragment;

/**
 * Activity for Spinner-based version of the editor.
 * 
 * @author brione
 *
 */
public class BPRecordEditor extends FragmentActivity  implements BPRecordEditorFragment.Callback {
	private static final String TAG = "BPRecordEditor";

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Intent intent = getIntent();
		Uri uri = intent.getData();
		String action = intent.getAction();
		
		setContentView(R.layout.bp_record_editor);

		if (Intent.ACTION_EDIT.equals(action)) {
			setTitle(getText(R.string.title_edit));
		} else if (Intent.ACTION_INSERT.equals(action)) {
			setTitle(getText(R.string.title_create));
		} else {
			Log.e(TAG, "Unknown action, exiting");
			finish();
			return;
		}
		
		BPRecordEditorSpinnerFragment mEditorFragment = new BPRecordEditorSpinnerFragment();
		Bundle args = new Bundle();
		args.putString(BPRecordEditorFragment.DATA_KEY, uri.toString());
		args.putString(BPRecordEditorFragment.ACTION_KEY, action);
		mEditorFragment.setArguments(args);
		
		// Pass the action and data Uri to the fragment via fragment arguments
		this.getSupportFragmentManager().beginTransaction().add(R.id.editor_fragment_container, mEditorFragment).commit();
	}
	
	public void onEditComplete(int status) {
		Log.i(TAG, "Got an onEditComplete(" + status + ")");
		finish();
	}
}
