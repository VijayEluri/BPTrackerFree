package com.eyebrowssoftware.bptrackerfree;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.text.DateFormat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.csvreader.CsvWriter;
import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;

public class BPSendFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, 
		OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = "BPSendFragment";

	private static final String[] PROJECTION = { 
		BPRecord._ID,
		BPRecord.SYSTOLIC, 
		BPRecord.DIASTOLIC, 
		BPRecord.PULSE,
		BPRecord.CREATED_DATE,
		BPRecord.NOTE
	};

	private static final int COLUMN_ID_INDEX = 0;
	private static final int COLUMN_SYSTOLIC_INDEX = 1;
	private static final int COLUMN_DIASTOLIC_INDEX = 2;
	private static final int COLUMN_PULSE_INDEX = 3;
	private static final int COLUMN_CREATED_AT_INDEX = 4;
	private static final int COLUMN_NOTE_INDEX = 5;

	private Uri mUri;

	private TextView mMsgLabelView;
	private TextView mMsgView;
	private CheckBox mSendText;
	private CheckBox mSendFile;
	private Button mSendButton;
	private Button mCancelButton;

	private WeakReference<TextView> mWeakLabelView;
	private WeakReference<TextView> mWeakMsgView;

	private String mMsgLabelString;
	
	public static final boolean ALL_DATES = true;
	public static final String REVERSE = "reverse";

	// These are key names for saving things in the icicle
	private static final String SEND_TEXT = "tsend";
	private static final String SEND_FILE = "fsend";
	
	// This may or may not be used
	@SuppressWarnings("unused")
	private boolean mReverse = true;
	
	private static final int SEND_LOAD_ID = 2;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.bp_send_fragment, container, false);

		mMsgLabelView = (TextView) layout.findViewById(R.id.message_label);
		mWeakLabelView = new WeakReference<TextView>(mMsgLabelView);
		
		mMsgLabelString = getString(R.string.label_message_format);
		
		mMsgView = (TextView) layout.findViewById(R.id.message);
		mWeakMsgView = new WeakReference<TextView>(mMsgView);

		mSendText = (CheckBox) layout.findViewById(R.id.text);
		mSendText.setOnCheckedChangeListener(this);

		mSendFile = (CheckBox) layout.findViewById(R.id.attach);
		mSendFile.setOnCheckedChangeListener(this);

		mSendButton = (Button) layout.findViewById(R.id.send);
		mSendButton.setOnClickListener(this);

		mCancelButton = (Button) layout.findViewById(R.id.cancel);
		mCancelButton.setOnClickListener(this);

		return layout;
	}
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		this.setHasOptionsMenu(true);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Activity activity = this.getActivity();
		Intent intent = activity.getIntent();
		if (intent.getData() == null) {
			intent.setData(BPRecords.CONTENT_URI);
		}
		mReverse = intent.getBooleanExtra(REVERSE, true);

		mUri = intent.getData();
		
		if(savedInstanceState != null) {
			mSendText.setChecked(savedInstanceState.getBoolean(SEND_TEXT));
			mSendFile.setChecked(savedInstanceState.getBoolean(SEND_FILE));
		} else {
			mSendText.setChecked(true);
			mSendFile.setChecked(true);
		}
		this.getLoaderManager().initLoader(SEND_LOAD_ID, null, this);
	}
	
	public void onSaveInstanceState(Bundle icicle) {
		icicle.putBoolean(SEND_TEXT, mSendText.isChecked());
		icicle.putBoolean(SEND_FILE, mSendFile.isChecked());
	}
	
	
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// Create a CursorLoader that will take care of creating a cursor for the data
		CursorLoader loader = new CursorLoader(getActivity(), mUri,	PROJECTION, null, null, null);
		return loader;
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if(cursor != null) {
			TextView labelView = mWeakLabelView.get();
			TextView msgView = mWeakMsgView.get();
			if(labelView != null && msgView != null) {
				String msg = getMessage(cursor);
		        labelView.setText(String.format(mMsgLabelString, msg.length()));
		        msgView.setText(msg);
			}
		}
	}

	public void onLoaderReset(Loader<Cursor> cursor) {
		TextView labelView = mWeakLabelView.get();
		TextView msgView = mWeakMsgView.get();
		if(labelView != null && msgView != null) {
	        labelView.setText(null);
	        msgView.setText(null);
		}
	}
	
	public void onClick(View v) {
		Callback callback = (Callback) this.getActivity();
		if (v.equals(mSendButton)) {
			if (sendData()) {
				callback.onComplete(Activity.RESULT_OK);
			} else {
				Toast.makeText(getActivity(), R.string.msg_nothing_to_send, Toast.LENGTH_SHORT).show();
			}
		} else if (v.equals(mCancelButton)) {
			callback.onComplete(Activity.RESULT_CANCELED);
		}
	}
	
	private static final String FILENAME = "data.csv";
	private static final String MSGNAME = "bpdata.csv";

	private boolean sendData() {
		String msg = mMsgView.getText().toString();
		Activity activity = this.getActivity();
		
		// We're going to send the data as message text and/or as an attachment
		if (msg == null || !(mSendText.isChecked() || mSendFile.isChecked())) {
			Toast.makeText(getActivity(), R.string.msg_nothing_to_send, Toast.LENGTH_SHORT).show();
			return false;
		}
		try {
			Intent sendIntent = new Intent(Intent.ACTION_SEND);
			sendIntent.setType("text/plain");
			sendIntent.putExtra(Intent.EXTRA_TITLE, MSGNAME);
			sendIntent.putExtra(Intent.EXTRA_SUBJECT, MSGNAME);
			if (mSendText.isChecked())
				sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
			if (mSendFile.isChecked()) {
				File fileDir = activity.getFilesDir();
				if(fileDir.exists()) {
					FileOutputStream fos = activity.openFileOutput(FILENAME, Context.MODE_WORLD_READABLE);
					fos.write(msg.getBytes());
					fos.close();
					File streamPath = activity.getFileStreamPath(FILENAME);
					if(streamPath != null && streamPath.exists() && streamPath.length() > 0) {
						sendIntent.setType("text/csv");
						sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(streamPath));
					} else if (streamPath == null) {
						BPTrackerFree.logErrorAndToast(activity, TAG, R.string.msg_null_file_error);
					} else if (!streamPath.exists()) {
						BPTrackerFree.logErrorAndToast(activity, TAG, R.string.msg_no_file_error);
					} else if(streamPath.length() == 0) {
						BPTrackerFree.logErrorAndToast(activity, TAG, R.string.msg_zero_size_error);
					} else {
						BPTrackerFree.logErrorAndToast(activity, TAG, R.string.msg_twilight_zone);
					}

				} else {
					BPTrackerFree.logErrorAndToast(activity, TAG, R.string.msg_directory_error);
				}
			}
			startActivity(Intent.createChooser(sendIntent, getString(R.string.msg_choose_send_method)));
			return true;
		} catch (FileNotFoundException e) {
			BPTrackerFree.logErrorAndToast(activity, TAG, R.string.msg_file_not_found_error);
			e.printStackTrace();
		} catch (IOException e) {
			BPTrackerFree.logErrorAndToast(activity, TAG, R.string.msg_io_exception_error);
			e.printStackTrace();
		}
		return false;
	}

	public void onCheckedChanged(CompoundButton check_box, boolean checked) {
		if (check_box.equals(mSendText) && !checked && !mSendFile.isChecked())
			Toast.makeText(getActivity(), R.string.msg_nothing_to_send, Toast.LENGTH_SHORT).show();
		
		if (check_box.equals(mSendFile) && !checked && !mSendText.isChecked())
			Toast.makeText(getActivity(), R.string.msg_nothing_to_send, Toast.LENGTH_SHORT).show();
	}


	// Uses the member Cursor mRecordsCursor
	private String getMessage(Cursor cursor) {
		
		String date_localized;
		String time_localized;
		String sys_localized;
		String dia_localized;
		String pls_localized;
		String note_localized;

		Resources res = getResources();
		date_localized = res.getString(R.string.bp_send_date);
		time_localized = res.getString(R.string.bp_send_time);
		sys_localized = res.getString(R.string.bp_send_sys);
		dia_localized = res.getString(R.string.bp_send_dia);
		pls_localized = res.getString(R.string.bp_send_pls);
		note_localized = res.getString(R.string.bp_send_note);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		CsvWriter csvw = new CsvWriter(baos, ',', Charset.forName("UTF-8"));
		if (cursor != null && cursor.moveToFirst()) {
			try {
				String[] cnames = cursor.getColumnNames();
				int columns = cnames.length;
				
				for (int j = 0; j < columns; ++j) {
					if (j == COLUMN_ID_INDEX) { // put out nothing for the id column
						continue;
					}
					else if (j == COLUMN_SYSTOLIC_INDEX) {
						csvw.write(sys_localized);
					} else if (j == COLUMN_DIASTOLIC_INDEX) {
						csvw.write(dia_localized);
					} else if (j == COLUMN_PULSE_INDEX) {
						csvw.write(pls_localized);
					} else if (j == COLUMN_CREATED_AT_INDEX) {
						// This turns into two columns
						csvw.write(date_localized);
						csvw.write(time_localized);
					} else if (j == COLUMN_NOTE_INDEX) {
						csvw.write(note_localized);
					} else
						csvw.write(cnames[j]);
				}
				csvw.endRecord();
				do {
					// the final separator of each field is put on at the end.
					for (int j = 0; j < columns; ++j) {
						if (j == COLUMN_ID_INDEX) {
							continue;
						} else if (j == COLUMN_CREATED_AT_INDEX) {
							String date = BPTrackerFree.getDateString(cursor
									.getLong(j), DateFormat.SHORT);
							String time = BPTrackerFree.getTimeString(cursor
									.getLong(j), DateFormat.SHORT);
							csvw.write(date);
							csvw.write(time);
						} else if (j == COLUMN_NOTE_INDEX) { 
							csvw.write(String.valueOf(cursor.getString(j)));
						} else
							csvw.write(String.valueOf(cursor.getInt(j)));
					}
					csvw.endRecord();
				} while (cursor.moveToNext());
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				csvw.close();
			}
		}
		return baos.toString();
	}

	// Identifiers of our menu items
	private static final int SEND_ID = Menu.FIRST;
	private static final int CANCEL_ID = Menu.FIRST + 1;

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		// Build the menus that are shown when editing.
		menu.add(Menu.NONE, SEND_ID, 0, R.string.menu_send);
		menu.add(Menu.NONE, CANCEL_ID, 1, R.string.menu_cancel);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle all of the possible menu actions.
		Callback callback = (Callback) getActivity();
		switch (item.getItemId()) {
		case CANCEL_ID:
			callback.onComplete(Activity.RESULT_CANCELED);
			return true;
		case SEND_ID:
			if(sendData()) {
				callback.onComplete(Activity.RESULT_OK);
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public interface Callback {
		void onComplete(int status);
	}

}
