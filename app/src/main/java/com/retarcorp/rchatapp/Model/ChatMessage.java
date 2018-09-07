package com.retarcorp.rchatapp.Model;

import java.util.Date;

/**
 * Created by CaptainOsmant on 11.01.2018.
 */

public class ChatMessage {

    public String text;
    public Date created;
    public Direction direction;

    public ChatMessage(String text, Date created, ChatMessage.Direction direction) {
        this.text = text;
        this.created = created;
        this.direction = direction;
    }

    public enum Direction {
        MEMBER, ADMIN
    }

}