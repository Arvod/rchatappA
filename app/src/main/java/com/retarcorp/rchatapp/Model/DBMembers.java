package com.retarcorp.rchatapp.Model;

import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBMembers extends SQLiteOpenHelper {

    public DBMembers(Context ctx){
        super(ctx, "rchatm", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE members (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT" +
                ",ssid VARCHAR(256)" +
                ",last_ip VARCHAR(256)" +
                ",last_city VARCHAR(256) DEFAULT '' " +
                ",name VARCHAR(100) DEFAULT ''" +
                ",pagehref VARCHAR(256) DEFAULT ''" +
                ",unread INTEGER" +
                ",last_message VARCHAR(256) DEFAULT ''" +
                ",messages INEGER DEFAULT 1" +
                ",lastonline VARCHAR(256) DEFAULT '')");
        db.execSQL("CREATE TABLE messages (" +
                "uid INTEGER NOT NULL" +
                ",direction INTEGER" +
                ",created VARCHAR(256) DEFAULT ''" +
                ",text VARCHAR(256) DEFAULT '')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE members");
        db.execSQL("DROP TABLE messages");
        this.onCreate(db);
    }

    public boolean isEmpty(String TableName) {
        SQLiteDatabase database = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(database, TableName) == 0;
    }
}
