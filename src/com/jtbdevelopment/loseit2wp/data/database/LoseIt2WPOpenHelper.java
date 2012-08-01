package com.jtbdevelopment.loseit2wp.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by IntelliJ IDEA.
 * JTB Development
 * Date: 7/21/12
 * Time: 7:43 PM
 */
public class LoseIt2WPOpenHelper extends SQLiteOpenHelper {

    public LoseIt2WPOpenHelper(final Context context) {
        super(context, LoseIt2WPDataSource.DATABASE_NAME, null, LoseIt2WPDataSource.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(LoseIt2WPDataSource.DATABASE_MESSAGE_TABLE.createDatabaseTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        throw new UnsupportedOperationException("No known upgrades at this time");
    }
}
