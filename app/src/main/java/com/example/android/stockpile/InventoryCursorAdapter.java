package com.example.android.stockpile;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.stockpile.data.InventoryContract;

import static android.content.ContentValues.TAG;
import static com.example.android.stockpile.data.InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE;


//created by OlgaS Art 03/04/2019

/**
 * {@link InventoryCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of inventory data as its data source. This adapter knows
 * how to create list items for each row of inventory data in the {@link Cursor}.
 */

public class InventoryCursorAdapter extends CursorAdapter {
    /*** Constructs a new {@link InventoryCursorAdapter}.**/

    ImageView imageView;

    public InventoryCursorAdapter(CatalogActivity context, Cursor c) {
        super(context, c, 0 /* flags */);
    }
    /* Makes a new blank list item view. No data is set (or bound) to the views yet.
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.*/

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView nameTextView =  view.findViewById(R.id.name);
        TextView summaryTextView = view.findViewById(R.id.summary);
        TextView quantityTextView =  view.findViewById(R.id.quantity);
        ImageView imageView = view.findViewById(R.id.imageView);
        ImageButton sellOneItem = view.findViewById(R.id.sell_button);


        //Find the columns of the inventory table that we are interested in
        final int inventoryIdColumnIndex = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry._ID));
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME);
        int priceColumnIndex = cursor.getColumnIndex(COLUMN_INVENTORY_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY);
        int imageColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_IMAGE);


        // Extract properties from cursor
        String inventoryName = cursor.getString(nameColumnIndex);
        String inventoryInfo = cursor.getString(priceColumnIndex);
        final int inventoryQuantity = cursor.getInt(quantityColumnIndex);
        String imageUriString = cursor.getString(imageColumnIndex);
        Uri inventoryImageUri = Uri.parse(imageUriString);


        // Populate fields with extracted properties
        nameTextView.setText(inventoryName);
        summaryTextView.setText(String.valueOf(inventoryInfo));
        quantityTextView.setText(String.valueOf(inventoryQuantity));
        imageView.setImageURI(inventoryImageUri);


        //decrease the amount of quantity by one on button click
        sellOneItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri productUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, inventoryIdColumnIndex);
                changeInventoryQuantity(context, productUri, inventoryQuantity);
            }
        });
    }

    /**
     * @param context                   is the activity context
     * @param productUri                is an Uri used to update the inventory in the ListView for a specific item
     * @param existingInventoryQuantity - existing amount of a specific inventory
     */
    public void changeInventoryQuantity(Context context, Uri productUri, int existingInventoryQuantity) {

        // Subtract 1 from current value if quantity of product >= 1
        int newQuantityValue = (existingInventoryQuantity >= 1) ? existingInventoryQuantity - 1 : 0;

        if (existingInventoryQuantity == 0) {
            Toast.makeText(context.getApplicationContext(), R.string.toast_out_of_stock_msg, Toast.LENGTH_SHORT).show();
        }

        // Update table with the new value of quantity
        ContentValues contentValues = new ContentValues();
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY, newQuantityValue);
        int numRowsUpdated = context.getContentResolver().update(productUri, contentValues, null, null);
        if (numRowsUpdated > 0) {
            // Show error message in Logs
            Log.i(TAG, context.getString(R.string.purchase_confirmation));
        } else {
            Toast.makeText(context.getApplicationContext(), R.string.no_inventory, Toast.LENGTH_SHORT).show();
            // Show error message in Logs with info about fail update.
            Log.e(TAG, context.getString(R.string.no_inventory));
        }


    }

}