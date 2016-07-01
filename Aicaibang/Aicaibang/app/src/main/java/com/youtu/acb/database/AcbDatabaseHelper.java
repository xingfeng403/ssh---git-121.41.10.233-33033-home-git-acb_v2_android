package com.youtu.acb.database;

import android.content.Context;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

/**
 * Created by xingf on 16/5/31.
 */
public class AcbDatabaseHelper extends SQLiteOpenHelper{
    public static final String CREATE_TABLE = "create table Ads(id INTEGER, link TEXT, start_time TEXT," +
            "end_time TEXT, img TEXT)";

    public AcbDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {

    }
}
