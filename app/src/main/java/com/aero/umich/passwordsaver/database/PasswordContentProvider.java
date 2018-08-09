/*
 * HUI ZHANG's Project
 * Copyrights belongs to Hui ZHANG (2018)
 *   for use, please contact him at ericzhng@umich.edu
 * */

package com.aero.umich.passwordsaver.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.net.URI;
import java.net.URISyntaxException;

import static android.provider.BaseColumns._ID;
import static com.aero.umich.passwordsaver.database.PasswordContract.PasswordEntry.TABLE_NAME;


/**
 * This class serves as the ContentProvider for all of PasswordSaver's data. This class allows us to
 * insert or bulkInsert data, query data, and delete data.
 */

public
class PasswordContentProvider extends ContentProvider {

    // create tag for log purpose
    private static final String TAG = PasswordContentProvider.class.getSimpleName();

    // Define final integer constants for the directory of passwords and a single item.
    // It's convention to use 100, 200, 300, etc for directories,
    // and related ints (101, 102, ..) for items in that directory.
    public static final int CODE_PASSWORDS = 100;
    public static final int CODE_PASSWORD_WITH_ID = 101;

    // Member variable for a TaskDbHelper that's initialized in the onCreate() method
    private PasswordDbHelper mPasswordDbHelper;


    // Declare a static variable for the Uri matcher that you construct
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    // Define a static buildUriMatcher method that associates URI's with their int match
    public static UriMatcher buildUriMatcher() {

        // Initialize a UriMatcher with no matches by passing in NO_MATCH to the constructor
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        /*
          All paths added to the UriMatcher have a corresponding int.
          For each kind of uri you may want to access, add the corresponding match with addURI.
          The two calls below add matches for the passwords directory and a single item by ID.
         */
        uriMatcher.addURI(PasswordContract.AUTHORITY, PasswordContract.PATH_PASSWORD, CODE_PASSWORDS);
        uriMatcher.addURI(PasswordContract.AUTHORITY, PasswordContract.PATH_PASSWORD + "/#", CODE_PASSWORD_WITH_ID);

        return uriMatcher;
    }


    // onCreate() initializes a PasswordDbHelper on startup
    @Override
    public boolean onCreate() {
        mPasswordDbHelper = new PasswordDbHelper(getContext());
        return true;
    }


    // Implement insert to handle requests to insert a single new row of data
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {

        // Get access to the task database (to write new data to)
        final SQLiteDatabase db = mPasswordDbHelper.getWritableDatabase();

        // Write URI matching code to identify the match for the tasks directory
        switch (sUriMatcher.match(uri)) {
            
            case CODE_PASSWORDS:

                // Insert new value into the database
                long id = db.insert(TABLE_NAME, null, values);

                // check whether it is successful
                if (id > 0) {
                    // URI to be returned
                    Uri returnUri = ContentUris.withAppendedId(PasswordContract.PasswordEntry.CONTENT_URI, id);

                    // Notify the resolver if the uri has been changed, and return the newly inserted URI
                    getContext().getContentResolver().notifyChange(uri, null);

                    // Return constructed uri (this points to the newly inserted row of data)
                    return returnUri;

                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }

            // Set the value for the returnedUri and write the default case for unknown URI's
            // Default case throws an UnsupportedOperationException
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }


    // Handles requests to insert a set of new rows.
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {

        // Get access to the task database (to write new data to)
        final SQLiteDatabase db = mPasswordDbHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {

            case CODE_PASSWORDS:

                db.beginTransaction();

                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {

                        long id = db.insert(TABLE_NAME,
                                null,
                                value);

                        if (id > 0) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                }
                finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;

            default:
                return super.bulkInsert(uri, values);
        }
    }


    // Implement query to handle requests for data by URI
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor returnCursor;

        // Get access to underlying database (read-only for query)
        final SQLiteDatabase db = mPasswordDbHelper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(PasswordContract.PasswordEntry.TABLE_NAME);

        // Query for the tasks directory and write a default case
        switch (sUriMatcher.match(uri)) {

            // Query for the tasks directory
            case CODE_PASSWORDS:

                break;

            case CODE_PASSWORD_WITH_ID:

                // Get the task ID from the URI path
                String id = uri.getLastPathSegment();
                builder.appendWhere(PasswordContract.PasswordEntry._ID + " = " + id);

                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        returnCursor = builder.query(db,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the desired Cursor
        return returnCursor;
    }


    // Implement delete to delete a single row of data
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        // Keep track of the number of deleted tasks
        int numRowsDeleted;

        /*
         * If we pass null as the selection to SQLiteDatabase#delete, our entire table will be
         * deleted. However, if we do pass null and delete all of the rows in the table, we won't
         * know how many rows were deleted. According to the documentation for SQLiteDatabase,
         * passing "1" for the selection will delete all rows and return the number of rows
         * deleted, which is what the caller of this method expects.
         */
        if (null == selection) selection = "1";

        // Get access to the database and write URI matching code to recognize a single item
        final SQLiteDatabase db = mPasswordDbHelper.getWritableDatabase();

        // Write the code to delete a single row of data
        switch (sUriMatcher.match(uri)) {

            // Handle the single item case, recognized by the ID included in the URI path
            case CODE_PASSWORD_WITH_ID:

                // Get the task ID from the URI path
                String id = uri.getPathSegments().get(1);

                // same purposes, Get the task ID
                // long id =  ContentUris.parseId(uri);

                // Use selections/selectionArgs to filter for this ID
                numRowsDeleted = db.delete(TABLE_NAME,
                        "_id=?",
                        new String[]{id});
                break;

            case CODE_PASSWORDS:

                numRowsDeleted = db.delete(TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver of a change and return the number of items deleted
        if (numRowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        // Return the number of tasks deleted
        return numRowsDeleted;
    }


    // Implement update to update a single row of data
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        // Get access to the database and write URI matching code to recognize a single item
        final SQLiteDatabase db = mPasswordDbHelper.getWritableDatabase();
        int updateCounts = 0;

        switch (sUriMatcher.match(uri)) {

            case CODE_PASSWORD_WITH_ID:

                // Get the task ID from the URI path
                String id = uri.getLastPathSegment();

                String selection_new = PasswordContract.PasswordEntry._ID + " = " + id;

                if (!TextUtils.isEmpty(selection)) {
                    selection_new += " AND " + selection;
                }

                updateCounts = db.update(TABLE_NAME, values, selection_new, selectionArgs);
                break;

            case CODE_PASSWORDS:

                updateCounts =  db.update(TABLE_NAME, values, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (updateCounts > 0)  {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return updateCounts;
    }


    @Override
    public String getType(@NonNull Uri uri) {

        switch (sUriMatcher.match(uri)) {

            case CODE_PASSWORD_WITH_ID:

                return "vnd.android.cursor.item/vnd.com.aero.umich.passwordsaver.passwords";

            case CODE_PASSWORDS:

                return "vnd.android.cursor.dir/vnd.com.aero.umich.passwordsaver.passwords";

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }
}
