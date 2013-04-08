package com.eyebrowssoftware.bptrackerfree.test;

import junit.framework.Assert;
import android.test.ActivityInstrumentationTestCase2;

import com.eyebrowssoftware.bptrackerfree.activity.BPDataManager;

public class BPDataManagerTest extends ActivityInstrumentationTestCase2<BPDataManager> {

    public BPDataManagerTest() {
        super(BPDataManager.class);
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
