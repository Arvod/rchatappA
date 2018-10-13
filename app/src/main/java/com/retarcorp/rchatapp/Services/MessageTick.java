package com.retarcorp.rchatapp.Services;

import com.retarcorp.rchatapp.Model.Site;

import org.json.JSONObject;

public class MessageTick {
    public int uid;
    public int mid;
    public String text;
    public Site site;

    public MessageTick(int uid, int mid, String text){
        this.uid = uid;
        this.mid = mid;
        this.text = text;
        this.site = null;
    }

    public static MessageTick fromJSON(JSONObject json){
        try {
            return new MessageTick(json.getInt("uid"), json.getInt("mid"),json.getString("text"));
        }catch (Exception e){
            return  null;
        }
    }
}

