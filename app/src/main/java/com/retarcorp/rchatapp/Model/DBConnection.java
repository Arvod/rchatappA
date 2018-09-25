package com.retarcorp.rchatapp.Model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by CaptainOsmant on 10.01.2018.
 */

public class DBConnection  extends SQLiteOpenHelper{

    public DBConnection(Context ctx){
        super(ctx, "rchat", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE sites (" +
                "_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT " +
                ",protocol VARCHAR(100)" +
                ",domain VARCHAR(256)" +
                ",token VARCHAR(256) DEFAULT '' " +
                ",dialogs INT DEFAULT 0" +
                ",last_connection DATETIME" +
                ",icon TEXT" +
                ",mid INT DEFAULT 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE sites;");

        this.onCreate(db);
    }
}
