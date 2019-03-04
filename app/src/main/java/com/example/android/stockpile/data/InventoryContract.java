package com.example.android.stockpile.data;

import android.net.Uri;
import android.provider.BaseColumns;
import android.content.ContentResolver;

//created by OlgaS Art 03/04/2019
public final class InventoryContract {
    /*** The "Content authority" is a name for the entire content provider, should be unique*/
    public static final String CONTENT_AUTHORITY = "com.example.android.stockpile";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.stockpile/inventory/ is a valid path for
     * looking at inventory data. content://com.example.android.stockpile/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_INVENTORY = "inventory";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.

    private InventoryContract() {
    }

    public static final class InventoryEntry implements BaseColumns {
        /**
         * Inner class that defines constant values for the inventory database table.
         * Each entry in the table represents a single inventory.
         */

        // * The MIME type of the {@link #CONTENT_URI} for a list of inventory.

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single inventory.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;
        /**
         * The content URI to access the inventory data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);
        //Table name
        public static final String TABLE_NAME = "inventory";
        //Table columns
        public static final String _ID = BaseColumns._ID; //type INTEGER - PRIMARY KEY AUTOINCREMENT - menas that the id number is assigned automatically incrementally
        public static final String COLUMN_INVENTORY_NAME = "name"; // type TEXT - NOT NULL - means the field cannot be left blank
        public static final String COLUMN_INFO = "information"; //type TEXT BREED
        public static final String COLUMN_INVENTORY_PRICE = "price"; //type INTEGER PRICE
        public static final String COLUMN_INVENTORY_CATEGORY = "category"; //type INTEGER - NOT NULL - means the field cannot be left blank - DEFAULT 0 - menas if no data entered, default is UNKNOWN gender.
        public static final String COLUMN_INVENTORY_QUANTITY = "no_of_inventory"; //type INTEGER QUANTITY
        public static final String COLUMN_INVENTORY_IMAGE = "image";
        public static final String COLUMN_INVENTORY_PHONE = "supplier_phone";
        public static final String COLUMN_INVENTORY_EMAIL = "supplier_email";

        /*Possible values for the CATEGORY*/
        public static final int CATEGORY_HOME = 1;
        public static final int CATEGORY_OFFICE = 2;
        public static final int CATEGORY_UNKNOWN = 0;


        //whether or not the given category is {@link #CATEGORY_UNKNOWN}, {@link #CATEGORY_HOME} or {@link #CATEGORY_OFFICE}.

        public static boolean isValidCategory(int gender) {
            if (gender == CATEGORY_UNKNOWN || gender == CATEGORY_HOME || gender == CATEGORY_OFFICE) {
                return true;
            }
            return false;
        }
    }

}

