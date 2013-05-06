package com.eyebrowssoftware.bptrackerfree.activity;

import java.util.GregorianCalendar;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.eyebrowssoftware.bptrackerfree.BPRecords;
import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;
import com.eyebrowssoftware.bptrackerfree.BPTrackerFree;
import com.eyebrowssoftware.bptrackerfree.R;
import com.eyebrowssoftware.bptrackerfree.fragments.BPRecordEditorFragment;
import com.eyebrowssoftware.bptrackerfree.fragments.BPRecordEditorFragment.BPEditorListener;

public class BPRecordEditor extends FragmentActivity implements BPEditorListener {
    static final String TAG = "BPRecordEditor";


    // Member Variables
    private int mState;

    private static final String[] AVERAGE_PROJECTION = {
        BPRecord.AVERAGE_SYSTOLIC,
        BPRecord.AVERAGE_DIASTOLIC,
        BPRecord.AVERAGE_PULSE
    };

    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.bp_fragment_container);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Uri uri = null;
        Intent intent = getIntent();
        if (Intent.ACTION_EDIT.equals(intent.getAction())) {
            uri = intent.getData();
            mState = BPRecordEditorFragment.STATE_EDIT;
        } else if (Intent.ACTION_INSERT.equals(intent.getAction())) {
            mState = BPRecordEditorFragment.STATE_INSERT;
            if (icicle != null)
                uri = Uri.parse(icicle.getString(BPTrackerFree.MURI));
            else {
                ContentValues cv = null;
                if (mSharedPreferences.getBoolean(BPTrackerFree.AVERAGE_VALUES_KEY, false)) {
                    cv = setAverageValues(mSharedPreferences);
                }
                else {
                    cv = setDefaultValues(mSharedPreferences);
                }
                cv.put(BPRecord.CREATED_DATE, GregorianCalendar.getInstance().getTimeInMillis());
                uri = getContentResolver().insert(BPRecords.CONTENT_URI, cv);
            }
        }
        if (icicle == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.editor_container, BPRecordEditorFragment.newInstance(uri, mState));
            ft.commit();
        }
    }

    @Override
    public void finishing() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.getMenuInflater().inflate(R.menu.bp_record_editor_activity_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(this, BPPreferenceActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private ContentValues setAverageValues(SharedPreferences prefs) {
        Cursor c = getContentResolver()
            .query(BPRecords.CONTENT_URI, AVERAGE_PROJECTION, null, null, null);
        ContentValues cv = new ContentValues();
        if (c != null && c.moveToFirst() && !c.isNull(0) && !c.isNull(1) && !c.isNull(2)) {
            cv = setContentValues((int) c.getFloat(0), (int) c.getFloat(1), (int) c.getFloat(2));
        }
        else {
            cv = setDefaultValues(mSharedPreferences);
        }
        c.close();
        return cv;
    }

    private ContentValues setDefaultValues(SharedPreferences prefs) {
        return setContentValues(
                Integer.valueOf(prefs.getString(BPTrackerFree.DEFAULT_SYSTOLIC_KEY, BPTrackerFree.SYSTOLIC_DEFAULT_STRING)),
                Integer.valueOf(prefs.getString(BPTrackerFree.DEFAULT_DIASTOLIC_KEY, BPTrackerFree.DIASTOLIC_DEFAULT_STRING)),
                Integer.valueOf(prefs.getString(BPTrackerFree.DEFAULT_PULSE_KEY, BPTrackerFree.PULSE_DEFAULT_STRING)));
    }

    private ContentValues setContentValues(int systolic, int diastolic, int pulse) {
        ContentValues cv = new ContentValues();
        cv.put(BPRecord.SYSTOLIC, systolic);
        cv.put(BPRecord.DIASTOLIC, diastolic);
        cv.put(BPRecord.PULSE, pulse);
        return cv;
    }

}
