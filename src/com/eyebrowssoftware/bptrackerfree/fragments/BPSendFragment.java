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
import android.os.AsyncTask;
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
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
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
public class BPSendFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "BPSend";

    private Uri mUri;

    private TextView mMsgLabelView;
    private TextView mMsgView;
    private CheckBox mSendText;
    private CheckBox mSendFile;
    private Button mSendButton;
    private Button mCancelButton;
    private boolean mContentShown = true;
    private View mProgressContainer = null;
    private View mContentContainer = null;


    private static String mMsgLabelString;

    private static final String REVERSE = "reverse";

    // These are key names for saving things in the icicle
    private static final String SEND_TEXT = "tsend";
    private static final String SEND_FILE = "fsend";

    private static final int SEND_ID = 9;

    private static final String FILENAME = "data.csv";
    private static final String MSGNAME = "bpdata.csv";


    // This may or may not be used
    private boolean mReverse = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {

        View v = inflater.inflate(R.layout.bp_send_fragment, container, false);

        mContentContainer = v.findViewById(R.id.content_container);
        mProgressContainer = v.findViewById(R.id.progress_container);

        mMsgLabelView = (TextView) v.findViewById(R.id.message_label);
        mMsgLabelString = getString(R.string.label_message_format);

        mMsgView = (TextView) v.findViewById(R.id.message);

        mSendText = (CheckBox) v.findViewById(R.id.text);
        mSendText.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSendButton.setEnabled(isChecked || mSendFile.isChecked());
            }

        });
        mSendFile = (CheckBox) v.findViewById(R.id.attach);
        mSendFile.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                mSendButton.setEnabled(isChecked || mSendText.isChecked());
            }

        });

        mSendButton = (Button) v.findViewById(R.id.send);
        mSendButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                sendData();
            }
        });

        mCancelButton = (Button) v.findViewById(R.id.cancel);
        mCancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BPSendFragment.this.getActivity().finish();
            }

        });

        if(icicle != null) {
            mSendText.setChecked(icicle.getBoolean(SEND_TEXT));
            mSendFile.setChecked(icicle.getBoolean(SEND_FILE));
        } else {
            mSendText.setChecked(true);
            mSendFile.setChecked(true);
        }
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
        this.setShown(false, true);
        // Set up our cursor loader. It manages the cursors from now on
        this.getLoaderManager().initLoader(SEND_ID, null, this);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.mSendButton.setEnabled(this.mSendFile.isChecked() || this.mSendText.isChecked());
    }

    @Override
    public void onPause() {
        super.onPause();
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
        (new CreateMessageTask(this)).execute(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        mMsgLabelView.setText(String.format(mMsgLabelString, 0));
        mMsgView.setText("");
    }

    public void onCheckedChanged(CompoundButton check_box, boolean checked) {
        Activity activity = this.getActivity();
        if (check_box.equals(mSendText) && !checked && !mSendFile.isChecked()) {
            Toast.makeText(activity, R.string.msg_nothing_to_send, Toast.LENGTH_SHORT).show();
        } else if (check_box.equals(mSendFile) && !checked && !mSendText.isChecked()) {
            Toast.makeText(activity, R.string.msg_nothing_to_send, Toast.LENGTH_SHORT).show();
        }
    }

    private void sendData() {
        Void[] params = new Void[0];
        (new SendDataTask(this, mMsgView.getText().toString(), mSendText.isChecked(), mSendFile.isChecked())).execute(params);
    }

    static class SendDataTask extends AsyncTask<Void, Void, Intent> {

        String mMsg;
        int mToastMessageResource = -1;
        BPSendFragment mFragment;
        boolean mAsText;
        boolean mAsAttachment;

        public SendDataTask(BPSendFragment fragment, String msg, boolean asText, boolean asAttachment) {
            super();
            mFragment = fragment;
            mMsg = msg;
            mAsText = asText;
            mAsAttachment = asAttachment;
        }

        @Override
        protected Intent doInBackground(Void... params) {
            if (this.isCancelled()) {
                return null;
            }
            try {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TITLE, MSGNAME);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, MSGNAME);
                if (mAsText)
                    sendIntent.putExtra(Intent.EXTRA_TEXT, mMsg);
                if (mAsAttachment) {
                    File fileDir = mFragment.getActivity().getFilesDir();
                    if(fileDir.exists()) {
                        FileOutputStream fos = mFragment.getActivity().openFileOutput(FILENAME, Context.MODE_PRIVATE);
                        fos.write(mMsg.getBytes());
                        fos.close();
                        File streamPath = mFragment.getActivity().getFileStreamPath(FILENAME);
                        if(streamPath != null && streamPath.exists() && streamPath.length() > 0) {
                            sendIntent.setType("text/csv");
                            sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(streamPath));
                            return sendIntent;
                        } else if (streamPath == null) {
                            mToastMessageResource = R.string.msg_null_file_error;
                        } else if (!streamPath.exists()) {
                            mToastMessageResource = R.string.msg_no_file_error;
                        } else if(streamPath.length() == 0) {
                            mToastMessageResource = R.string.msg_zero_size_error;
                        } else {
                            mToastMessageResource = R.string.msg_twilight_zone;
                        }

                    } else {
                        mToastMessageResource = R.string.msg_directory_error;
                    }
                }
                return null;
            } catch (FileNotFoundException e) {
                mToastMessageResource = R.string.msg_file_not_found_error;
                e.printStackTrace();
            } catch (IOException e) {
                mToastMessageResource = R.string.msg_io_exception_error;
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Intent sendIntent) {
            super.onPostExecute(sendIntent);
            if (sendIntent != null) {
                mFragment.startActivity(Intent.createChooser(sendIntent, mFragment.getString(R.string.msg_choose_send_method)));
            } else if (this.mToastMessageResource > 0) {
                BPTrackerFree.logErrorAndToast(mFragment.getActivity(), TAG, R.string.msg_null_file_error);
            }
        }

    }


    private static class CreateMessageTask extends AsyncTask<Cursor, Void, String > {

        BPSendFragment mFragment;
        Context mContext;

        public CreateMessageTask(BPSendFragment fragment) {
            super();
            mFragment = fragment;
            mContext = fragment.getActivity();
        }

        @Override
        protected String doInBackground(Cursor... cursors) {
            String date_localized;
            String time_localized;
            String sys_localized;
            String dia_localized;
            String pls_localized;
            String note_localized;

            Cursor cursor = cursors[0];

            date_localized = mContext.getString(R.string.bp_send_date);
            time_localized = mContext.getString(R.string.bp_send_time);
            sys_localized = mContext.getString(R.string.bp_send_sys);
            dia_localized = mContext.getString(R.string.bp_send_dia);
            pls_localized = mContext.getString(R.string.bp_send_pls);
            note_localized = mContext.getString(R.string.bp_send_note);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CsvWriter csvw = new CsvWriter(baos, ',', Charset.forName("UTF-8"));
            if (cursor != null && cursor.moveToFirst()) {
                try {
                    String[] cnames = cursor.getColumnNames();
                    int columns = cnames.length;

                    for (int j = 0; j < columns; ++j) {
                        if (j == BPTrackerFree.COLUMN_ID_INDEX
                                || j == BPTrackerFree.COLUMN_MODIFIED_AT_INDEX) {
                            // put out nothing for certain columns
                            continue;
                        } else if (j == BPTrackerFree.COLUMN_SYSTOLIC_INDEX) {
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
                            if (j == BPTrackerFree.COLUMN_ID_INDEX
                                    || j == BPTrackerFree.COLUMN_MODIFIED_AT_INDEX) {
                                continue;
                            } else if (j == BPTrackerFree.COLUMN_CREATED_AT_INDEX) {
                                String date = BPTrackerFree
                                        .getDateString(cursor.getLong(j), DateFormat.SHORT);
                                String time = BPTrackerFree.getTimeString(cursor
                                        .getLong(j), DateFormat.SHORT);
                                csvw.write(date);
                                csvw.write(time);
                            } else if (j == BPTrackerFree.COLUMN_NOTE_INDEX) {
                                csvw.write(cursor.getString(j));
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
        protected void onPostExecute(String msg) {
            mFragment.updateUI(msg);
        }
    }

    private void updateUI(String msg) {
        mMsgLabelView.setText(String.format(mMsgLabelString, msg.length()));
        mMsgView.setText(msg);
        if (BPSendFragment.this.isResumed()) {
            BPSendFragment.this.setShown(true,  true);
        } else {
            BPSendFragment.this.setShown(true, false);
        }
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
            sendData();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Control whether the content is being displayed.  You can make it not
     * displayed if you are waiting for the initial data to show in it.  During
     * this time an indeterminant progress indicator will be shown instead.
     *
     * @param shown If true, the list view is shown; if false, the progress
     * indicator.  The initial value is true.
     * @param animate If true, an animation will be used to transition to the
     * new state.
     */
    private void setShown(boolean shown, boolean animate) {
        if (mProgressContainer == null) {
            throw new IllegalStateException("Can't be used with a custom content view");
        }
        if (mContentContainer == null) {
            throw new IllegalStateException("Empty content container");
        }
        if (mContentShown == shown) {
            return;
        }
        mContentShown = shown;
        if (shown) {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
                mContentContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
            } else {
                mProgressContainer.clearAnimation();
                mContentContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.GONE);
            mContentContainer.setVisibility(View.VISIBLE);
        } else {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
                mContentContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
            } else {
                mProgressContainer.clearAnimation();
                mContentContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.VISIBLE);
            mContentContainer.setVisibility(View.GONE);
        }
    }



}
