/*
* Copyright (C) 2016 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.aero.umich.passwordsaver.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.aero.umich.passwordsaver.database.PasswordContract.PasswordEntry;
import com.aero.umich.passwordsaver.utils_xls.PasswordData;

public class PasswordDbHelper extends SQLiteOpenHelper {

    // The name of the database
    private static final String DATABASE_NAME = "pwd.db";

    // If you change the database schema, you must increment the database version
    private static final int DATABASE_VERSION = 6;

    // Constructor
    PasswordDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the tasks database is created for the first time.
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create tasks table (careful to follow SQL formatting rules)
        final String CREATE_TABLE = "CREATE TABLE "  + PasswordEntry.TABLE_NAME + " ("      +
                PasswordEntry._ID                + " INTEGER PRIMARY KEY AUTOINCREMENT, "   +
                PasswordEntry.COLUMN_ACCOUNTINFO + " TEXT NOT NULL, "   +
                PasswordEntry.COLUMN_USERNAME    + " TEXT NOT NULL, "   +
                PasswordEntry.COLUMN_EMAIL       + " TEXT, "            +
                PasswordEntry.COLUMN_PASSWORD    + " TEXT, "            +
                PasswordEntry.COLUMN_SECURITY    + " INTEGER NOT NULL, "+
                PasswordEntry.COLUMN_DESCRIPTION + " TEXT, "            +
                PasswordEntry.COLUMN_TIMESTAMP   + " TEXT NOT NULL, "     +
                " UNIQUE (" + PasswordEntry.COLUMN_ACCOUNTINFO + ", " + PasswordEntry.COLUMN_USERNAME + ") ON CONFLICT REPLACE );";

        db.execSQL(CREATE_TABLE);
    }


    /**
     * This method discards the old table of data and calls onCreate to recreate a new one.
     * This only occurs when the version number for this database (DATABASE_VERSION) is incremented.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PasswordEntry.TABLE_NAME);
        onCreate(db);
    }
}
