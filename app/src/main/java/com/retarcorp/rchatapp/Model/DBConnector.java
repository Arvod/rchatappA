package com.retarcorp.rchatapp.Model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.retarcorp.rchatapp.Global;
import com.retarcorp.rchatapp.Net.MessagesRefreshThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DBConnector {

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public DBConnector() {
        dbHelper = new DBHelper(Global.Ctx);
        db = dbHelper.getWritableDatabase();
    }

    public ArrayList<Integer> updateMembers(String replyFromServer) throws JSONException {
        ArrayList<Integer> messagesList = new ArrayList<>();
        JSONObject jsonMembers = new JSONObject(replyFromServer);
        String status = jsonMembers.getString("status");
        if (status.equals("OK")) {
            JSONArray array = jsonMembers.getJSONArray("data");
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Cursor query = db.query("members", null, "id" + " = ?", new String[]{String.valueOf(obj.getInt("id"))}, null, null, null);
                query.moveToFirst();
                if (query.getCount() == 0) {
                    db.insert("members", null, getMembersValues(obj));
                    messagesList.add(obj.getInt("id"));
                    new MessagesRefreshThread(obj.getInt("id")).run();
                } else if (!query.getString(7).equals(obj.getString("last_message"))) {
                    db.update("members", getMembersValues(obj), "id" + " = ?", new String[]{String.valueOf(obj.getInt("id"))});
                    messagesList.add(obj.getInt("id"));
                    new MessagesRefreshThread(obj.getInt("id")).run();
                }
                query.close();
            }
        }
        return messagesList;
    }

    private ContentValues getMembersValues(JSONObject obj) {
        ContentValues cv = new ContentValues();
        try {
            cv.put("id", obj.getInt("id"));
            cv.put("ssid", obj.getString("ssid"));
            cv.put("last_city", obj.getString("last_city"));
            cv.put("last_ip", obj.getString("last_ip"));
            cv.put("last_city", obj.getString("last_city"));
            cv.put("name", obj.getString("name"));
            cv.put("pagehref", obj.getString("pagehref"));
            cv.put("unread", obj.getInt("unread"));
            cv.put("last_message", obj.getString("last_message"));
            cv.put("messages", obj.getInt("messages"));
            cv.put("lastonline", obj.getString("lastonline"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cv;
    }

    public void updateMessages(String replyFromServer, int id) throws JSONException {
        JSONObject jsonMessages = new JSONObject(replyFromServer);
        String status = jsonMessages.getString("status");
        if (status.trim().equals("OK")) {
            JSONArray array = jsonMessages.getJSONArray("data");
            db.delete("messages", "uid=?", new String[]{"" + id});
            addMessagesToDB(array);
        }
    }

    private void addMessagesToDB(JSONArray array) {
        ContentValues cv = new ContentValues();
        try {
            for (int i = array.length() - 1; i >= 0; i--) {
                JSONObject obj = array.getJSONObject(i);
                cv.put("uid", obj.getInt("uid"));
                cv.put("direction", obj.getInt("direction"));
                cv.put("created", obj.getString("created"));
                cv.put("text", obj.getString("text"));
                db.insert("messages", null, cv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        db.close();
        dbHelper.close();
    }

    public void setMessagesRead(int id) {
        ContentValues cv = new ContentValues();
        cv.put("unread", 0);
        db.update("members", cv, "id" + " = ?", new String[]{String.valueOf(id)});
    }

    public ArrayList<ChatMessage> getMessages(int id) {
        ArrayList<ChatMessage> messages = new ArrayList<>();
        Cursor mCursor = db.query("messages", null, "uid" + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        mCursor.moveToFirst();
        if (mCursor.getCount() != 0) {
            for (int i = 0; i < mCursor.getCount(); i++) {
                ChatMessage.Direction direction = mCursor.getInt(1) == 1 ? ChatMessage.Direction.MEMBER : ChatMessage.Direction.ADMIN;
                Date created = new Date();
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
                try {
                    created = format.parse(mCursor.getString(2));
                } catch (ParseException e) {
                    e.printStackTrace();
                    created = null;
                }
                ChatMessage m = new ChatMessage(mCursor.getString(3), created, direction);
                messages.add(m);
                mCursor.moveToNext();
            }
        }
        mCursor.close();
        return messages;
    }

    public ArrayList<Member> getMembers() {
        ArrayList<Member> members = new ArrayList<>();
        if (!dbHelper.isEmpty("members")) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("select * from " + "members" + " ORDER BY " + "lastonline" + " DESC", null);
            cursor.moveToFirst();
            members = new ArrayList<>();
            for (int i = cursor.getCount() - 1; i >= 0; i--) {
                Member m = new Member(cursor.getInt(0));
                m.last_city = cursor.getString(3);
                m.last_ip = cursor.getString(4);
                m.last_message = cursor.getString(7);
                m.pagehref = cursor.getString(5);
                m.ssid = cursor.getString(1);
                m.messages = cursor.getInt(8);
                m.unread = cursor.getInt(6);
                members.add(m);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return members;
    }

    public Member getMember(int id) {
        Cursor mCursor = db.query("members", new String[]{"pagehref ", "last_city", "last_ip", "lastonline"}, "id" + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        mCursor.moveToFirst();
        Member member = new Member(id);
        member.pagehref = mCursor.getString(0);
        member.last_city = mCursor.getString(1);
        member.last_ip = mCursor.getString(2);
        member.last_message = mCursor.getString(3);
        mCursor.close();
        return member;
    }

}
