package com.eyebrowssoftware.bptrackerfree.test;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import com.eyebrowssoftware.bptrackerfree.BPRecords;
import com.eyebrowssoftware.bptrackerfree.activity.BPRecordEditor;

/**
 * @author brionemde
 *
 */
public class BPRecordEditorTest extends
        ActivityInstrumentationTestCase2<BPRecordEditor> {

    private BPRecordEditor mActivity;

    /**
     *
     */
    public BPRecordEditorTest() {
        super(BPRecordEditor.class);
}


    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(false);
        Intent i = new Intent(Intent.ACTION_INSERT);
        i.setData(BPRecords.CONTENT_URI);
        setActivityIntent(i);

        mActivity = getActivity();
    }

    /**
     *
     */
    public void testPreConditions() {
        assertNotNull(mActivity);
    }
}
