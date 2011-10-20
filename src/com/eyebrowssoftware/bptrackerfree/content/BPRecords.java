package com.eyebrowssoftware.bptrackerfree.content;

import com.eyebrowssoftware.bptrackerfree.BPProviderFree;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class BPRecords {

	// Private constructor - This class cannot be instantiated
	private BPRecords() {
	}

	/**
	 * The content:// style URL for this table
	 */
	public static final Uri CONTENT_URI = BPProviderFree.CONTENT_URI.buildUpon().appendPath("bp_records").build();

	/**
	 * The MIME type of {@link #CONTENT_URI} providing a directory of
	 * breweries.
	 */
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/vnd.eyebrowssoftware.free.bp_record";

	public static final class BPRecord implements BaseColumns {
		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
		 * note.
		 */
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.eyebrowssoftware.free.bp_record";

		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = "created_at DESC";

		/**
		 * The id of the Beer record
		 * <P>
		 * Type: INTEGER
		 */
		public static final String _ID = BaseColumns._ID;

		/**
		 * The Systolic value of the BP
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String SYSTOLIC = "systolic";

		/**
		 * The Diastolic value of the BP
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String DIASTOLIC = "diastolic";

		/**
		 * The pulse rate, entered or facilitated and entered
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String PULSE = "pulse_rate";

		/**
		 * The DATETIME that the entry was created in the database
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String CREATED_DATE = "created_at";

		/**
		 * The DATETIME that the entry was modified in the database
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String MODIFIED_DATE = "modified_at";

		/**
		 * The Maximum queried value for Systolic 
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String MAX_SYSTOLIC = "max_systolic"; // String.format("max(%s)", SYSTOLIC);

		/**
		 * The Minimum queried value for Systolic 
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String MIN_SYSTOLIC = "min_systolic"; // String.format("min(%s)", SYSTOLIC);

		/**
		 * The Maximum queried value for Diastolic 
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String MAX_DIASTOLIC = "max_diastolic"; // String.format("max(%s)", DIASTOLIC);

		/**
		 * The Minimum queried value for Diastolic 
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String MIN_DIASTOLIC = "min_diastolic"; // String.format("min(%s)", DIASTOLIC);

		/**
		 * The Maximum queried value for Pulse 
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String MAX_PULSE = "max_pulse"; // String.format("max(%s)", PULSE);
 
		/**
		 * The Minimum queried value for Pulse 
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String MIN_PULSE = "min_pulse"; // String.format("min(%s)", BPRecord.PULSE);
		
		/**
		 * The Maximum queried value for Creation Date 
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String MAX_CREATED_DATE = "max_date"; // String.format("max(%s)", BPRecord.CREATED_DATE);

		/**
		 * The Minimum queried value for Creation Date 
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String MIN_CREATED_DATE = "min_date"; // String.format("min(%s)", BPRecord.CREATED_DATE);
		
		/**
		 * The Note the user attached to this record
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String NOTE = "note";


	}
}
