package com.retarcorp.rchatapp.Net;

import com.retarcorp.rchatapp.Model.DBConnector;
import com.retarcorp.rchatapp.Model.Member;
import com.retarcorp.rchatapp.Model.Site;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MessagesRefreshThread implements Runnable {

    private int id;

    public MessagesRefreshThread(int id) {
        this.id = id;
    }

    @Override
    public void run() {
        DBConnector dbConnector = new DBConnector();
        String href = Site.getSites().get(0).api.getMessagesURL(new Member(id));
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
            dbConnector.updateMessages(builder.toString(), this.id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
