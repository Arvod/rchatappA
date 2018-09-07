package com.retarcorp.rchatapp.Model;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.retarcorp.rchatapp.Global;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by CaptainOsmant on 10.01.2018.
 */

public class Site extends Object {


    public int mid;
    public Site.API api = new Site.API();

    public int getId() {
        return id;
    }




    public class Protocol{
        public static final String HTTP = "http";
        public static final String HTTPS = "https";
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

    public static class SiteNotFoundException extends Exception{
        SiteNotFoundException(String s){
            super(s);
        }
    }

    public static class SiteAlreadyExistsException extends Exception{
        SiteAlreadyExistsException(String s){
            super(s);
        }
    }

    public Site(int id) throws SiteNotFoundException{
        DBConnection db = new DBConnection(Global.Ctx);
        Cursor c = db.getReadableDatabase().query("sites",null,"_id=?",new String[]{id+""},null,null,null,null);
        if(c.getCount() == 0 ){
            throw new SiteNotFoundException("No site with given ID found!");
        }
        c.moveToFirst();
        this.id = c.getInt(0);
        this.protocol = c.getString(1);
        this.domain = c.getString(2);
        this.token = c.getString(3);
        this.dialogs = c.getInt(4);

        String dateStr = c.getString(5);
        if(dateStr == null){
            dateStr = "";
        }
        this.last_connection = new Date();
        this.icon = c.getString(6);
        this.mid = c.getInt(7);
        c.close();
    }

    public void update() {
        DBConnection db = new DBConnection(Global.Ctx);
        ContentValues cv = new ContentValues();
        cv.put("token", this.token);
        cv.put("mid",this.mid);
        cv.put("icon",this.icon);
        cv.put("dialogs", this.dialogs);
        cv.put("icon", this.icon);
        db.getWritableDatabase().update("sites",cv,"_id=?",new String[]{String.valueOf(this.id)});
    }

    public static boolean isSiteAlreadyExists(String protocol, String domain){
        DBConnection db = new DBConnection(Global.Ctx);
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

        DBConnection db = new DBConnection(Global.Ctx);
        ContentValues cv = new ContentValues();
        cv.put("protocol", protocol);
        cv.put("domain", domain);
        cv.put("token", key);
        cv.put("dialogs", 0);

        int id = (int)db.getWritableDatabase().insert("sites","",cv);
        try {
            return new Site(id);
        }catch (Exception e){
            Log.e(null, "Unable to add site!");
        }
        return null;
    }

    public static String getConnectivityCheckerPath(String protocol, String domain, String key){
        return protocol+"://"+domain+"/Core/apps/RChat/app/Connectivity/?key="+key;
    }

    public static ArrayList<Site> getSites() {
        DBConnection db = new DBConnection(Global.Ctx);
        Cursor c = db.getReadableDatabase().query("sites",null,null,null,null,null,null);
        ArrayList<Site> sites = new ArrayList<>();
        c.moveToFirst();
        if(c.getCount()>0){
            do{
                try {
                    sites.add(new Site(c.getInt(0)));
                }catch (SiteNotFoundException e){
                    continue;
                }
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
