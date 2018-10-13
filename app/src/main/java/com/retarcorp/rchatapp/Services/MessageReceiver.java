package com.retarcorp.rchatapp.Services;

public interface MessageReceiver {
    void onMessagesRefreshed(MessageTick[] messages);
}
