package com.aero.umich.passwordsaver.database;

import android.net.Uri;
import android.provider.BaseColumns;

public class PasswordContract {

/*
     Add content provider constants to the Contract that
     Clients need to know how to access the task data,
     and it's your job to provide these content URI's for the path to that data:
        1) Content authority,
        2) Base content URI,
        3) Path(s) to the tasks directory
        4) Content URI for data in the TaskEntry class
*/

    public static final String  AUTHORITY = "com.aero.umich.passwordsaver";
    public static final Uri     BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String  PATH_PASSWORD = "passwords";

    public static final class PasswordEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_PASSWORD)
                .build();

        public static final String TABLE_NAME = "passwords";

        // all columns that the table requires, the _id column is automatically generated
        public static final String COLUMN_ACCOUNTINFO = "accountinfo";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_SECURITY = "securitylevel";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_TIMESTAMP = "time_stamp";
    }
}
