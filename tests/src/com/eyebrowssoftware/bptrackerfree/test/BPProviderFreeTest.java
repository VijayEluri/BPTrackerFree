package com.eyebrowssoftware.bptrackerfree.test;

import junit.framework.Assert;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import com.eyebrowssoftware.bptrackerfree.BPProviderFree;
import com.eyebrowssoftware.bptrackerfree.BPRecords;
import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;
import com.eyebrowssoftware.bptrackerfree.BPTrackerFree;

/**
 * @author brionemde
 *
 */
public class BPProviderFreeTest extends ProviderTestCase2<BPProviderFree> {
    static final String TAG = "BPProviderTest";

    private static final String[] PROJECTION = {
            BPRecord._ID,
            BPRecord.SYSTOLIC,
            BPRecord.DIASTOLIC,
            BPRecord.PULSE,
            BPRecord.CREATED_DATE,
            BPRecord.MODIFIED_DATE
    };

    private static final String[] AVERAGE_PROJECTION = {
        BPRecord.AVERAGE_SYSTOLIC,
        BPRecord.AVERAGE_DIASTOLIC,
        BPRecord.AVERAGE_PULSE
    };

    // BP Record Indices
    // private static final int COLUMN_ID_INDEX = 0;
    @SuppressWarnings("unused")
    private static final int COLUMN_SYSTOLIC_INDEX = 1;
    @SuppressWarnings("unused")
    private static final int COLUMN_DIASTOLIC_INDEX = 2;
    @SuppressWarnings("unused")
    private static final int COLUMN_PULSE_INDEX = 3;
    @SuppressWarnings("unused")
    private static final int COLUMN_CREATED_AT_INDEX = 4;
    @SuppressWarnings("unused")
    private static final int COLUMN_MODIFIED_AT_INDEX = 5;

    private static final int SYS_1 = 121;
    private static final int SYS_2 = 144;
    private static final int SYS_3 = 84;
    private static final int DIA_1 = 72;
    private static final int DIA_2 = 92;
    private static final int DIA_3 = 50;
    private static final int PLS_1 = 73;
    private static final int PLS_2 = 125;
    private static final int PLS_3 = 59;

    MockContentResolver mCR;

    /**
     *
     */
    public BPProviderFreeTest() {
        super(BPProviderFree.class, BPProviderFree.AUTHORITY);
        Log.d(TAG, "Reporting in");
}

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mCR = this.getMockContentResolver();
        mCR.addProvider("com.eyebrowssoftware.bptrackerfree.bp", this.getProvider());
    }

    @Override
    public void tearDown() throws Exception {
        mCR = null;
        super.tearDown();
    }

   /**
 *
 */
