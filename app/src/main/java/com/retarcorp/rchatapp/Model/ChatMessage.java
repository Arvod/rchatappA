package com.retarcorp.rchatapp.Model;

import java.util.Date;

public class ChatMessage {

    public String text;
    public Date created;
    public Direction direction;

    ChatMessage(String text, Date created, ChatMessage.Direction direction) {
        this.text = text;
        this.created = created;
        this.direction = direction;
    }

    public enum Direction {
        MEMBER, ADMIN
    }

    public String getText() {
        return text;
    }

    public Date getCreated() {
        return created;
    }
}