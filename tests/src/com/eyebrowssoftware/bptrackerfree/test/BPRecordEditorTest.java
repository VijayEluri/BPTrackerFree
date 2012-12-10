package com.eyebrowssoftware.bptrackerfree.test;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.eyebrowssoftware.bptrackerfree.BPRecords;
import com.eyebrowssoftware.bptrackerfree.activity.BPRecordEditor;

/**
 * @author brionemde
 *
 */
public class BPRecordEditorTest extends
        ActivityInstrumentationTestCase2<BPRecordEditor> {

    private BPRecordEditor mActivity;
    private Spinner mSysSpinner;
    private Spinner mDiaSpinner;
    private Spinner mPlsSpinner;
    private TextView mNoteView;
    private Button mDateButton;
    private Button mTimeButton;
    private Button mDoneButton;
    private Button mRevertButton;

    /**
     *
     */
    public BPRecordEditorTest() {
        super("com.eyebrowssoftware.bptrackerfree", BPRecordEditor.class);
}


    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(false);
        Intent i = new Intent(Intent.ACTION_INSERT);
        i.setData(BPRecords.CONTENT_URI);
        setActivityIntent(i);

        mActivity = getActivity();

        mSysSpinner = (Spinner) mActivity.findViewById(com.eyebrowssoftware.bptrackerfree.R.id.systolic_spinner);
        mDiaSpinner = (Spinner) mActivity.findViewById(com.eyebrowssoftware.bptrackerfree.R.id.diastolic_spinner);
        mPlsSpinner = (Spinner) mActivity.findViewById(com.eyebrowssoftware.bptrackerfree.R.id.pulse_spinner);
        mNoteView = (TextView) mActivity.findViewById(com.eyebrowssoftware.bptrackerfree.R.id.note);
        mDateButton = (Button) mActivity.findViewById(com.eyebrowssoftware.bptrackerfree.R.id.date_button);
        mTimeButton = (Button) mActivity.findViewById(com.eyebrowssoftware.bptrackerfree.R.id.time_button);
        mDoneButton = (Button) mActivity.findViewById(com.eyebrowssoftware.bptrackerfree.R.id.done_button);
        mRevertButton = (Button) mActivity.findViewById(com.eyebrowssoftware.bptrackerfree.R.id.revert_button);
    }

    /**
     *
     */
    public void testPreConditions() {
        assertNotNull(mSysSpinner);
        assertNotNull(mDiaSpinner);
        assertNotNull(mPlsSpinner);
        assertNotNull(mNoteView);
        assertNotNull(mDateButton);
        assertNotNull(mTimeButton);
        assertNotNull(mDoneButton);
        assertNotNull(mRevertButton);
    }

/*    private static final int SYS_VALUE_1 = 125;
    private static final int DIA_VALUE_1 = 85;
    private static final int PLS_VALUE_1 = 70;
*/
    /**
     *
     */
/*    public void testSysSpinnerUI() {
        mActivity.runOnUiThread(
            new Runnable() {
                public void run() {
                    mSysSpinner.requestFocus();
                    mSysSpinner.setSelection(SYS_VALUE_1);
                }
            });
    }
*/
    /**
     *
     */
/*    public void testDiaSpinnerUI() {
        mActivity.runOnUiThread(
            new Runnable() {
                public void run() {
                    mDiaSpinner.requestFocus();
                    mDiaSpinner.setSelection(DIA_VALUE_1);
                 }
            });
    }
*/
    /**
     *
     */
/*    public void testPlsSpinnerUI() {
        mActivity.runOnUiThread(
            new Runnable() {
                public void run() {
                    mPlsSpinner.requestFocus();
                    mPlsSpinner.setSelection(PLS_VALUE_1);
                }
            });
    }

*/}
