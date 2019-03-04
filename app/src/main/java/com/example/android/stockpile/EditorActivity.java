package com.example.android.stockpile;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.stockpile.data.InventoryContract;
import com.example.android.stockpile.data.InventoryContract.InventoryEntry;

//created by OlgaS Art 03/04/2019
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    /** Initialize loader*/
    /**
     * Request an image from gallery.  a static int, any request number of choice
     */
    private static final int SELECT_AN_IMAGE_REQUEST = 3;
    /**
     * Request to read internal storage of the phone
     */
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 1;
    /*** Identifier for the inventory data loader*/
    private static final int EXISTING_INVENTORY_LOADER = 0;
    /*** ImageView to dislay the inventory's image selected from the computer*/
    ImageView mPhotoImageView;
    Uri actualUri;
    ImageButton uploadImage;
    // Defines a variable to contain the number of rows deleted
    int mRowsDeleted = 0;
    // Defines selection criteria for the rows you want to delete
    String mSelectionClause = InventoryContract.PATH_INVENTORY;
    String[] mSelectionArgs = {""};
    // Boolean flag for required fields,TRUE if these fields have been populated
    boolean hasAllRequiredValues = false;
    /**
     * This method is called when Plus (+) is clicked.
     * This method restricts you to only using numbers below 50
     */
    int quantity = 0;
    /*** Content URI for the existing inventory (null if it's a new inventory)*/
    private Uri mCurrentInventoryUri;
    /*** EditText field to enter the inventory's name*/
    private EditText mNameEditText;
    /*** EditText field to enter the inventory's price*/
    private EditText mPriceEditText;
    /*** EditText field to enter the upplier's phone*/
    private EditText mSupplierPhoneEditText;

    //public static Uri publicSelectedImage;
    /*** EditText field to enter the supplier's email*/
    private EditText mSupplierEmailEditText;
    /*** EditText field to enter the inventory's additional information*/
    private EditText mAdditionalInfoEditText;
    /*** Spinner field to enter the inventory's category*/
    private Spinner mCategorySpinner;
    /*** TextView field to increase or decrease the quantity of the inventory*/
    private TextView mQuantityTextView;
    /*** URI of product image*/
    private Uri mImageUri;
    /**
     * Category of the inventory
     * Valid values are in the InventoryContract.java file:
     * {@link InventoryEntry#CATEGORY_UNKNOWN}, {@link InventoryEntry#CATEGORY_HOME}, or
     * {@link InventoryEntry#CATEGORY_OFFICE}.
     */

    private int mCategory = InventoryEntry.CATEGORY_UNKNOWN;
    /**
     * Boolean flag that keeps track of whether the inventory has been edited (true) or not (false)
     */
    private boolean mInventoryHasChanged = false;
    /**
     * OnTouchListener that listens for any user touches on a View,
     * implying that they are modifying the view, and we change the mInventoryHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mInventoryHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //Examine the intent that was used to launch this activity,
        //in order to figure out if we're creating a new inventory or editing an existing one.
        Intent intent = getIntent();
        mCurrentInventoryUri = intent.getData();

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_inventory_name);
        mAdditionalInfoEditText = (EditText) findViewById(R.id.edit_inventory_info);
        mPriceEditText = (EditText) findViewById(R.id.edit_price);
        mCategorySpinner = (Spinner) findViewById(R.id.spinner_category);
        mQuantityTextView = (TextView) findViewById(R.id.quantity);
        uploadImage = (ImageButton) findViewById(R.id.uploadImage); // upload image button
        mPhotoImageView = (ImageView) findViewById(R.id.imageView); //image view
        mSupplierPhoneEditText = (EditText) findViewById(R.id.edit_phone_contact);//supplier phone number
        mSupplierEmailEditText = (EditText) findViewById(R.id.edit_email_contact);//supplier email adress


        //if intent DOES NOT contain an inventory content URI, then we know we are creating a new one:
        if (mCurrentInventoryUri == null) {   //This is a new inventory, so change the label accordingly
            setTitle(getString(R.string.editor_activity_title_new_inventory));
            // Invalidate the options menu, so the "Delete" menu option can be hidden (no need for ''delete'' for new item.
            invalidateOptionsMenu();


            //Set custom image on the opening of the new inventory
            mPhotoImageView.setImageResource(R.drawable.ic_inventory_empty);

        } else { // Otherwise, this is an existing inventory
            setTitle(getString(R.string.editor_activity_title_edit_inventory)); // Initialize a loader to read the inventory data from the database and display the current values in the editor
            //disable upload button as an image has been already selected
            // uploadImage.setEnabled(false);

            //Hide the upload image button as this is an existing item
            uploadImage.setVisibility(View.GONE);


            getSupportLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);
        }


        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mAdditionalInfoEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mCategorySpinner.setOnTouchListener(mTouchListener);
        mQuantityTextView.setOnTouchListener(mTouchListener);
        mPhotoImageView.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);
        mSupplierEmailEditText.setOnTouchListener(mTouchListener);

        setupSpinner();


        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInventoryHasChanged = true;
                trySelectImage();
            }
        });
    }

    public void trySelectImage() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }
        openSelector();
    }

    private void openSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType(getString(R.string.intent_type));
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), SELECT_AN_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //upon permisison granted, selector is opened
                    openSelector();
                }
        }
    }


    /**
     * Setup the increment/decrement button clicks to increase or decrease the quantity of the inventory
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_AN_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {

            if (data != null) {
                mImageUri = data.getData();
                mPhotoImageView.setImageURI(mImageUri);
                mPhotoImageView.invalidate();
            }
        }
    }

    public void increment(View view) {
        if (quantity == 50) {
            //Show an error message as a Toast
            Toast.makeText(this, "The number of inventory cannot exceed 50", Toast.LENGTH_SHORT).show();
            //Exit this method early as there is nothing left to do
            return;
        }
        quantity = quantity + 1;
        displayQuantity(quantity);
    }

    /**
     * This method is called when Minus (-) is clicked.
     * This method restricts you to only using positive numbers
     */
    public void decrement(View view) {
        if (quantity == 0) {
            //Show an error message as a Toast
            Toast.makeText(this, "The number of inventory cannot be negative", Toast.LENGTH_SHORT).show();
            //Exit this method early as there is nothing left to do
            return;
        }

        quantity = quantity - 1;
        displayQuantity(quantity);
    }


    /**
     * This method displays the given quantity value on the screen.
     */
    private void displayQuantity(int number) {
        TextView quantityTextView = (TextView) findViewById(R.id.quantity);
        quantityTextView.setText("" + number);
    }


    /**
     * Setup the dropdown spinner that allows the user to select the gender of the inventory.
     */

    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter categorySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_category_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mCategorySpinner.setAdapter(categorySpinnerAdapter);

        // Set the integer mSelected to the constant values
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.category_home))) {
                        mCategory = InventoryEntry.CATEGORY_HOME;
                    } else if (selection.equals(getString(R.string.category_office))) {
                        mCategory = InventoryEntry.CATEGORY_OFFICE;
                    } else {
                        mCategory = InventoryEntry.CATEGORY_UNKNOWN;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCategory = InventoryEntry.CATEGORY_UNKNOWN;
            }
        });
    }

    /**
     * * Get user input from editor and save inventory into database.
     */


    private boolean saveInventory() {

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String infoString = mAdditionalInfoEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityTextView.getText().toString().trim();
        String phoneString = mSupplierPhoneEditText.getText().toString().trim();
        String emailString = mSupplierEmailEditText.getText().toString().trim();
        String image = mImageUri.toString();

        {

            // Check if this is supposed to be a new inventory and check if all the fields in the editor are blank
            if (mCurrentInventoryUri == null &&
                    TextUtils.isEmpty(nameString) &&
                    TextUtils.isEmpty(infoString) &&
                    TextUtils.isEmpty(priceString) &&
                    TextUtils.isEmpty(quantityString) &&
                    TextUtils.isEmpty(phoneString) &&
                    TextUtils.isEmpty(emailString) &&
                    mImageUri == null &&
                    mCategory == InventoryEntry.CATEGORY_UNKNOWN) {
                // Since no fields were modified, we can return early without creating a new inventory.
                // No need to create ContentValues and no need to do any ContentProvider operations.
                hasAllRequiredValues = true;
                return hasAllRequiredValues;
            }

            /// Create a ContentValues object where column names are the keys,
            // and inventory attributes from the editor are the values.
            ContentValues values = new ContentValues();

            // Required values to be populated by the user: name and image
            if (TextUtils.isEmpty(nameString)) {
                Toast.makeText(this, getString(R.string.name_required), Toast.LENGTH_SHORT).show();
                return hasAllRequiredValues;
            } else {
                values.put(InventoryEntry.COLUMN_INVENTORY_NAME, nameString);
            }

            if (mImageUri == null) {
                Toast.makeText(this, getString(R.string.image_required), Toast.LENGTH_SHORT).show();
                return hasAllRequiredValues;
            } else {
                values.put(InventoryEntry.COLUMN_INVENTORY_IMAGE, image);
            }

            // Optional values
            values.put(InventoryEntry.COLUMN_INVENTORY_NAME, nameString);
            values.put(InventoryEntry.COLUMN_INFO, infoString);
            values.put(InventoryEntry.COLUMN_INVENTORY_CATEGORY, mCategory);
            values.put(InventoryEntry.COLUMN_INVENTORY_PHONE, phoneString);
            values.put(InventoryEntry.COLUMN_INVENTORY_EMAIL, emailString);


            // If the price is not provided by the user, don't try to parse the string into an
            // integer value. Use 0 by default.
            int price = 0;
            if (!TextUtils.isEmpty(priceString)) {
                price = Integer.parseInt(priceString);
            }
            values.put(InventoryEntry.COLUMN_INVENTORY_PRICE, price);

            // If the quantity is not provided by the user, don't try to parse the string into an
            // integer value. Use 0 by default.
            int quantity = 0;
            if (!TextUtils.isEmpty(quantityString)) {
                quantity = Integer.parseInt(quantityString);
            }
            values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, quantity);

            /// Determine if this is a new or existing inventory by checking if mCurrentInventoryUri is null or not
            if (mCurrentInventoryUri == null) {
                // This is a NEW inventory, so insert a new inventory into the provider,
                // returning the content URI for the new inventory.
                Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful.
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.editor_insert_inventory_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_insert_inventory_successful),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                // Otherwise this is an EXISTING inventory, so update the inventory with content URI: mCurrentInventoryUri
                // and pass in the new ContentValues. Pass in null for the selection and selection args
                // because mCurrentInventoryUri will already identify the correct row in the database that
                // we want to modify.
                int rowsAffected = getContentResolver().update(mCurrentInventoryUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, getString(R.string.editor_update_inventory_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_update_inventory_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }
            hasAllRequiredValues = true;
            return hasAllRequiredValues;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }


    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new inventory
        if (mCurrentInventoryUri == null) {
            //hide the "Delete" menu item
            MenuItem deleteMenuItem = menu.findItem(R.id.action_delete);
            deleteMenuItem.setVisible(false);
            //hide the "Order More" menu item
            MenuItem orderMenuItem = menu.findItem(R.id.action_order);
            orderMenuItem.setVisible(false);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save inventory to database
                saveInventory();
                if (hasAllRequiredValues == true) {
                    // Exit activity
                    finish();
                }
                return true;

            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Order Additional" menu option
            case R.id.action_order:
                // Pop up confirmation dialog for ordering additional inventory
                orderMoreInventory();
                return true;

            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                // NavUtils.navigateUpFromSameTask(this);
                // If the inventory hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mInventoryHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the inventory hasn't changed, continue with handling back button press
        if (!mInventoryHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {


        // Since the editor shows all inventory attributes, define a projection that contains
        // all columns from the inventory table
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_NAME,
                InventoryEntry.COLUMN_INFO,
                InventoryEntry.COLUMN_INVENTORY_CATEGORY,
                InventoryEntry.COLUMN_INVENTORY_PRICE,
                InventoryEntry.COLUMN_INVENTORY_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_IMAGE,
                InventoryEntry.COLUMN_INVENTORY_PHONE,
                InventoryEntry.COLUMN_INVENTORY_EMAIL
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentInventoryUri,         // Query the content URI for the current inventory
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }


    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of inventory attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME);
            int infoColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INFO);
            int categoryColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_CATEGORY);
            int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_QUANTITY);
            int phoneColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PHONE);
            int emailColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_EMAIL);
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_IMAGE);


            //correct
            // mPhotoImageView.setImageURI(Uri.parse(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_IMAGE))));

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String info = cursor.getString(infoColumnIndex);
            int gender = cursor.getInt(categoryColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplierPhone = cursor.getString(phoneColumnIndex);
            String supplierEmail = cursor.getString(emailColumnIndex);
            String imageUriString = cursor.getString(imageColumnIndex);
            mImageUri = Uri.parse(imageUriString);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mAdditionalInfoEditText.setText(info);
            mPriceEditText.setText(Integer.toString(price));
            mQuantityTextView.setText(Integer.toString(quantity));
            mSupplierPhoneEditText.setText(supplierPhone);
            mSupplierEmailEditText.setText(supplierEmail);
            mPhotoImageView.setImageURI(mImageUri);


            // Category is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Home, 2 is Office).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (gender) {
                case InventoryEntry.CATEGORY_HOME:
                    mCategorySpinner.setSelection(1);
                    break;
                case InventoryEntry.CATEGORY_OFFICE:
                    mCategorySpinner.setSelection(2);
                    break;
                default:
                    mCategorySpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mAdditionalInfoEditText.setText("");
        mPriceEditText.setText("");
        mCategorySpinner.setSelection(0); // Select "Unknown" gender
        mQuantityTextView.setText("");
        mSupplierEmailEditText.setText("");
        mSupplierPhoneEditText.setText("");
        mPhotoImageView.setImageURI(Uri.parse(""));
    }


    /**
     * Open email application to order additional inventory
     */

    public void orderMoreInventory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.order_message);
        builder.setPositiveButton(R.string.via_phone, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // intent to phone
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + mSupplierPhoneEditText.getText().toString().trim()));
                startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.via_email, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // intent to email
                Intent intent = new Intent(android.content.Intent.ACTION_SENDTO);
                intent.setType("text/plain");
                intent.setData(Uri.parse("mailto:" + mSupplierEmailEditText.getText().toString().trim()));
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "New Inventory Order");
                String bodyMessage = "Dear All," +
                        "\n" +
                        "Please send us more of the following item: " +
                        "\n" +
                        mNameEditText.getText().toString().trim() +
                        "\n" +
                        "Additional details: " + mAdditionalInfoEditText.getText().toString().trim() +
                        "\n" +
                        "Quantity: " +
                        "\n" +
                        "Thank you";
                intent.putExtra(android.content.Intent.EXTRA_TEXT, bodyMessage);
                startActivity(intent);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the inventory.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the inventory.
                deleteInventory();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the inventory.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the inventory in the database.
     */
    private void deleteInventory() {
        // Only perform the delete if this is an existing inventory.
        if (mCurrentInventoryUri != null) {
            // Call the ContentResolver to delete the inventory at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentInventoryUri
            // content URI already identifies the inventory that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentInventoryUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_inventory_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_inventory_successful),
                        Toast.LENGTH_SHORT).show();
            }
            // Close the activity
            finish();

        }


    }


}
