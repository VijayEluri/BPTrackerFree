/*
 * Copyright 2010 - Brion Noble Emde
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
package com.eyebrowssoftware.bptrackerfree;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;

/**
 * Provides access to a database of notes. Each note has a title, the note
 * itself, a creation date and a modified data.
 */
public class BPProviderFree extends ContentProvider {
    @SuppressWarnings("unused")
    private static final String TAG = "BPProviderFree";

    /**
     *
     */
    public static final String AUTHORITY = "com.eyebrowssoftware.bptrackerfree.bp";
    /**
     *
     */
    public static final String URI_STRING = "content://" + AUTHORITY;

    private static final String DATABASE_NAME = "bptrackerfree.db";
    private static final int DB_VERSION_1 = 1;
    private static final int DB_VERSION_2 = 2;
    private static final int DATABASE_VERSION = DB_VERSION_2;

    private static final String BP_RECORDS_TABLE_NAME = "bp_records";

    private static HashMap<String, String> sBPProjectionMap;

    private static final UriMatcher sUriMatcher;

    /**
     *
     */
    public static final Uri CONTENT_URI = Uri.parse(URI_STRING);

    private ContentResolver mCR;

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL("CREATE TABLE " + BP_RECORDS_TABLE_NAME + " ("
                    + BPRecord._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + BPRecord.SYSTOLIC + " INTEGER," + BPRecord.DIASTOLIC
                    + " INTEGER," + BPRecord.PULSE + " INTEGER,"
                    + BPRecord.CREATED_DATE + " INTEGER,"
                    + BPRecord.MODIFIED_DATE + " INTEGER,"
                    + BPRecord.NOTE + " TEXT"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion == DB_VERSION_1) {
                if (newVersion == DB_VERSION_2) {
                    db.execSQL("ALTER TABLE " + BP_RECORDS_TABLE_NAME + " ADD COLUMN " + BPRecord.NOTE + " TEXT");
                }
            }
        }
    }

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        Context c = getContext();
        mOpenHelper = new DatabaseHelper(c);
        mCR = c.getContentResolver();
        return true;
    }

    @Override
    public void finalize() {
        mOpenHelper.close();
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        Cursor c = null;

        switch (sUriMatcher.match(uri)) {
        // Result is a page of bp_records
        case BP_RECORDS:
            qb.setTables(BP_RECORDS_TABLE_NAME);
            qb.setProjectionMap(sBPProjectionMap);
            c = qb.query(db, projection, selection, selectionArgs, null, null,
                    sortOrder);
            break;
        // Result is a single bp_record
        case BP_RECORD_ID:
            qb.setTables(BP_RECORDS_TABLE_NAME);
            qb.setProjectionMap(sBPProjectionMap);
            qb.appendWhere(BPRecords.BPRecord._ID + "="
                    + uri.getPathSegments().get(1));
            c = qb.query(db, projection, selection, selectionArgs, null, null,
                    sortOrder);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        // Tell the cursor what uri to watch, so it knows when its source data
        // changes
        c.setNotificationUri(mCR, uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case BP_RECORDS:
            return BPRecords.CONTENT_TYPE;
        case BP_RECORD_ID:
            return BPRecord.CONTENT_ITEM_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) throws SQLException {

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long item_id = -1;
        ContentValues values = new ContentValues();
        if (initialValues != null) {
            values = initialValues;
        } else {
            values = new ContentValues();
        }
        Long now = Long.valueOf(System.currentTimeMillis());

        switch (sUriMatcher.match(uri)) {
        case BP_RECORDS:
            if (values.containsKey(BPRecords.BPRecord.CREATED_DATE) == false)
                values.put(BPRecords.BPRecord.CREATED_DATE, now);
            if (values.containsKey(BPRecords.BPRecord.MODIFIED_DATE) == false)
                values.put(BPRecords.BPRecord.MODIFIED_DATE, now);
            if (values.containsKey(BPRecords.BPRecord.SYSTOLIC) == false)
                values.put(BPRecords.BPRecord.SYSTOLIC, BPTrackerFree.SYSTOLIC_DEFAULT);
            if (values.containsKey(BPRecords.BPRecord.DIASTOLIC) == false)
                values.put(BPRecords.BPRecord.DIASTOLIC, BPTrackerFree.DIASTOLIC_DEFAULT);
            if (values.containsKey(BPRecords.BPRecord.PULSE) == false) {
                values.put(BPRecords.BPRecord.PULSE, BPTrackerFree.PULSE_DEFAULT);
            }
            item_id = db.insert(BP_RECORDS_TABLE_NAME,
                    BPRecords.BPRecord.CREATED_DATE, values);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        Uri ret = null;
        if (item_id > 0) {
            ret = ContentUris.withAppendedId(BPRecords.CONTENT_URI,
                    item_id);
            mCR.notifyChange(ret, null);
        }
        return ret;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        int count = 0;
        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String recId;
        String whereClause;

        switch (sUriMatcher.match(uri)) {
        case BP_RECORDS:
            count = db.delete(BP_RECORDS_TABLE_NAME, where, whereArgs);
            break;
        case BP_RECORD_ID:
            recId = uri.getPathSegments().get(1);
            whereClause = BPRecord._ID + "=" + recId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
            count = db.delete(BP_RECORDS_TABLE_NAME, whereClause, whereArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        mCR.notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where,
            String[] whereArgs) {
        int count = 0;
        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String recId;
        String whereClause;

        switch (sUriMatcher.match(uri)) {
        case BP_RECORDS:
            count = db.update(BP_RECORDS_TABLE_NAME, values, where, whereArgs);
            break;
        case BP_RECORD_ID:
            recId = uri.getPathSegments().get(1);
            whereClause = BPRecord._ID + "=" + recId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
            count = db.update(BP_RECORDS_TABLE_NAME, values, whereClause, whereArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        mCR.notifyChange(uri, null);
        return count;
    }

    private static final int BP_RECORDS = 1;
    private static final int BP_RECORD_ID = BP_RECORDS + 1;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "bp_records", BP_RECORDS);
        sUriMatcher.addURI(AUTHORITY, "bp_records/#", BP_RECORD_ID);

        sBPProjectionMap = new HashMap<String, String>();
        sBPProjectionMap.put(BPRecord._ID, BPRecord._ID);
        sBPProjectionMap.put(BPRecord.SYSTOLIC, BPRecord.SYSTOLIC);
        sBPProjectionMap.put(BPRecord.DIASTOLIC, BPRecord.DIASTOLIC);
        sBPProjectionMap.put(BPRecord.PULSE, BPRecord.PULSE);
        sBPProjectionMap.put(BPRecord.CREATED_DATE, BPRecord.CREATED_DATE);
        sBPProjectionMap.put(BPRecord.MODIFIED_DATE, BPRecord.MODIFIED_DATE);
        sBPProjectionMap.put(BPRecord.MAX_SYSTOLIC, String.format("max(%s)", BPRecord.SYSTOLIC));
        sBPProjectionMap.put(BPRecord.MIN_SYSTOLIC, String.format("min(%s)", BPRecord.SYSTOLIC));
        sBPProjectionMap.put(BPRecord.MAX_DIASTOLIC, String.format("max(%s)", BPRecord.DIASTOLIC));
        sBPProjectionMap.put(BPRecord.MIN_DIASTOLIC, String.format("min(%s)", BPRecord.DIASTOLIC));
        sBPProjectionMap.put(BPRecord.MAX_PULSE, String.format("max(%s)", BPRecord.PULSE));
        sBPProjectionMap.put(BPRecord.MIN_PULSE, String.format("min(%s)", BPRecord.PULSE));
        sBPProjectionMap.put(BPRecord.MAX_CREATED_DATE, String.format("max(%s)", BPRecord.CREATED_DATE));
        sBPProjectionMap.put(BPRecord.MIN_CREATED_DATE, String.format("min(%s)", BPRecord.CREATED_DATE));
        sBPProjectionMap.put(BPRecord.NOTE, BPRecord.NOTE);
        sBPProjectionMap.put(BPRecord.AVERAGE_SYSTOLIC, String.format("round(avg(%s))", BPRecord.SYSTOLIC));
        sBPProjectionMap.put(BPRecord.AVERAGE_DIASTOLIC, String.format("round(avg(%s))", BPRecord.DIASTOLIC));
        sBPProjectionMap.put(BPRecord.AVERAGE_PULSE, String.format("round(avg(%s))", BPRecord.PULSE));
    }
}
