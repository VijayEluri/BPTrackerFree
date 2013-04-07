/*
 * Copyright 2013 - Brion Noble Emde
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
package com.eyebrowssoftware.bptrackerfree.fragments;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.csvreader.CsvWriter;
import com.eyebrowssoftware.bptrackerfree.BPRecords;
import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;
import com.eyebrowssoftware.bptrackerfree.BPTrackerFree;
import com.eyebrowssoftware.bptrackerfree.R;

/**
 * @author brionemde
 *
 */
public class BPSendFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
    CompoundButton.OnCheckedChangeListener, OnClickListener {

    private static final String TAG = "BPSend";

    private Uri mUri;

    private TextView mMsgLabelView;
    private TextView mMsgView;
    private CheckBox mSendText;
    private CheckBox mSendFile;
    private Button mSendButton;
    private Button mCancelButton;

    private static String mMsgLabelString;

    private static final String REVERSE = "reverse";

    // These are key names for saving things in the icicle
    private static final String SEND_TEXT = "tsend";
    private static final String SEND_FILE = "fsend";

    private static final int SEND_ID = 9;

    // This may or may not be used
    private boolean mReverse = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {

        View v = inflater.inflate(R.layout.bp_send_fragment, container, false);

        mMsgLabelView = (TextView) v.findViewById(R.id.message_label);
        mMsgLabelString = getString(R.string.label_message_format);

        mMsgView = (TextView) v.findViewById(R.id.message);

        mSendText = (CheckBox) v.findViewById(R.id.text);
        mSendText.setOnCheckedChangeListener(this);

        mSendFile = (CheckBox) v.findViewById(R.id.attach);
        mSendFile.setOnCheckedChangeListener(this);

        mSendButton = (Button) v.findViewById(R.id.send);
        mSendButton.setOnClickListener(this);

        mCancelButton = (Button) v.findViewById(R.id.cancel);
        mCancelButton.setOnClickListener(this);

return v;

    }

    @Override
    public void onActivityCreated(Bundle icicle) {
        super.onActivityCreated(icicle);
        Intent intent = this.getActivity().getIntent();
        if (intent.getData() == null) {
            intent.setData(BPRecords.CONTENT_URI);
        }
        mReverse = intent.getBooleanExtra(REVERSE, true);

        mUri = intent.getData();
        // Set up our cursor loader. It manages the cursors from now on
        this.getLoaderManager().initLoader(SEND_ID, null, this);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if(icicle != null) {
            mSendText.setChecked(icicle.getBoolean(SEND_TEXT));
            mSendFile.setChecked(icicle.getBoolean(SEND_FILE));
        } else {
            mSendText.setChecked(true);
            mSendFile.setChecked(true);
        }
    }

    public void onClick(View v) {
        Activity activity = this.getActivity();
        if (v.equals(mSendButton)) {
            if (sendData()) {
                activity.finish();
            } else {
                Toast.makeText(activity, R.string.msg_nothing_to_send, Toast.LENGTH_SHORT).show();
            }
        } else if (v.equals(mCancelButton)) {
            activity.finish();
        }
    }

    private static final String FILENAME = "data.csv";
    private static final String MSGNAME = "bpdata.csv";

    private boolean sendData() {
        Activity activity = this.getActivity();
        String msg = mMsgView.getText().toString();
        // We're going to send the data as message text and/or as an attachment
        if (msg == null || !(mSendText.isChecked() || mSendFile.isChecked())) {
            Toast.makeText(activity, R.string.msg_nothing_to_send, Toast.LENGTH_SHORT).show();
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
                    FileOutputStream fos = activity.openFileOutput(FILENAME, Context.MODE_PRIVATE);
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
        Activity activity = this.getActivity();
        if (check_box.equals(mSendText) && !checked && !mSendFile.isChecked())
            Toast.makeText(activity, R.string.msg_nothing_to_send, Toast.LENGTH_SHORT).show();

        if (check_box.equals(mSendFile) && !checked && !mSendText.isChecked())
            Toast.makeText(activity, R.string.msg_nothing_to_send, Toast.LENGTH_SHORT).show();
    }


    // Uses the member Cursor mRecordsCursor
    private static String getMessage(Context context, Cursor cursor) {

        String date_localized;
        String time_localized;
        String sys_localized;
        String dia_localized;
        String pls_localized;
        String note_localized;

        date_localized = context.getString(R.string.bp_send_date);
        time_localized = context.getString(R.string.bp_send_time);
        sys_localized = context.getString(R.string.bp_send_sys);
        dia_localized = context.getString(R.string.bp_send_dia);
        pls_localized = context.getString(R.string.bp_send_pls);
        note_localized = context.getString(R.string.bp_send_note);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CsvWriter csvw = new CsvWriter(baos, ',', Charset.forName("UTF-8"));
        if (cursor != null && cursor.moveToFirst()) {
            try {
                String[] cnames = cursor.getColumnNames();
                int columns = cnames.length;

                for (int j = 0; j < columns; ++j) {
                    if (j == BPTrackerFree.COLUMN_ID_INDEX) { // put out nothing for the id column
                        continue;
                    }
                    else if (j == BPTrackerFree.COLUMN_SYSTOLIC_INDEX) {
                        csvw.write(sys_localized);
                    } else if (j == BPTrackerFree.COLUMN_DIASTOLIC_INDEX) {
                        csvw.write(dia_localized);
                    } else if (j == BPTrackerFree.COLUMN_PULSE_INDEX) {
                        csvw.write(pls_localized);
                    } else if (j == BPTrackerFree.COLUMN_CREATED_AT_INDEX) {
                        // This turns into two columns
                        csvw.write(date_localized);
                        csvw.write(time_localized);
                    } else if (j == BPTrackerFree.COLUMN_NOTE_INDEX) {
                        csvw.write(note_localized);
                    } else
                        csvw.write(cnames[j]);
                }
                csvw.endRecord();
                do {
                    // the final separator of each field is put on at the end.
                    for (int j = 0; j < columns; ++j) {
                        if (j == BPTrackerFree.COLUMN_ID_INDEX) {
                            continue;
                        } else if (j == BPTrackerFree.COLUMN_CREATED_AT_INDEX) {
                            String date = BPTrackerFree.getDateString(cursor
                                    .getLong(j), DateFormat.SHORT);
                            String time = BPTrackerFree.getTimeString(cursor
                                    .getLong(j), DateFormat.SHORT);
                            csvw.write(date);
                            csvw.write(time);
                        } else if (j == BPTrackerFree.COLUMN_NOTE_INDEX) {
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.bp_send_options_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle all of the possible menu actions.
        Activity activity = this.getActivity();
        switch (item.getItemId()) {
        case R.id.menu_cancel:
            activity.finish();
            return true;
        case R.id.menu_send:
            if(sendData()) {
                activity.finish();
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        Activity activity = this.getActivity();
        CursorLoader loader = new CursorLoader(activity, mUri, BPTrackerFree.PROJECTION, null, null,
                        BPRecord.CREATED_DATE + ((mReverse) ? " DESC" : " ASC"));
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        String msg = getMessage(this.getActivity(), cursor);
        mMsgLabelView.setText(String.format(mMsgLabelString, msg.length()));
        mMsgView.setText(msg);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        mMsgLabelView.setText(String.format(mMsgLabelString, 0));
        mMsgView.setText("");
    }


}
