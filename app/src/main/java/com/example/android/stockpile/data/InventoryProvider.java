package com.example.android.stockpile.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.RadioGroup;

import com.example.android.stockpile.R;
import com.example.android.stockpile.data.InventoryContract.InventoryEntry;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.stockpile.data.InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME;
import static com.example.android.stockpile.data.InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE;
import static com.example.android.stockpile.data.InventoryContract.InventoryEntry.TABLE_NAME;
import static com.example.android.stockpile.data.InventoryContract.PATH_INVENTORY;


/*** {@link ContentProvider} for Inventory app.*/
//created by OlgaS Art 03/04/2019
public class InventoryProvider extends ContentProvider {

    /*
     * Tag for the log messages
     */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();
    // the # is the wild card character that can be placed with any integer
//the * is the wild card character that can be placed with any String
    //Define the oonstant integer values mapped to the code
    private static final int INVENTORY = 100;
    private static final int INVENTORY_ID = 101;
    //Define the UriMatcher as a global variable and add patterns
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static InventoryDbHelper mDbHelper;

    static {
        sURIMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, PATH_INVENTORY, INVENTORY);
        sURIMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, PATH_INVENTORY + "/#", INVENTORY_ID);
    }

    SQLiteDatabase database;
    RadioGroup rgSort;

    // Create and initialize a InventoryDbHelper object to gain access to the inventory database.
    public InventoryProvider() {
    }


    /*** Initialize the provider and the database helper object.*/
    @Override
    public boolean onCreate() {
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        boolean useAuthorityUri = false;

// Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
// This cursor will hold the result of the query
        Cursor cursor;
// Figure out if the URI matcher can match the URI to a specific code
        int match = sURIMatcher.match(uri);
        switch (match) {
            case INVENTORY:
// For the INVENTORY code, query the inventory table directly with the given
// projection, selection, selection arguments, and sort order. The cursor
// could contain multiple rows of table.
                cursor = database.query(InventoryEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null,
                        sortOrder);
                break;

            case INVENTORY_ID:
                // For the INVENTORY_ID code, extract out the ID from the URI.
// For every "?" in the selection, we need to have an element in the selection
// arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.

                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf((ContentUris.parseId(uri)))};
//query

                cursor = database.query(InventoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("No match found");
        }


        // notify all listeners of changes:
        // getContext().getContentResolver().notifyChange(uri, null);
        // if we want to be notified of any changes:
        if (useAuthorityUri) {
            cursor.setNotificationUri(
                    getContext().getContentResolver(),
                    InventoryContract.BASE_CONTENT_URI);
        } else {
            cursor.setNotificationUri(
                    getContext().getContentResolver(),
                    uri);
        }

        return cursor;

    }


    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sURIMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return insertInventory(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }


    /*** Insert a inventory into the database with the given content values. Return the new content URI
     * for that specific row in the database.*/

    private Uri insertInventory(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(COLUMN_INVENTORY_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Inventory requires a name");
        }

        // If the price is provided, check that it's greater than or equal to 0
        // Price cannot be null(!=null)
        Integer price = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Inventory requires a valid price");
        }

        // Check that the quantity is not null
        Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Inventory requires a valid quantity");
        }


        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // notify all listeners of changes:
        getContext().getContentResolver().notifyChange(uri, null);

        // Insert the new inventory with the given values
        long id = database.insert(InventoryEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sURIMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updateInventory(uri, contentValues, selection, selectionArgs);
            case INVENTORY_ID:
                // For the INVENTORY_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateInventory(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update database with the given content values.
     */
    private int updateInventory(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link InventoryEntry#COLUMN_INVENTORY_NAME} key is present,
        // check that the name value is not null.


        if (values.containsKey(COLUMN_INVENTORY_NAME)) {
            String name = values.getAsString(COLUMN_INVENTORY_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Name cannot be blank");
            }
        }


        // If the key is present, check that values are valid.
        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_PRICE)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer price = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Inventory requires a valid price");
            }
        }


        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_QUANTITY)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer price = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_QUANTITY);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Valid quantity required");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // notify all listeners of changes:
        getContext().getContentResolver().notifyChange(uri, null);

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Returns the number of database rows affected by the update statement
        // return database.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }


    /**
     * Delete the data at the given selection and selection arguments.
     */

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sURIMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sURIMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                // For the INVENTORY_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    //function used for fetching data for exporting database


    public Cursor exportQuery(Uri uri, String[] projection, String selection,
                              String[] selectionArgs, String sortOrder) {

        boolean useAuthorityUri = false;

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;
// Figure out if the URI matcher can match the URI to a specific code

        cursor = database.query(InventoryEntry.TABLE_NAME,
                projection, selection, selectionArgs, null, null,
                sortOrder);

        // notify all listeners of changes:
        // getContext().getContentResolver().notifyChange(uri, null);
        // if we want to be notified of any changes:
        if (useAuthorityUri) {
            cursor.setNotificationUri(
                    getContext().getContentResolver(),
                    InventoryContract.BASE_CONTENT_URI);
        } else {
            cursor.setNotificationUri(
                    getContext().getContentResolver(),
                    uri);
        }

        return cursor;

    }
}