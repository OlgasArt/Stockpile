package com.example.android.stockpile.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.stockpile.data.InventoryContract.InventoryEntry;
import static com.example.android.stockpile.data.InventoryContract.InventoryEntry.TABLE_NAME;
import static com.example.android.stockpile.data.InventoryContract.InventoryEntry._ID;

/**
 * Database helper for Inventory app. Manages database creation and version management.
 */
//created by OlgaS Art 03/04/2019
public class InventoryDbHelper extends SQLiteOpenHelper {


    public static final String LOG_TAG = InventoryDbHelper.class.getSimpleName();
    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "inventory.db";
    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link InventoryDbHelper}.
     *
     * @param context of the app
     */
    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the inventory table
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COLUMN_INVENTORY_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_INFO + " TEXT, "
                + InventoryEntry.COLUMN_INVENTORY_CATEGORY + " INTEGER NOT NULL, "
                + InventoryEntry.COLUMN_INVENTORY_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryEntry.COLUMN_INVENTORY_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryEntry.COLUMN_INVENTORY_IMAGE + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_INVENTORY_PHONE + " TEXT, "
                + InventoryEntry.COLUMN_INVENTORY_EMAIL + " TEXT );";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    /*** This is called when the database needs to be upgraded.*/
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }

}