package com.retarcorp.rchatapp.Model;

import java.util.Date;

/**
 * Created by CaptainOsmant on 11.01.2018.
 */

public class Member {

    public String name;
    public String last_ip = "";
    public String last_city = "";
    public Date last_online = null;
    public String ssid = "";
    private int id;
    public int messages = 0;
    public String last_message = "";
    public String pagehref = "";
    public int unread = 0;

    public Member(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getLast_message() {
        return last_message;
    }
}
