package com.eyebrowssoftware.bptrackerfree.test;

import junit.framework.Assert;
import android.test.ActivityInstrumentationTestCase2;

import com.eyebrowssoftware.bptrackerfree.activity.BPSend;

public class BPSendTest extends ActivityInstrumentationTestCase2<BPSend> {

    public BPSendTest() {
        super(BPSend.class);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testPreconditions() {
        Assert.assertNotNull(this.getActivity());
    }
}
