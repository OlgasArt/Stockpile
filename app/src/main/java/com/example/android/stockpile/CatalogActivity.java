package com.example.android.stockpile;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.example.android.stockpile.data.InventoryContract;
import com.example.android.stockpile.data.InventoryContract.InventoryEntry;
import com.example.android.stockpile.data.InventoryProvider;

import android.support.design.widget.FloatingActionButton;

import static com.example.android.stockpile.data.InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE;

//created by OlgaS Art 03/04/2019

/**
 * Displays list of inventory that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //** Tag for the log messages */
    public static final String LOG_TAG = CatalogActivity.class.getSimpleName();
    /**
     * Constant value for the INVENTORY LOADER loader ID
     */
    private static final int INVENTORY_LOADER_ID = 0;
    /**
     * Adapter
     */
    InventoryCursorAdapter mCursorAdapter;

    /**
     * DB helper
     **/
    private InventoryProvider provider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);


        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find ListView to populate
        ListView inventoryListView = findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);

        // Setup cursor adapter using cursor from last step
        mCursorAdapter = new InventoryCursorAdapter(this, null);
        // Attach cursor adapter to the ListView
        inventoryListView.setAdapter(mCursorAdapter);

        //Setup item click listener
        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ////create a new intent to go to EditorActivity
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                //form the content URI that represents the specific inventory clicked on by adding "id" (passed as input to this method) onto the PentEntry.Content_URI
                //For example, uri would be "content://com.example.android.stockpile/stockpile/2""
                //if inventory id 2 was clicked
                Uri currentInventoryUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);
                //Set the URI on the data field of the intent
                intent.setData(currentInventoryUri);
                //Launch EditorActivity to display the data for the current inventory
                startActivity(intent);
            }

        });
        //Kick off the loader
        getSupportLoaderManager().initLoader(INVENTORY_LOADER_ID, null, this);
    }


    /**
     * Helper method to insert hardcoded inventory data into the database. For debugging purposes only.
     */
    private void insertDummyInventory() {
        // Create a ContentValues object where column names are the keys,
        // and smartphone's inventory attributes are the values.

        ContentValues values = new ContentValues();

        values.put(InventoryEntry.COLUMN_INVENTORY_NAME, "Samsung Galaxy S9");
        values.put(InventoryEntry.COLUMN_INFO, "Android 8 Oreo, 64 GB storage space, 12 Mpix camera");
        values.put(InventoryEntry.COLUMN_INVENTORY_CATEGORY, InventoryEntry.CATEGORY_HOME);
        values.put(COLUMN_INVENTORY_PRICE, 700);
        values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, 1);
        values.put(InventoryEntry.COLUMN_INVENTORY_IMAGE, "android.resource://com.example.android.stockpile/drawable/samsung_galaxy");
        values.put(InventoryEntry.COLUMN_INVENTORY_PHONE, "999555888");
        values.put(InventoryEntry.COLUMN_INVENTORY_EMAIL, "example@dot.com");

        // Receive the new content URI that will allow us to access Dummy Inventory data in the future.
        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }


    /**
     * Helper method to delete all inventory in the database.
     * Why the content URI? Because that’s the generic __/inventory uri which in our content provider will delete all inventory.
     */
    private void deleteAllInventory() {
        int rowsDeleted = getContentResolver().delete(InventoryContract.InventoryEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from inventory database");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertDummyInventory();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllInventory();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
//define the projection - columns that we care about
        String[] projection = {
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME,
                COLUMN_INVENTORY_PRICE,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY,
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_IMAGE,
        };

        String selection = null;

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(this,
                InventoryEntry.CONTENT_URI,
                projection,
                selection,
                null,
                null);
    }


    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);

    }


}