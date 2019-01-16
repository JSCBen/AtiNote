package com.tju.twist.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Twist on 2015/9/19.
 */
public class SummaryDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "mydata1.db";
    private static final int version = 1;

    public SummaryDatabase(Context context){
        super(context, DB_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists summary(" +
                "sum text default \"\"," +
                "get text default \"\"," +
                "special text default \"\"," +
                "time text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
