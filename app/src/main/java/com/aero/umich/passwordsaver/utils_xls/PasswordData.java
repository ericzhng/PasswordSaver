package com.aero.umich.passwordsaver.utils_xls;

import android.content.ContentValues;

import com.aero.umich.passwordsaver.database.PasswordContract;

public class PasswordData {

    public String AccountInfo, Username, Email, Password, Description;
    public int securityLevel;
    public long timeStamp;

    public
    PasswordData(String accountInfo, String username, String email, String password, String description, int securityLevel, long timeStamp) {
        this.AccountInfo = accountInfo;
        this.Username = username;
        this.Email = email;
        this.Password = password;
        this.Description = description;
        this.securityLevel = securityLevel;
        this.timeStamp = timeStamp;
    }

    public static
    String[] getColumnNames() {
        String[] names = new String[]{
                "AccountInfo",
                "Username",
                "Email",
                "Password",
                "securityLevel",
                "Description",
                "TimeStamp"
        };
        return names;
    }

    public ContentValues convertContentValues() {

        // Insert new task data via a ContentResolver
        // Create new empty ContentValues object
        ContentValues contentValues = new ContentValues();

        // Put the task description and selected mPriority into the ContentValues
        contentValues.put(PasswordContract.PasswordEntry.COLUMN_ACCOUNTINFO, AccountInfo);
        contentValues.put(PasswordContract.PasswordEntry.COLUMN_USERNAME, Username);
        contentValues.put(PasswordContract.PasswordEntry.COLUMN_EMAIL, Email);
        contentValues.put(PasswordContract.PasswordEntry.COLUMN_PASSWORD, Password);
        contentValues.put(PasswordContract.PasswordEntry.COLUMN_SECURITY, securityLevel);
        contentValues.put(PasswordContract.PasswordEntry.COLUMN_DESCRIPTION, Description);
        contentValues.put(PasswordContract.PasswordEntry.COLUMN_TIMESTAMP, timeStamp);

        return contentValues;
    }


    public void convertClassData(ContentValues contentValues) {

        // Put the task description and selected mPriority into the ContentValues
        this.AccountInfo = String.valueOf(contentValues.get(PasswordContract.PasswordEntry.COLUMN_ACCOUNTINFO));
        this.Username = String.valueOf(contentValues.get(PasswordContract.PasswordEntry.COLUMN_USERNAME));
        this.Email = String.valueOf(contentValues.get(PasswordContract.PasswordEntry.COLUMN_EMAIL));
        this.Password = String.valueOf(contentValues.get(PasswordContract.PasswordEntry.COLUMN_PASSWORD));
        this.securityLevel = (int) contentValues.get(PasswordContract.PasswordEntry.COLUMN_SECURITY);
        this.Description = String.valueOf(contentValues.get(PasswordContract.PasswordEntry.COLUMN_DESCRIPTION));

        this.timeStamp = (long) contentValues.get(PasswordContract.PasswordEntry.COLUMN_TIMESTAMP);
    }
}
