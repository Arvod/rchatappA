package com.retarcorp.rchatapp.Net;

import android.os.AsyncTask;

import com.retarcorp.rchatapp.Model.Member;
import com.retarcorp.rchatapp.Model.Site;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by CaptainOsmant on 11.01.2018.
 */

public class GrabMessagesTask extends AsyncTask<Member, Void, String> {

    Site site;
    Member member;
    MessagesGrabCallback callback;
    public GrabMessagesTask(MessagesGrabCallback callback, Site site){
        this.site = site;
        this.member = member;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(Member ...params) {
        this.member = params[0];

        try {
            URL url = new URL(site.api.getMessagesURL(member));
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String s;
            while((s = reader.readLine())!=null){
                builder.append(s);
            }
            connection.disconnect();
            return builder.toString();

        }catch(Exception e){
            e.printStackTrace();
            return "{\"status\":\"ERROR\",\"message\":\"Ошибка сети!\"}";

        }
    }

    @Override
    public void onPostExecute(String result){
        this.callback.onMessagesGrabbed(result);
    }
}
