package com.eyebrowssoftware.bptrackerfree;

import java.lang.ref.WeakReference;
import java.text.DateFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;

public class BPListFragment extends ListFragment implements OnClickListener {
	
	private static final String TAG = "BPListFragment";

	private static final String[] PROJECTION = { 
		BPRecord._ID,
		BPRecord.SYSTOLIC, 
		BPRecord.DIASTOLIC, 
		BPRecord.PULSE,
		BPRecord.CREATED_DATE,
		BPRecord.NOTE 
	};

	// private static final int COLUMN_ID_INDEX = 0;
	// private static final int COLUMN_SYSTOLIC_INDEX = 1;
	// private static final int COLUMN_DIASTOLIC_INDEX = 2;
	// private static final int COLUMN_PULSE_INDEX = 3;
	private static final int COLUMN_CREATED_AT_INDEX = 4;
	// private static final int COLUMN_NOTE_INDEX = 5;

	// Menu item ids
	public static final int MENU_ITEM_DELETE = Menu.FIRST;
	public static final int MENU_ITEM_EDIT = Menu.FIRST + 1;
	public static final int MENU_ITEM_SEND = Menu.FIRST + 2;
	public static final int MENU_DATA_MANAGER = Menu.FIRST + 3;
	
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
		R.id.sys_value,
		R.id.dia_value, 
		R.id.pulse_value,
		R.id.note
	};
	
	private static final String CONTEXT_URI = "context_uri";
	private static final String SELECTION = "selection";
	
	// The state that needs to be saved and stored
	private int mCurrentCheckPosition = 0;
	private long mContextMenuRecordId = 0;

	private MyAsyncQueryHandler mMAQH;
	
	private static final int BPRECORDS_TOKEN = 0;
	
	boolean mDualPane = false;
	Uri mStartUri;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState) {
		
		// Inflate the layout for this fragment
		View layout = inflater.inflate(R.layout.bp_list_fragment, container, false);
		RelativeLayout mEmptyContent = (RelativeLayout) layout.findViewById(R.id.empty_content);
		mEmptyContent.setOnClickListener(this);
		
		ListView lv = (ListView) layout.findViewById(android.R.id.list);

		View v = inflater.inflate(R.layout.bp_record_list_header, null);
		lv.addHeaderView(v, null, true);

		lv.setOnCreateContextMenuListener(this);

		return layout;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
	
	/** Called when the fragment is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		// No cursor yet. Will be assigned when the asynchronous query is complete
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), R.layout.bp_record_list_item, null, VALS, IDS);
		adapter.setViewBinder(new MyViewBinder());
		setListAdapter(adapter);

		mMAQH = new MyAsyncQueryHandler(getActivity().getContentResolver(), adapter);
		mMAQH.startQuery(BPRECORDS_TOKEN, this, mStartUri, PROJECTION, null, null, BPRecord.CREATED_DATE + " DESC");
		
        // Check to see if we have a frame in which to embed the details
        // fragment directly in the containing UI.
        View detailsFrame = getActivity().findViewById(R.id.details_fragment);
        mDualPane = detailsFrame != null
                && detailsFrame.getVisibility() == View.VISIBLE;

        if (mDualPane) {
            // In dual-pane mode, list view highlights selected item.
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            // Make sure our UI is in the correct state.
            showDetails(mCurrentCheckPosition);
        }
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		showDetails(position);
	}
	
	void showDetails(int position) {
		Uri data = mStartUri;
		long id = this.getListAdapter().getItemId(position);
		if(mDualPane) {
			getListView().setItemChecked(position, true);
			
			// Check which fragment is shown and get the editor 
			if(id < 0) {
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
		} else {
			if(id < 0) {
				if (data == null) {
					data = BPRecords.CONTENT_URI; //TODO: getIntent().getData();
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
		Uri data = BPRecords.CONTENT_URI; //TODO: getIntent().getData();
		startActivity(new Intent(Intent.ACTION_INSERT, data));
	}
	
	private class MyViewBinder implements SimpleCursorAdapter.ViewBinder {
		String val;

		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			int id = view.getId();

			switch (id) {
			case R.id.sys_value:
			case R.id.dia_value:
			case R.id.pulse_value: // Pulse
				((TextView) view).setText(String.valueOf(cursor.getInt(columnIndex)));
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
	
	private class MyAsyncQueryHandler extends AsyncQueryHandler {

		private WeakReference<SimpleCursorAdapter> mAdapter;
		
		public MyAsyncQueryHandler(ContentResolver cr, SimpleCursorAdapter adapter) {
			super(cr);
			mAdapter = new WeakReference<SimpleCursorAdapter>(adapter);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if (cursor != null) {
				getActivity().startManagingCursor(cursor);
				SimpleCursorAdapter adapter = mAdapter.get();
				if(adapter != null) {
					adapter.changeCursor(cursor);
				}
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(CONTEXT_URI, mContextMenuRecordId);
		outState.putInt(SELECTION, mCurrentCheckPosition);
	}

	@Override
	public void onDestroy() {
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) this.getListAdapter();
		this.setListAdapter(null);
		if(adapter != null) {
			adapter.changeCursor(null);
		}
		super.onDestroy();
	}


	@Override
	public void onDetach() {
		super.onDetach();
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
			long datetime = cursor.getLong(COLUMN_CREATED_AT_INDEX);
			String date = BPTrackerFree.getDateString(datetime, DateFormat.SHORT);
			String time = BPTrackerFree.getTimeString(datetime, DateFormat.SHORT);
			String fmt = getString(R.string.datetime_format);
			menu.setHeaderTitle(String.format(fmt, date, time));
		}
		// Add a menu item to edit the record
		menu.add(Menu.NONE, MENU_ITEM_EDIT, 0, R.string.menu_edit);
		// Add a menu item to delete the record
		menu.add(Menu.NONE, MENU_ITEM_DELETE, 1, R.string.menu_delete);
		// Add a menu item to send the record
		menu.add(Menu.NONE, MENU_ITEM_SEND, 2, R.string.menu_send);
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
			case MENU_ITEM_DELETE:
				DeleteDialogFragment diagFrag = new DeleteDialogFragment();
				diagFrag.show(this.getFragmentManager(), "delete");
				return true;
			case MENU_ITEM_EDIT:
				startActivity(new Intent(Intent.ACTION_EDIT, contextMenuUri));
				return true;
			case MENU_ITEM_SEND:
				startActivity(new Intent(Intent.ACTION_SEND, contextMenuUri, getActivity(), BPSend.class));
				return true;
			}
			return false;
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
			return false;
		}

	}
	
	private void deleteRecord() {
		Uri contextMenuUri = ContentUris.withAppendedId(mStartUri, mContextMenuRecordId);
		getActivity().getContentResolver().delete(contextMenuUri, null, null);
	}
	
	private class DeleteDialogFragment extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceData) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(getString(R.string.really_delete))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.label_yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						deleteRecord();
					}
				})
				.setNegativeButton(getString(R.string.label_no), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
			return builder.create();
		}

	}

}
