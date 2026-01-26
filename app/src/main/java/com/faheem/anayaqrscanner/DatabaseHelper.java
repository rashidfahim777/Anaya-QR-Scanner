package com.faheem.anayaqrscanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.faheem.anayaqrscanner.ScanItem;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // Database Info
    private static final String DATABASE_NAME = "scanner.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_HISTORY = "scan_history";

    // History Table Columns
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    // Singleton Instance
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database is created for the FIRST time
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables...");

        String CREATE_HISTORY_TABLE = "CREATE TABLE " + TABLE_HISTORY + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TYPE + " INTEGER NOT NULL,"
                + COLUMN_CONTENT + " TEXT NOT NULL,"
                + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";

        db.execSQL(CREATE_HISTORY_TABLE);
        Log.d(TAG, "Table " + TABLE_HISTORY + " created successfully");
    }

    // Called when the database needs to be upgraded
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        if (oldVersion < 1) {
            // Drop older table if existed
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
            onCreate(db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    // ==================== CRUD Operations ====================

    /**
     * Add a new scan to the database
     * @param scanItem The scan item to add
     * @return The row ID of the newly inserted row, or -1 if an error occurred
     */
    public long addScan(String content) {
        Log.d(TAG, "Adding scan to database with content: " +
                (content != null ? content.substring(0, Math.min(20, content.length())) + "..." : "null"));

        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TYPE, getScanTypeFromContent(content)); // Automatically determine type
            values.put(COLUMN_CONTENT, content);
            values.put(COLUMN_TIMESTAMP, getCurrentTimestamp());

            long id = db.insert(TABLE_HISTORY, null, values);

            Log.d(TAG, "Scan added with ID: " + id);
            return id;

        } catch (Exception e) {
            Log.e(TAG, "Error adding scan to database", e);
            return -1;
        } finally {
            db.close();
        }
    }

    public boolean deleteScan(String content) {
        Log.d(TAG, "Deleting scan with content: " +
                (content != null ? content.substring(0, Math.min(20, content.length())) + "..." : "null"));

        SQLiteDatabase db = this.getWritableDatabase();

        try {
            int rowsAffected = db.delete(TABLE_HISTORY,
                    COLUMN_CONTENT + " = ?",
                    new String[]{content});

            Log.d(TAG, "Rows affected by delete: " + rowsAffected);
            return rowsAffected > 0;

        } catch (Exception e) {
            Log.e(TAG, "Error deleting scan by content", e);
            return false;
        } finally {
            db.close();
        }
    }

    // Update the getScanTypeFromContent method:
    private int getScanTypeFromContent(String content) {
        if (content == null) return 256; // Default to QR Code

        content = content.trim();

        // Check for barcode patterns (numeric only)
        if (content.matches("^[0-9]+$")) {
            int length = content.length();

            // Common barcode lengths
            if (length == 12) return 128; // UPC-A
            if (length == 13) return 64;  // EAN-13
            if (length == 8) return 32;   // EAN-8
            if (length >= 6 && length <= 8) return 512; // UPC-E
            if (length >= 4 && length <= 30) return 1;  // CODE_128

            return 1; // Default to CODE_128 for other numeric codes
        }

        // Alphanumeric - likely CODE_39
        if (content.matches("^[A-Za-z0-9]+$") && content.length() <= 20) {
            return 2; // CODE_39
        }

        // Content-based type detection
        if (content.startsWith("http://") || content.startsWith("https://")) {
            return 256; // URL (QR Code)
        } else if (content.startsWith("tel:")) {
            return 257; // Phone
        } else if (content.startsWith("mailto:")) {
            return 258; // Email
        } else if (content.startsWith("WIFI:")) {
            return 259; // Wi-Fi
        } else if (content.startsWith("BEGIN:VCARD")) {
            return 260; // Contact
        } else if (content.startsWith("SMSTO:")) {
            return 261; // SMS
        } else if (content.length() > 100) {
            return 256; // Long text - likely QR Code
        } else {
            return 256; // Default to QR Code
        }
    }

    /**
     * Get all scans from the database, ordered by timestamp (newest first)
     * @return List of all scans
     */
    public List<ScanItem> getAllScans() {
        Log.d(TAG, "Getting all scans from database");

        List<ScanItem> scanList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_HISTORY +
                " ORDER BY " + COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    ScanItem scan = new ScanItem();

                    // Use getColumnIndex to avoid errors
                    int idIndex = cursor.getColumnIndex(COLUMN_ID);
                    int typeIndex = cursor.getColumnIndex(COLUMN_TYPE);
                    int contentIndex = cursor.getColumnIndex(COLUMN_CONTENT);
                    int timestampIndex = cursor.getColumnIndex(COLUMN_TIMESTAMP);

                    if (idIndex != -1) scan.setId(cursor.getInt(idIndex));
                    if (typeIndex != -1) scan.setType(cursor.getInt(typeIndex));
                    if (contentIndex != -1) scan.setContent(cursor.getString(contentIndex));
                    if (timestampIndex != -1) scan.setTimestamp(cursor.getString(timestampIndex));

                    scanList.add(scan);

                    Log.d(TAG, "Loaded scan - ID: " + scan.getId() +
                            ", Type: " + scan.getType() +
                            ", Content: " + scan.getContent());

                } while (cursor.moveToNext());
            }

            Log.d(TAG, "Total scans loaded: " + scanList.size());

        } catch (Exception e) {
            Log.e(TAG, "Error getting all scans", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return scanList;
    }

    /**
     * Get a single scan by its ID
     * @param id The ID of the scan to retrieve
     * @return The ScanItem, or null if not found
     */
    public ScanItem getScanById(int id) {
        Log.d(TAG, "Getting scan by ID: " + id);

        SQLiteDatabase db = this.getReadableDatabase();
        ScanItem scanItem = null;
        Cursor cursor = null;

        try {
            String query = "SELECT * FROM " + TABLE_HISTORY +
                    " WHERE " + COLUMN_ID + " = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(id)});

            if (cursor.moveToFirst()) {
                scanItem = new ScanItem();
                scanItem.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                scanItem.setType(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
                scanItem.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)));
                scanItem.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));

                Log.d(TAG, "Found scan with ID: " + id);
            } else {
                Log.d(TAG, "No scan found with ID: " + id);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error getting scan by ID", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return scanItem;
    }

    /**
     * Delete a scan from the database
     * @param id The ID of the scan to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteScan(int id) {
        Log.d(TAG, "Deleting scan with ID: " + id);

        SQLiteDatabase db = this.getWritableDatabase();

        try {
            int rowsAffected = db.delete(TABLE_HISTORY,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)});

            Log.d(TAG, "Rows affected by delete: " + rowsAffected);
            return rowsAffected > 0;

        } catch (Exception e) {
            Log.e(TAG, "Error deleting scan", e);
            return false;
        } finally {
            db.close();
        }
    }

    /**
     * Delete all scans from the database
     * @return true if successful, false otherwise
     */
    public boolean clearAllHistory() {
        Log.d(TAG, "Clearing all history from database");

        SQLiteDatabase db = this.getWritableDatabase();

        try {
            int rowsAffected = db.delete(TABLE_HISTORY, null, null);
            Log.d(TAG, "Cleared " + rowsAffected + " rows from history");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error clearing history", e);
            return false;
        } finally {
            db.close();
        }
    }

    /**
     * Update an existing scan in the database
     * @param scanItem The scan item with updated values
     * @return true if successful, false otherwise
     */
    public boolean updateScan(ScanItem scanItem) {
        Log.d(TAG, "Updating scan with ID: " + scanItem.getId());

        if (scanItem.getId() <= 0) {
            Log.e(TAG, "Invalid scan ID for update: " + scanItem.getId());
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TYPE, scanItem.getType());
            values.put(COLUMN_CONTENT, scanItem.getContent());
            values.put(COLUMN_TIMESTAMP, scanItem.getTimestamp());

            int rowsAffected = db.update(TABLE_HISTORY, values,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(scanItem.getId())});

            Log.d(TAG, "Rows affected by update: " + rowsAffected);
            return rowsAffected > 0;

        } catch (Exception e) {
            Log.e(TAG, "Error updating scan", e);
            return false;
        } finally {
            db.close();
        }
    }

    /**
     * Get the total count of scans in the database
     * @return The number of scans
     */
    public int getScanCount() {
        Log.d(TAG, "Getting scan count");

        String countQuery = "SELECT COUNT(*) FROM " + TABLE_HISTORY;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;

        try {
            cursor = db.rawQuery(countQuery, null);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            Log.d(TAG, "Total scan count: " + count);

        } catch (Exception e) {
            Log.e(TAG, "Error getting scan count", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return count;
    }

    /**
     * Get scans filtered by type
     * @param type The type of scan (e.g., 256 for QR Code)
     * @return List of scans of the specified type
     */
    public List<ScanItem> getScansByType(int type) {
        Log.d(TAG, "Getting scans by type: " + type);

        List<ScanItem> scanList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_HISTORY +
                " WHERE " + COLUMN_TYPE + " = ?" +
                " ORDER BY " + COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(type)});

            if (cursor.moveToFirst()) {
                do {
                    ScanItem scan = new ScanItem();
                    scan.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    scan.setType(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
                    scan.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)));
                    scan.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));

                    scanList.add(scan);

                } while (cursor.moveToNext());
            }

            Log.d(TAG, "Found " + scanList.size() + " scans of type " + type);

        } catch (Exception e) {
            Log.e(TAG, "Error getting scans by type", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return scanList;
    }

    /**
     * Search scans by content (case-insensitive partial match)
     * @param searchText The text to search for
     * @return List of matching scans
     */
    public List<ScanItem> searchScans(String searchText) {
        Log.d(TAG, "Searching scans for: " + searchText);

        List<ScanItem> scanList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_HISTORY +
                " WHERE " + COLUMN_CONTENT + " LIKE ?" +
                " ORDER BY " + COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(query, new String[]{"%" + searchText + "%"});

            if (cursor.moveToFirst()) {
                do {
                    ScanItem scan = new ScanItem();
                    scan.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    scan.setType(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
                    scan.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)));
                    scan.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));

                    scanList.add(scan);

                } while (cursor.moveToNext());
            }

            Log.d(TAG, "Found " + scanList.size() + " scans matching: " + searchText);

        } catch (Exception e) {
            Log.e(TAG, "Error searching scans", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return scanList;
    }

    /**
     * Get the most recent scan
     * @return The most recent ScanItem, or null if database is empty
     */
    public ScanItem getMostRecentScan() {
        Log.d(TAG, "Getting most recent scan");

        String query = "SELECT * FROM " + TABLE_HISTORY +
                " ORDER BY " + COLUMN_TIMESTAMP + " DESC LIMIT 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        ScanItem scanItem = null;

        try {
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                scanItem = new ScanItem();
                scanItem.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                scanItem.setType(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
                scanItem.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)));
                scanItem.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));

                Log.d(TAG, "Most recent scan ID: " + scanItem.getId());
            }

        } catch (Exception e) {
            Log.e(TAG, "Error getting most recent scan", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return scanItem;
    }

    /**
     * Check if a scan with the same content already exists
     * @param content The content to check
     * @return true if exists, false otherwise
     */
    public boolean scanExists(String content) {
        Log.d(TAG, "Checking if scan exists with content: " +
                (content != null ? content.substring(0, Math.min(20, content.length())) + "..." : "null"));

        String query = "SELECT COUNT(*) FROM " + TABLE_HISTORY +
                " WHERE " + COLUMN_CONTENT + " = ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;

        try {
            cursor = db.rawQuery(query, new String[]{content});
            if (cursor.moveToFirst()) {
                exists = cursor.getInt(0) > 0;
            }

            Log.d(TAG, "Scan exists: " + exists);

        } catch (Exception e) {
            Log.e(TAG, "Error checking if scan exists", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return exists;
    }

    /**
     * Delete scans older than a specific date
     * @param daysOld Delete scans older than this many days
     * @return Number of rows deleted
     */
    public int deleteOldScans(int daysOld) {
        Log.d(TAG, "Deleting scans older than " + daysOld + " days");

        String query = "DELETE FROM " + TABLE_HISTORY +
                " WHERE " + COLUMN_TIMESTAMP + " < datetime('now', '-" + daysOld + " days')";

        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = 0;

        try {
            db.execSQL(query);

            // Get the number of rows affected
            Cursor cursor = db.rawQuery("SELECT changes()", null);
            if (cursor.moveToFirst()) {
                rowsDeleted = cursor.getInt(0);
            }
            cursor.close();

            Log.d(TAG, "Deleted " + rowsDeleted + " old scans");

        } catch (Exception e) {
            Log.e(TAG, "Error deleting old scans", e);
        } finally {
            db.close();
        }

        return rowsDeleted;
    }

    // ==================== Utility Methods ====================

    /**
     * Get current timestamp in database format
     * @return Current timestamp string
     */
    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Check if database is empty
     * @return true if empty, false otherwise
     */
    public boolean isDatabaseEmpty() {
        return getScanCount() == 0;
    }

    /**
     * Export database to a file (for debugging)
     * @param context The context
     * @return The path to the exported file, or null if failed
     */
    public String exportDatabase(Context context) {
        try {
            String dbPath = context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
            Log.d(TAG, "Database path: " + dbPath);
            return dbPath;
        } catch (Exception e) {
            Log.e(TAG, "Error getting database path", e);
            return null;
        }
    }

}