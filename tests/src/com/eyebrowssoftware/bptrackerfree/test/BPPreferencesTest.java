package com.eyebrowssoftware.bptrackerfree.test;

import junit.framework.Assert;
import android.test.ActivityInstrumentationTestCase2;

import com.eyebrowssoftware.bptrackerfree.activity.BPPreferenceActivity;


public class BPPreferencesTest extends ActivityInstrumentationTestCase2<BPPreferenceActivity> {

    public BPPreferencesTest() {
        super(BPPreferenceActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testPreconditions() {
        Assert.assertNotNull(this.getActivity());
    }
}
