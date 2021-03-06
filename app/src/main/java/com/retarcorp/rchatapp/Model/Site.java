package com.retarcorp.rchatapp.Model;

import android.content.ContentValues;
import android.database.Cursor;

import com.retarcorp.rchatapp.Global;

import java.util.ArrayList;
import java.util.Date;


public class Site extends Object {


    public int mid;
    public Site.API api = new Site.API();

    public int getId() {
        return id;
    }

    public String protocol;
    public String domain;
    public String token;
    public int dialogs;
    public Date last_connection;
    public String icon;
    private int id;

    public String getTitle() {
        return protocol.toLowerCase()+"://"+domain;
    }

    public static class SiteAlreadyExistsException extends Exception{
        SiteAlreadyExistsException(String s){
            super(s);
        }
    }

    public Site() {
    }

    public Site(int id) {
        DBHelper db = new DBHelper(Global.Ctx);
        Cursor c = db.getReadableDatabase().query("sites",null,"_id=?",new String[]{id+""},null,null,null,null);
        c.moveToFirst();
        this.id = c.getInt(0);
        this.protocol = c.getString(1);
        this.domain = c.getString(2);
        this.token = c.getString(3);
        this.dialogs = c.getInt(4);
        this.last_connection = new Date();
        this.icon = c.getString(6);
        this.mid = c.getInt(7);
        c.close();
    }

    public void update() {
        DBHelper db = new DBHelper(Global.Ctx);
        ContentValues cv = new ContentValues();
        cv.put("token", this.token);
        cv.put("mid",this.mid);
        cv.put("icon",this.icon);
        cv.put("dialogs", this.dialogs);
        cv.put("icon", this.icon);
        db.getWritableDatabase().update("sites",cv,"_id=?",new String[]{String.valueOf(this.id)});
    }

    public static boolean isSiteAlreadyExists(String protocol, String domain){
        DBHelper db = new DBHelper(Global.Ctx);
        Cursor c = db.getReadableDatabase().query(
                "sites"
                ,null
                ,"protocol=? AND domain=?"
                ,new String[]{protocol, domain}
                ,null
                , null
                , null);
        if(c.getCount()>0){
            c.close();
            return true;
        }
        c.close();
        return false;
    }

    public static Site createSite(String protocol, String domain, String key) throws SiteAlreadyExistsException{

        if(isSiteAlreadyExists(protocol, domain)){
            throw new SiteAlreadyExistsException("Site already exists!");
        }

        DBHelper db = new DBHelper(Global.Ctx);
        ContentValues cv = new ContentValues();
        cv.put("protocol", protocol);
        cv.put("domain", domain);
        cv.put("token", key);
        cv.put("dialogs", 0);

        int id = (int)db.getWritableDatabase().insert("sites","",cv);
        try {
            return new Site(id);
        } catch (Exception e) {
        }
        return null;
    }

    public static String getConnectivityCheckerPath(String protocol, String domain, String key){
        return protocol+"://"+domain+"/Core/apps/RChat/app/Connectivity/?key="+key;
    }

    public static ArrayList<Site> getSites() {
        DBHelper db = new DBHelper(Global.Ctx);
        Cursor c = db.getReadableDatabase().query("sites",null,null,null,null,null,null);
        ArrayList<Site> sites = new ArrayList<>();
        c.moveToFirst();
        if(c.getCount()>0){
            do{
                sites.add(new Site(c.getInt(0)));
            }while(c.moveToNext());
        }
        c.close();
        return sites;
    }

    public class API{
        public API(){

        }

        public String getMembersURL(){
            return Site.this.protocol+"://"+Site.this.domain+"/Core/apps/RChat/app/Members/?key="+Site.this.token;
        }

        public String getMessagesURL(Member member) {
            return Site.this.protocol+"://"+Site.this.domain+"/Core/apps/RChat/app/Messages/?key="+Site.this.token+"&member="+member.getId();
        }

        public String getMessageSendingURL(Member member) {
            return Site.this.protocol+"://"+Site.this.domain+"/Core/apps/RChat/app/Messages/?key="+Site.this.token+"&member="+member.getId();
        }

        public String getMemberInfoURL(Member member) {
            return Site.this.protocol+"://"+Site.this.domain+"/Core/apps/RChat/app/Members/?key="+Site.this.token+"&member="+member.getId();
        }

        public String getMessagesTouchURL() {
            return Site.this.protocol+"://"+Site.this.domain+"/Core/apps/RChat/app/Messages/Touch/?key="+Site.this.token;
        }

        public String getIconURL() {
            return Site.this.protocol+"://"+Site.this.domain+"/favicon.ico";
        }
    }
}
