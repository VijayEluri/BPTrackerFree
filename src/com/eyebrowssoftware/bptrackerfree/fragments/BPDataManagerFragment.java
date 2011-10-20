package com.eyebrowssoftware.bptrackerfree.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.eyebrowssoftware.bptrackerfree.R;
import com.eyebrowssoftware.bptrackerfree.content.BPRecords;

public class BPDataManagerFragment extends Fragment implements OnClickListener, AlertDialogFragment.Callback {

	public interface Callback {
		
		void onDataManagerComplete(int status);
	}
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View layout = inflater.inflate(R.layout.bp_data_manager_fragment, container, false);
		
		Button deleteButton = (Button) layout.findViewById(R.id.delete_button);
		deleteButton.setOnClickListener(this);
		return layout;
	}
	
	private static final int TARGET_CODE = 247;
	
	public void onClick(View v) {
		AlertDialogFragment diagFrag = AlertDialogFragment.getNewInstance(R.string.msg_delete, R.string.label_yes, R.string.label_no);
		diagFrag.setTargetFragment(this, TARGET_CODE);
		diagFrag.show(this.getFragmentManager(), "delete");
	}
	
	private void deleteHistory() {
		Activity activity = this.getActivity();
		int deleted = activity.getContentResolver().delete(BPRecords.CONTENT_URI, null, null);
		Toast.makeText(activity, String.format(getString(R.string.msg_deleted), deleted), Toast.LENGTH_LONG).show();
	}

	private void complete(int status) {
		Fragment frag = this.getTargetFragment();
		if(frag != null) {
			((BPDataManagerFragment.Callback) frag).onDataManagerComplete(status);
		} else {
			((BPDataManagerFragment.Callback) this.getActivity()).onDataManagerComplete(status);
		}
	}
	
	public void onNegativeButtonClicked() {
		this.complete(Activity.RESULT_CANCELED);
	}

	public void onPositiveButtonClicked() {
		this.deleteHistory();
		this.complete(Activity.RESULT_CANCELED);
	}

}
