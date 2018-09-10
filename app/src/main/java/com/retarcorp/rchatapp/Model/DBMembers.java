package com.retarcorp.rchatapp.Model;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBMembers extends SQLiteOpenHelper {

    public DBMembers(Context ctx){
        super(ctx, "rchatm", null, 1);
    }

    public DBMembers(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE members (" +
                "_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT " +
                ",ssid VARCHAR(256)" +
                ",last_ip VARCHAR(256)" +
                ",last_city VARCHAR(256) DEFAULT '' " +
                ",name VARCHAR(100) DEFAULT ''" +
                ",pagehref VARCHAR(256) DEFAULT ''" +
                ",unread INTEGER" +
                ",last_message VARCHAR(256) DEFAULT ''" +
                ",messages INEGER DEFAULT 1)") ;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE members;");
        this.onCreate(db);
    }
}
