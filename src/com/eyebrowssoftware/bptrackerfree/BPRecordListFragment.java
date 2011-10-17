package com.eyebrowssoftware.bptrackerfree;

import java.text.DateFormat;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;

public class BPRecordListFragment extends ListFragment implements OnClickListener, 
		LoaderManager.LoaderCallbacks<Cursor>, BPSendFragment.Callback, BPRecordEditorFragment.Callback,
		BPDataManagerFragment.Callback, AlertDialogFragment.Callback {
	
	private static final String TAG = "BPListFragment";

	private static final String[] VALS = { 
		BPRecord.CREATED_DATE,
		BPRecord.CREATED_DATE, 
		BPRecord.SYSTOLIC, 
		BPRecord.DIASTOLIC,
		BPRecord.PULSE,
		BPRecord.NOTE
	};

	private static final int[] IDS = { 
		R.id.date, 
		R.id.time, 
		R.id.bp_value,
		R.id.dia_value, 
		R.id.pulse_value,
		R.id.note
	};
	
	private static final String CONTEXT_URI = "context_uri";
	private static final String SELECTION = "selection";
	
	// The state that needs to be saved and stored
	private int mCurrentCheckPosition = 0;
	private long mContextMenuRecordId = 0;
	
	private static final int LIST_LOADER_ID = 0;

	boolean mDualPane = false;
	
	private static final int DUAL_PANE_STATE_EMPTY = -1;
	@SuppressWarnings("unused")
	private static final int DUAL_PANE_STATE_EDIT = 0;
	@SuppressWarnings("unused")
	private static final int DUAL_PANE_STATE_INSERT = 1;
	@SuppressWarnings("unused")
	private static final int DUAL_PANE_STATE_SEND = 2;
	@SuppressWarnings("unused")
	private static final int DUAL_PANE_STATE_DATA = 3;

	@SuppressWarnings("unused")
	private int mDualPaneState = DUAL_PANE_STATE_EMPTY;
	
	private Uri mStartUri;
	private LinearLayout mEmptyControls;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState) {
		
		// Inflate the layout for this fragment
		View layout = inflater.inflate(R.layout.bp_record_list_fragment, container, false);

		RelativeLayout mEmptyContent = (RelativeLayout) layout.findViewById(R.id.empty_content_id);
		mEmptyContent.setOnClickListener(this);

		mEmptyControls = (LinearLayout) layout.findViewById(R.id.empty_controls_id);

		return layout;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		this.getListView().setOnCreateContextMenuListener(null);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if(savedInstanceState != null) {
			mContextMenuRecordId = savedInstanceState.getLong(CONTEXT_URI);
			mCurrentCheckPosition = savedInstanceState.getInt(SELECTION);
		}
		Intent intent = getActivity().getIntent();
		mStartUri = intent.getData();
		if(mStartUri == null) {
			mStartUri = BPRecords.CONTENT_URI;
		}
		// No cursor yet. Will be assigned when the CursorLoader query is complete
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), 
				R.layout.bp_record_list_item, null,	VALS, IDS, 0);
		
		adapter.setViewBinder(new MyViewBinder());
		View v = this.getLayoutInflater(null).inflate(R.layout.bp_record_list_header, null);

		ListView lv = this.getListView();
		lv.addHeaderView(v, null, true);
		lv.setOnCreateContextMenuListener(this);
		
		this.setListAdapter(adapter);

		// Set up our cursor loader. It manages the cursors from now on
		this.getLoaderManager().initLoader(LIST_LOADER_ID, null, this);
		
        // Check to see if we have a frame in which to embed the details
        // fragment directly in the containing UI.
        View detailsFrame = getActivity().findViewById(R.id.details_fragment);
        mDualPane = detailsFrame != null
                && detailsFrame.getVisibility() == View.VISIBLE;

        if (mDualPane) {
            // In dual-pane mode, list view highlights selected item.
            lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		showDetails(position);
	}
	
	private void showDetails(int position) {
		
		mCurrentCheckPosition = position;
		
		Uri data = BPRecords.CONTENT_URI;
		long id = getListView().getItemIdAtPosition(position);
		
		if(mDualPane) {
			getListView().setItemChecked(position, true);
			mCurrentCheckPosition = position;
			
			FragmentManager fMgr = this.getFragmentManager();
			// Show the correct fragment 
			BPRecordEditorFragment mEditorFragment;
			if(id < 0) {
				mEditorFragment = BPRecordEditorTextFragment.newInstance(data, Intent.ACTION_INSERT);
			} else {
				Uri uri = ContentUris.withAppendedId(data, id);
				mEditorFragment = BPRecordEditorTextFragment.newInstance(uri, Intent.ACTION_EDIT);
			}
			mEditorFragment.setTargetFragment(this, 128);
			fMgr.beginTransaction().replace(R.id.details_fragment, mEditorFragment).commit();

		} else {
			if(id < 0) {
				if (data == null) {
					data = BPRecords.CONTENT_URI;
				}
				startActivity(new Intent(Intent.ACTION_INSERT, data));
			} else {
				Uri uri = ContentUris.withAppendedId(data, id);
				String action = getActivity().getIntent().getAction();
				if (Intent.ACTION_PICK.equals(action)
						|| Intent.ACTION_GET_CONTENT.equals(action)) {
					// The caller is waiting for us to return a note selected by
					// the user. The have clicked on one, so return it now.
					getActivity().setResult(Activity.RESULT_OK, new Intent().setData(uri));
				} else {
					// Launch activity to view/edit the currently selected item
					startActivity(new Intent(Intent.ACTION_EDIT, uri));
				}
			}
		}
	}

	public void onClick(View v) {
		Uri data = BPRecords.CONTENT_URI;
		startActivity(new Intent(Intent.ACTION_INSERT, data));
	}
	
	private class MyViewBinder implements SimpleCursorAdapter.ViewBinder {
		String val;

		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			int id = view.getId();

			switch (id) {
			case R.id.bp_value:
				int sys = cursor.getInt(columnIndex);
				int dia = cursor.getInt(BPTrackerFree.COLUMN_DIASTOLIC_INDEX);
				String bp = String.valueOf(sys) + "/" + String.valueOf(dia); 
				((TextView) view).setText(bp);
				return true;
			case R.id.pulse_value: // Pulse
				((TextView) view).setText(String.valueOf(cursor.getInt(columnIndex)));
				return true;
			case R.id.dia_value:
				// do nothing
				return true;
			case R.id.date: // Date
			case R.id.time: // Time -- these use the same cursor column
				long datetime = cursor.getLong(columnIndex);
				val = (id == R.id.date) ? BPTrackerFree.getDateString(datetime, DateFormat.SHORT) 
						: BPTrackerFree.getTimeString(datetime, DateFormat.SHORT);
				((TextView)view).setText(val);
				return true;
			case R.id.note:
				String note = cursor.getString(columnIndex);
				if(note != null && note.length() > 0) {
					((TextView)view).setText(note);
					view.setVisibility(View.VISIBLE);
				} else {
					view.setVisibility(View.GONE);
				}
				return true;
			default:
				return false;
			}
		}
	}
	
	/**
	 * Called when the Loader is created
	 */
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// Create a CursorLoader that will take care of creating a cursor for the data
		CursorLoader loader = new CursorLoader(getActivity(), mStartUri, 
				BPTrackerFree.PROJECTION, null, null, BPRecord.DEFAULT_SORT_ORDER);
		return loader;
	}

	/**
	 * Called when the load of the cursor is finished
	 */
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if(cursor.getCount() == 0) {
			this.mEmptyControls.setVisibility(View.VISIBLE);
		} else {
			this.mEmptyControls.setVisibility(View.GONE);
		}
		((SimpleCursorAdapter) this.getListAdapter()).swapCursor(cursor);
	}

	/**
	 * Called when the loader is reset, we swap out the current cursor with null
	 */
	public void onLoaderReset(Loader<Cursor> arg0) {
		((SimpleCursorAdapter) this.getListAdapter()).swapCursor(null);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(CONTEXT_URI, mContextMenuRecordId);
		outState.putInt(SELECTION, mCurrentCheckPosition);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}


	private void doSendAction() {
		this.startActivity(new Intent(Intent.ACTION_SEND, BPRecords.CONTENT_URI, this.getActivity(), BPSend.class));
	}
	
	private void doDataManagerAction() {
		this.startActivity(new Intent(this.getActivity(), BPDataManager.class));
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.bp_record_list_fragment_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_send:
			doSendAction();
			return true;
		case R.id.menu_data:
			doDataManagerAction();
			return true;
		default:
			return false;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		
		AdapterView.AdapterContextMenuInfo info;
		
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
			return;
		}
		Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
		if (cursor != null && cursor.moveToFirst()) {
			long datetime = cursor.getLong(BPTrackerFree.COLUMN_CREATED_AT_INDEX);
			String date = BPTrackerFree.getDateString(datetime, DateFormat.SHORT);
			String time = BPTrackerFree.getTimeString(datetime, DateFormat.SHORT);
			String fmt = getString(R.string.datetime_format);
			menu.setHeaderTitle(String.format(fmt, date, time));
		}
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.bp_record_list_fragment_context_menu, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
			// Get the Uri of the record we're intending to operate on
			mContextMenuRecordId = info.id;
			Uri contextMenuUri = ContentUris.withAppendedId(mStartUri, mContextMenuRecordId);
			switch (item.getItemId()) {
			case R.id.menu_delete:
				AlertDialogFragment diagFrag = AlertDialogFragment.getNewInstance(R.string.msg_delete, R.string.label_yes, R.string.label_no);
				diagFrag.show(this.getFragmentManager(), "delete");
				return true;
			case R.id.menu_edit:
				startActivity(new Intent(Intent.ACTION_EDIT, contextMenuUri));
				return true;
			case R.id.menu_send:
				startActivity(new Intent(Intent.ACTION_SEND, contextMenuUri, getActivity(), BPSend.class));
				return true;
			}
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
		}
		return super.onContextItemSelected(item);
	}
	
	private void deleteRecord() {
		Uri contextMenuUri = ContentUris.withAppendedId(mStartUri, mContextMenuRecordId);
		getActivity().getContentResolver().delete(contextMenuUri, null, null);
	}
	
	public void onSendComplete(int status) {
		Log.i(TAG, "onSendComplete called with status: " + status);
		showDetails(mCurrentCheckPosition);
	}

	public void onEditComplete(int status) {
		Log.i(TAG, "onEditComplete called with status: " + status);
		showDetails(mCurrentCheckPosition);
	}

	public void onDataManagerComplete(int status) {
		Log.i(TAG, "onEditComplete called with status: " + status);
		// XXX: do something here
	}

	public void onNegativeButtonClicked() {
		// nothing to do, dialog is cancelled already
	}

	public void onPositiveButtonClicked() {
		deleteRecord();
	}
}