@SmallTest
    public void testPreconditions() {
        // Log.d(TAG, "Preconditions");
    }

    /**
     *
     */
    public void testInsertEmpty() {
        Uri url = BPRecords.CONTENT_URI;
        ContentValues values = new ContentValues();
        Uri newUri = mCR.insert(url, values);
        Assert.assertNotNull(newUri);
        Cursor c = mCR.query(newUri, PROJECTION, null, null, null);
        Assert.assertTrue(c.getCount() == 1);
        c.moveToFirst();
        // These will change when the new defaults stuff appears
        Assert.assertTrue(c.getInt(1) == BPTrackerFree.SYSTOLIC_DEFAULT);
        Assert.assertTrue(c.getInt(2) == BPTrackerFree.DIASTOLIC_DEFAULT);
        Assert.assertTrue(c.getInt(3) == BPTrackerFree.PULSE_DEFAULT);
        c.close();
    }

    /**
     *
     */
    public void testInsertOne() {
        Uri url = BPRecords.CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put(BPRecord.SYSTOLIC, SYS_1);
        values.put(BPRecord.DIASTOLIC, DIA_1);
        values.put(BPRecord.PULSE, PLS_1);
        Uri newUri = mCR.insert(url, values);
        Assert.assertNotNull(newUri);
        Cursor c = mCR.query(newUri, PROJECTION, null, null, null);
        Assert.assertTrue(c.getCount() == 1);
        c.moveToFirst();
        Assert.assertTrue(c.getInt(1) == SYS_1);
        Assert.assertTrue(c.getInt(2) == DIA_1);
        Assert.assertTrue(c.getInt(3) == PLS_1);
        c.close();
    }

    /**
     *
     */
    public void testUpdateItem() {
        Uri url = BPRecords.CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put(BPRecord.SYSTOLIC, SYS_1);
        values.put(BPRecord.DIASTOLIC, DIA_1);
        values.put(BPRecord.PULSE, PLS_1);
        url = mCR.insert(url, values);
        values.put(BPRecord.SYSTOLIC, SYS_2);
        values.put(BPRecord.DIASTOLIC, DIA_2);
        values.put(BPRecord.PULSE, PLS_2);
        Assert.assertTrue(mCR.update(url, values, null, null) == 1);
        Cursor c = mCR.query(url, PROJECTION, null, null, null);
        Assert.assertTrue(c.getCount() == 1);
        c.moveToFirst();
        Assert.assertTrue(c.getInt(1) == SYS_2);
        Assert.assertTrue(c.getInt(2) == DIA_2);
        Assert.assertTrue(c.getInt(3) == PLS_2);
        c.close();
    }

    /**
     *
     */
    public void testDeleteItem() {
         Uri url = BPRecords.CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put(BPRecord.SYSTOLIC, SYS_1);
        values.put(BPRecord.DIASTOLIC, DIA_1);
        values.put(BPRecord.PULSE, PLS_1);
        url = mCR.insert(url, values);
        Assert.assertTrue(mCR.delete(url, null, null) == 1);
    }

    private void doBulkCreation() {
        Uri url = BPRecords.CONTENT_URI;

        ContentValues[] vals_array = new ContentValues[3];
        vals_array[0] = new ContentValues();
        vals_array[0].put(BPRecord.SYSTOLIC, SYS_1);
        vals_array[0].put(BPRecord.DIASTOLIC, DIA_1);
        vals_array[0].put(BPRecord.PULSE, PLS_1);

        vals_array[1] = new ContentValues();
        vals_array[1].put(BPRecord.SYSTOLIC, SYS_2);
        vals_array[1].put(BPRecord.DIASTOLIC, DIA_2);
        vals_array[1].put(BPRecord.PULSE, PLS_2);

        vals_array[2] = new ContentValues();
        vals_array[2].put(BPRecord.SYSTOLIC, SYS_3);
        vals_array[2].put(BPRecord.DIASTOLIC, DIA_3);
        vals_array[2].put(BPRecord.PULSE, PLS_3);

        Assert.assertTrue(mCR.bulkInsert(url, vals_array) == 3);

    }

    /**
     *
     */
    public void testQueryBPRecords() {
        Uri url = BPRecords.CONTENT_URI;

        doBulkCreation();

        Cursor c = mCR.query(url, PROJECTION, null, null, null);
        Assert.assertNotNull(c);
        Assert.assertTrue(c.getCount() == 3);

        c.moveToFirst();
        Assert.assertTrue(c.getInt(1) == SYS_1);
        Assert.assertTrue(c.getInt(2) == DIA_1);
        Assert.assertTrue(c.getInt(3) == PLS_1);

        c.moveToNext();
        Assert.assertTrue(c.getInt(1) == SYS_2);
        Assert.assertTrue(c.getInt(2) == DIA_2);
        Assert.assertTrue(c.getInt(3) == PLS_2);

        c.moveToNext();
        Assert.assertTrue(c.getInt(1) == SYS_3);
        Assert.assertTrue(c.getInt(2) == DIA_3);
        Assert.assertTrue(c.getInt(3) == PLS_3);

        String[] selArgs = { String.valueOf(SYS_3), String.valueOf(DIA_3) };
        c = mCR.query(url, PROJECTION, "systolic=? AND diastolic=?", selArgs, null);
        Assert.assertTrue(c.getCount() == 1);
        c.moveToFirst();
        Assert.assertTrue(c.getInt(1) == SYS_3);
        Assert.assertTrue(c.getInt(2) == DIA_3);
        Assert.assertTrue(c.getInt(3) == PLS_3);

        c.close();

    }

    /**
     *
     */
    public void testAverageValues() {
        Uri url = BPRecords.CONTENT_URI;

        doBulkCreation();

        Cursor c = mCR.query(url, AVERAGE_PROJECTION, null, null, null);
        assertNotNull(c);
        assertTrue(c.getCount() == 1);
        assertTrue(c.moveToFirst());
        assertEquals((int) c.getFloat(0), (int) Math.round((SYS_1 + SYS_2 + SYS_3)/3.0));
        assertEquals((int) c.getFloat(1), (int) Math.round((DIA_1 + DIA_2 + DIA_3)/3.0));
        assertEquals((int) c.getFloat(2), (int) Math.round((PLS_1 + PLS_2 + PLS_3)/3.0));
    }

    /**
    *
    */
   public void testAverageValuesForEmptyDB() {
       Uri url = BPRecords.CONTENT_URI;

       Cursor c = mCR.query(url, AVERAGE_PROJECTION, null, null, null);
       assertNotNull(c);
       assertTrue(c.getCount() == 1);
       assertTrue(c.moveToFirst());
       assertTrue(c.isNull(0) && c.isNull(1) && c.isNull(2));
   }

}
