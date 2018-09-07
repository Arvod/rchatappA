package com.retarcorp.rchatapp.Services;

/**
 * Created by CaptainOsmant on 13.01.2018.
 */

public interface MessageReceiver {
    void onMessagesRefreshed(MessageTick[] messages);
}
