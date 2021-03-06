package com.tju.twist.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Twist on 2015/9/11.
 */
public class EventDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "mydata.db";
    private static final int version = 1;

    public EventDatabase(Context context){
        super(context, DB_NAME, null, version);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists event(" +
                    "name text default \"\"," +
                    "content text default \"\"," +
                    "time char(20)," +
                    "color integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}











