package com.aero.umich.passwordsaver;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.support.design.widget.FloatingActionButton;

import com.aero.umich.passwordsaver.database.PasswordContract;
import com.aero.umich.passwordsaver.utils_xls.ExcelFileInstance;

public class MainActivity extends AppCompatActivity implements
        PasswordAdapter.ListItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    // create tag for log purpose
    private static final String TAG = MainActivity.class.getSimpleName();

    // This ID will be used to identify the Loader responsible for loading our weather forecast
    private static final int TASK_LOADER_ID = 2320;

    // for RecyclerView use
    private PasswordAdapter mAdapter;
    private RecyclerView mPasswordRecyclerView;

    // display toast message
    private Toast mToast;

    ExcelFileInstance mExcelInstance = new ExcelFileInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPasswordRecyclerView = (RecyclerView) findViewById(R.id.rv_display_password);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mPasswordRecyclerView.setLayoutManager(layoutManager);

        //  Use this setting to improve performance if you know that changes in content do not
        //  change the child layout size in the RecyclerView
        mPasswordRecyclerView.setHasFixedSize(true);

        mAdapter = new PasswordAdapter(this, this);
        mPasswordRecyclerView.setAdapter(mAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

                // implement swipe to delete
                int id = (int) viewHolder.itemView.getTag();

                // Build appropriate uri with String row id appended
                Uri uri = PasswordContract.PasswordEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(Integer.toString(id)).build();

                getContentResolver().delete(uri, null, null);

                // Restart the loader to re-query for all tasks after a deletion
                getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, MainActivity.this);
            }
        }).attachToRecyclerView(mPasswordRecyclerView);

        /*
         Set the Floating Action Button (FAB) to its corresponding View.
         Attach an OnClickListener to it, so that when it's clicked, a new intent will be created
         to launch the AddTaskActivity.
         */
        FloatingActionButton fabButton = (FloatingActionButton) findViewById(R.id.fab);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addTaskIntent = new Intent(MainActivity.this, AddPasswordActivity.class);
                startActivity(addTaskIntent);
            }
        });

        /*
         * Ensures a loader is initialized and active. If the loader doesn't already exist, one is
         * created and (if the activity/fragment is currently started) starts the loader. Otherwise
         * the last created loader is re-used.
         */
        getSupportLoaderManager().initLoader(TASK_LOADER_ID, null, this);
        setupSharedPreferences();

        boolean flag = isStoragePermissionGranted();
        Log.d(TAG, "Storage permission: " + flag);
    }

    /**
     * This method is called after this activity has been paused or restarted.
     * Often, this is after new data has been inserted through an AddTaskActivity,
     * so this restarts the loader to re-query the underlying data for any changes.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // re-queries for all tasks
        getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Uri uri = PasswordContract.PasswordEntry.CONTENT_URI;

        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_settings:

                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.action_sync_database:

                mExcelInstance.ReadXlsFile();

                int nSize = mExcelInstance.mPasswordArray.size();
                ContentValues[] contentValues = mExcelInstance.mPasswordArray.toArray(new ContentValues[nSize]);

                getContentResolver().bulkInsert(uri, contentValues);

                return true;

            case R.id.action_save_xlsx:

                Cursor cursor = getContentResolver().query(uri, null, null, null, PasswordContract.PasswordEntry.COLUMN_SECURITY);

                String OutputFileName = "output.xlsx";
                mExcelInstance.WriteXlsFile(OutputFileName, cursor);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(int itemId) {

        // Build appropriate uri with String row id appended
        Uri uri = PasswordContract.PasswordEntry.CONTENT_URI;
        uri = uri.buildUpon().appendPath(Integer.toString(itemId)).build();

        // Launch AddTaskActivity adding the itemId as an extra in the intent
        Intent intent = new Intent(MainActivity.this, AddPasswordActivity.class);

        intent.putExtra(AddPasswordActivity.UPDATE_PASSWORD_INSTANCE_KEY, itemId);
        intent.setData(uri);

        startActivity(intent);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, final Bundle args) {

        switch (loaderId) {

            case TASK_LOADER_ID:

                return new CursorLoader(this,
                        PasswordContract.PasswordEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        PasswordContract.PasswordEntry.COLUMN_SECURITY);

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }


    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            default:
                break;
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    void changeBackgroundColor(SharedPreferences sharedPreferences) {

        String colorIndex = sharedPreferences.getString(getString(R.string.pref_colors_key),
                getString(R.string.pref_color_value_white));
        
        FrameLayout bgElement = (FrameLayout) findViewById(R.id.main_activity_id);

        int res_color = R.color.color_white;
        switch (colorIndex) {
            case "white":
                res_color = R.color.color_white;
                break;
            case "pink":
                res_color = R.color.color_pink;
                break;
            case "grey":
                res_color = R.color.color_grey;
                break;
            case "yellow":
                res_color = R.color.color_yellow;
                break;
            default:
                break;
        }

        bgElement.setBackgroundColor(getResources().getColor(res_color));
    }


    private void setupSharedPreferences() {
        // Get all of the values from shared preferences to set it up
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        changeBackgroundColor(sharedPreferences);

        // Register the listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    // Updates the screen if the shared preferences change. This method is required when you make a
    // class implement OnSharedPreferenceChangedListener
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_colors_key))) {
            changeBackgroundColor(sharedPreferences);
        }
    }
}
