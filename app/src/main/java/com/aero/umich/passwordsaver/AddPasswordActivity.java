package com.aero.umich.passwordsaver;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.aero.umich.passwordsaver.database.PasswordContract;

import java.util.Date;

import static com.aero.umich.passwordsaver.utils_xls.DateUtils.toDate;
import static com.aero.umich.passwordsaver.utils_xls.DateUtils.toDateString;
import static com.aero.umich.passwordsaver.utils_xls.DateUtils.toTimestamp;


public
class AddPasswordActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    // Extra for the task ID to be received in the intent
    public static final String UPDATE_PASSWORD_INSTANCE_KEY = "UpdatePassword";
    // Extra for the task ID to be received after rotation
    public static final String NEW_PASSWORD_INSTANCE_KEY = "NewPassword";

    // Constant for default task id to be used when not in update mode
    private static final int DEFAULT_NEW_INSTANCE_ID = -1;
    private int mActivityMode = DEFAULT_NEW_INSTANCE_ID;

    private static final int LOADER_ADD_ACTIVITY_ID = 353;

    // Fields for views
    private EditText mAccountInfo;
    private EditText mUsername;
    private EditText mEmailOnProfile;
    private EditText mPassword;
    private RadioGroup mRadioGroup;
    private EditText mDescription;
    private TextView mTimeStamp;

    /* The URI that is used to access the details */
    private Uri mUri;

    // Constants for priority
    public static final int LEVEL_HIGH = 1;
    public static final int LEVEL_MEDIUM = 2;
    public static final int LEVEL_LOW = 3;

    protected
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_password_entry);

        initItemView();

        if (savedInstanceState != null && savedInstanceState.containsKey(NEW_PASSWORD_INSTANCE_KEY)) {
            mActivityMode = savedInstanceState.getInt(NEW_PASSWORD_INSTANCE_KEY, DEFAULT_NEW_INSTANCE_ID);
        }

        Intent intent = getIntent();
        if (intent != null) {

            if (intent.hasExtra(UPDATE_PASSWORD_INSTANCE_KEY)) {

                if (mActivityMode == DEFAULT_NEW_INSTANCE_ID) {
                    // populate the UI
                    mActivityMode = intent.getIntExtra(UPDATE_PASSWORD_INSTANCE_KEY, DEFAULT_NEW_INSTANCE_ID);

                    mUri = intent.getData();
                    if (mUri == null)
                        throw new NullPointerException("URI for DetailActivity cannot be null");

                    // Initialize the loader for DetailActivity
                    getSupportLoaderManager().initLoader(LOADER_ADD_ACTIVITY_ID, null, this);
                }
            }
        }
    }

    @Override
    public
    boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_menu, menu);
        return true;
    }

    @Override
    public
    boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_save:
                onSaveClicked();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected
    void onSaveInstanceState(Bundle outState) {
        outState.putInt(NEW_PASSWORD_INSTANCE_KEY, mActivityMode);
        super.onSaveInstanceState(outState);
    }

    private
    void initItemView() {

        mAccountInfo = (EditText) findViewById(R.id.et_value_website);
        mUsername = (EditText) findViewById(R.id.et_value_username);
        mEmailOnProfile = (EditText) findViewById(R.id.et_value_email);
        mPassword = (EditText) findViewById(R.id.et_value_password);
        mDescription = (EditText) findViewById(R.id.et_value_description);
        mTimeStamp = (TextView) findViewById(R.id.tv_timestamp);

        // Initialize to lowest mSecurityLevel by default (securityLevel = 1)
        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        setSecurityLevelInViews(LEVEL_LOW);

        Date date = new Date();
        mTimeStamp.setText(toDateString(date));

        // Programmatically enable the EditText to sentence first letter capitalization
        mAccountInfo.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        mUsername.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        mDescription.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    }


    /**
     * populateUI would be called to populate the UI when in update mode
     */
    private
    void populateViews(Cursor data) {

        String accountInfo = data.getString(data.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_ACCOUNTINFO));
        String userName = data.getString(data.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_USERNAME));
        String emailAdd = data.getString(data.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_EMAIL));
        String password = data.getString(data.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_PASSWORD));
        String description = data.getString(data.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_DESCRIPTION));

        int securityLevel = data.getInt(data.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_SECURITY));

        Long dateLong = data.getLong(data.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_TIMESTAMP));
        Date date = toDate(dateLong);

        mAccountInfo.setText(accountInfo);
        mUsername.setText(userName);

        if (emailAdd != null)
            mEmailOnProfile.setText(emailAdd);
        //else
        //    mEmailOnProfile.setText("none");

        if (password != null)
            mPassword.setText(password);
        //else
        //    mPassword.setText("none");

        if (description != null)
            mDescription.setText(description);
        //else
        //    mDescription.setText("none");

        setSecurityLevelInViews(securityLevel);
        mTimeStamp.setText(toDateString(date));
    }


    // Insert new task data via a ContentResolver
    public
    void onSaveClicked() {

        // Create new empty ContentValues object
        ContentValues contentValues = new ContentValues();

        String accountInfo = mAccountInfo.getText().toString();
        if (accountInfo.length() == 0) {
            accountInfo = "unspecified";
        }
        contentValues.put(PasswordContract.PasswordEntry.COLUMN_ACCOUNTINFO, accountInfo);

        String username = mUsername.getText().toString();
        if (username.length() == 0) {
            username = "none";
        }
        contentValues.put(PasswordContract.PasswordEntry.COLUMN_USERNAME, username);

        String email = mEmailOnProfile.getText().toString();
        if ((email.length() != 0) && !(email.equals("none"))) {
            contentValues.put(PasswordContract.PasswordEntry.COLUMN_EMAIL, email);
        }

        String password = mPassword.getText().toString();
        if ((password.length() != 0) && !(password.equals("none"))) {
            contentValues.put(PasswordContract.PasswordEntry.COLUMN_PASSWORD, password);
        }

        String description = mDescription.getText().toString();
        if ((description.length() != 0) && !(description.equals("none"))) {
            contentValues.put(PasswordContract.PasswordEntry.COLUMN_DESCRIPTION, description);
        }

        // Put the task description and selected mPriority into the ContentValues
        int securityLevel = getSecurityLevelFromViews();
        contentValues.put(PasswordContract.PasswordEntry.COLUMN_SECURITY, securityLevel);

        // Put the task description and selected mPriority into the ContentValues
        Long timeStamp = toTimestamp(new Date());
        contentValues.put(PasswordContract.PasswordEntry.COLUMN_TIMESTAMP, timeStamp);

        if (mActivityMode == DEFAULT_NEW_INSTANCE_ID) {
            // Insert the content values via a ContentResolver
            Uri uri = getContentResolver().insert(PasswordContract.PasswordEntry.CONTENT_URI, contentValues);
        } else {
            int id = getContentResolver().update(mUri, contentValues, null, null);
        }

        finish();
    }

    public
    int getSecurityLevelFromViews() {

        int level = LEVEL_LOW;

        int checkedId = mRadioGroup.getCheckedRadioButtonId();
        switch (checkedId) {
            case R.id.radButton1:
                level = LEVEL_HIGH;
                break;
            case R.id.radButton2:
                level = LEVEL_MEDIUM;
                break;
            case R.id.radButton3:
                level = LEVEL_LOW;
        }
        return level;
    }


    public
    void setSecurityLevelInViews(int level) {
        switch (level) {
            case LEVEL_HIGH:
                mRadioGroup.check(R.id.radButton1);
                break;
            case LEVEL_MEDIUM:
                mRadioGroup.check(R.id.radButton2);
                break;
            case LEVEL_LOW:
                mRadioGroup.check(R.id.radButton3);
        }
    }


    @Override
    public
    Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {

        switch (loaderId) {

            case LOADER_ADD_ACTIVITY_ID:

                CursorLoader cursorLoader = new CursorLoader(this,
                        mUri,
                        null,
                        null,
                        null,
                        null);

                return cursorLoader;

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public
    void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            /* We have valid data, continue on to bind the data to the UI */
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {
            /* No data to display, simply return and do nothing */
            return;
        }

        populateViews(data);
    }

    @Override
    public
    void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
