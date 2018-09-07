package com.retarcorp.rchatapp.Net;

import android.os.AsyncTask;

import com.retarcorp.rchatapp.Model.Member;
import com.retarcorp.rchatapp.Model.Site;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by CaptainOsmant on 13.01.2018.
 */

public class MemberInfoReceiveTask extends AsyncTask<Member, Void, String> {

    private final MemberReceiveCallback callback;
    private final Site site;

    public MemberInfoReceiveTask(Site site, MemberReceiveCallback callback){
        this.callback = callback;
        this.site = site;
    }

    @Override
    protected String doInBackground(Member[] params) {
        Member m = params[0];
        try{
            URL url = new URL(site.api.getMemberInfoURL(m));
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String s;
            while((s = reader.readLine())!=null){
                builder.append(s);
            }
            return builder.toString();


        }catch (Exception e){
            return "{\"status\":\"ERROR\",\"message\":\"Ошибка запроса данных о пользователе\"}";
        }
    }

    @Override
    protected void onPostExecute(String result){
        callback.onMemberInfoReceived(result);

    }



}
