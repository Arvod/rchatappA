package com.retarcorp.rchatapp.Net;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.retarcorp.rchatapp.Global;
import com.retarcorp.rchatapp.Model.DBMembers;
import com.retarcorp.rchatapp.Model.Member;
import com.retarcorp.rchatapp.Model.Site;
import com.retarcorp.rchatapp.Services.MessageReceiver;
import com.retarcorp.rchatapp.Services.MessageTick;
import com.retarcorp.rchatapp.Services.SiteProducer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by CaptainOsmant on 13.01.2018.
 */

public class MessageTouchTask extends AsyncTask<Integer, String, String> {


    private final SiteProducer producer;
    private final MessageReceiver receiver;
    private DBMembers dbHelper;
    private SQLiteDatabase db;
    private Site siteDB;

    public MessageTouchTask(SiteProducer producer, MessageReceiver receiver){
        this.producer = producer;
        this.receiver = receiver;
    }

    private List<Site> sites;
    @Override
    protected String doInBackground(Integer... params) {
        try {
            do{
                siteDB = producer.touch().get(0);
                Thread.sleep(params[0]);
                sites = producer.touch();
                String[] messages = new String[params.length];
                for (int i = 0; i < params.length; i++) {
                    Site site = sites.get(i);
                    String href = site.api.getMessagesTouchURL();
                    try {
                        URL url = new URL(href);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String s;
                        StringBuilder builder = new StringBuilder();
                        while ((s = reader.readLine()) != null) {
                            builder.append(s);
                        }
                        reader.close();
                        messages[i]=builder.toString();
                    } catch (Exception e) {
                        messages[i] = "{\"status\":\"ERROR\",\"message\":\""+e.getMessage()+"\"}";
                        e.printStackTrace();
                        continue;
                    }
                }
                try {
                    dbHelper = new DBMembers(Global.Ctx);
                    db = dbHelper.getWritableDatabase();
                    JSONObject jsonMembers = new JSONObject(getMembersJSON());
                    String status = jsonMembers.getString("status");
                    if (status.equals("OK")) {
                        JSONArray array = jsonMembers.getJSONArray("data");
                        int len = array.length();
                        if (dbHelper.isEmpty("members")) {
                            addMembersToDB(len, array);
                        }
                        if (dbHelper.isEmpty("messages")) {
                            Cursor mCursor = db.query("members", new String[]{"id"}, null, null, null, null, null);
                            mCursor.moveToFirst();
                            for (int i = 0; i < mCursor.getCount(); i++) {
                                addMessagesOfId(mCursor.getInt(0));
                                mCursor.moveToNext();
                            }
                        } else {
                            for (int i = 0; i < len; i++) {
                                JSONObject obj = array.getJSONObject(i);
                                Cursor mCursor = db.query("members", null, "id" + " = ?", new String[]{String.valueOf(obj.getInt("id"))}, null, null, null);
                                mCursor.moveToFirst();
                                if (mCursor.getCount() == 0) {
                                    db.insert("members", null, getMembersValues(obj));
                                    addMessagesOfId(obj.getInt("id"));
                                } else if (!mCursor.getString(7).equals(obj.getString("last_message")) || mCursor.getInt(6) != (obj.getInt("unread"))) {
                                    db.update("members", getMembersValues(obj), "id" + " = ?", new String[]{String.valueOf(obj.getInt("id"))});
                                    addMessagesOfId(obj.getInt("id"));
                                }
                                mCursor.close();
                            }
                        }
                    }
                    dbHelper.close();
                    db.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                publishProgress(messages);
            }while(true);
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public void onProgressUpdate(String ...messages){
        MessageTick[] ticks = new MessageTick[messages.length];
        for(int i = 0; i<messages.length; i++){
            try{
                JSONObject obj = new JSONObject(messages[i]);
                String status = obj.getString("status");
                if(status.trim().equals("OK")){
                    ticks[i] = MessageTick.fromJSON(obj.getJSONObject("data"));
                    ticks[i].site = this.sites.get(i);
                }else{
                    throw new Exception(status+":"+obj.getString("message"));
                }
            }catch (Exception e){
                ticks[i] = null;
                e.printStackTrace();
            }
        }
        this.receiver.onMessagesRefreshed(ticks);
    }

    private void addMessagesOfId(int id) {
        try {
            JSONObject jsonObj = new JSONObject(getMessagesJSON(id));
            String status = jsonObj.getString("status");
            if (status.trim().equals("OK")) {
                JSONArray array = jsonObj.getJSONArray("data");
                int len = array.length();
                Cursor mCursor = db.query("messages", null, "uid" + " = ?", new String[]{String.valueOf(id)}, null, null, null);
                db.delete("messages", "uid=?", new String[]{"" + id});
                addMessagesToDB(len, array);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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


    private void addMembersToDB(int len, JSONArray array) {
        ContentValues cv = new ContentValues();
        try {
            for (int i = 0; i < len; i++) {
                JSONObject obj = array.getJSONObject(i);
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
                db.insert("members", null, cv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addMessagesToDB(int len, JSONArray array) {
        ContentValues cv = new ContentValues();
        try {
            for (int i = len - 1; i >= 0; i--) {
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

    private String getMembersJSON() {
        try {
            URL url = new URL(siteDB.api.getMembersURL());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer buffer = new StringBuffer();
            String s;
            while ((s = reader.readLine()) != null) {
                buffer.append(s);
            }
            connection.disconnect();
            reader.close();
            return buffer.toString();
        } catch (Exception e) {
            return "{\"status\":\"ERROR\",\"message\":\"Ошибка запроса\"}";
        }
    }

    private String getMessagesJSON(int id) {
        String href = siteDB.api.getMessagesURL(new Member(id));
        try {
            URL url = new URL(href);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String s;
            StringBuilder builder = new StringBuilder();
            while ((s = reader.readLine()) != null) {
                builder.append(s);
            }
            connection.disconnect();
            reader.close();
            return builder.toString();
        } catch (Exception e) {
            return "{\"status\":\"ERROR\",\"message\":\"Ошибка запроса\"}";
        }
    }
}
