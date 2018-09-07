package com.retarcorp.rchatapp.Net;

import android.os.AsyncTask;

import com.retarcorp.rchatapp.Model.Site;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by CaptainOsmant on 11.01.2018.
 */

public class GrabMembersTask extends AsyncTask<Site, Void, String> {

    private Site site;
    MemberGrabCallback callback;
    public GrabMembersTask( MemberGrabCallback callback){

        this.callback = callback;
    }

    @Override
    protected String doInBackground(Site... params) {
        this.site = params[0];
        try {
            URL url = new URL(site.api.getMembersURL());
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer buffer = new StringBuffer();
            String s;
            while((s = reader.readLine())!=null){
                buffer.append(s);
            }
            connection.disconnect();
            return buffer.toString();
        }catch(Exception e){
            return "{\"status\":\"ERROR\",\"message\":\"Ошибка на уровне Android-приложения\"}";
        }
    }

    @Override
    public void onPostExecute(String result){
        this.callback.onMembersJSONGrabbed(result);
    }
}
