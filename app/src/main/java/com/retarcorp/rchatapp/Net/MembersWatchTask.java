package com.retarcorp.rchatapp.Net;

import android.os.AsyncTask;

import com.retarcorp.rchatapp.Model.Site;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by CaptainOsmant on 12.01.2018.
 */

public class MembersWatchTask extends AsyncTask<Site, String, String> {
    private final int delay;
    private Site site;
    MemberGrabCallback callback;
    public MembersWatchTask( MemberGrabCallback callback, int delay){

        this.delay = delay;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(Site... params) {
        this.site = params[0];
        do {
            try {
                URL url = new URL(site.api.getMembersURL());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuffer buffer = new StringBuffer();
                String s;
                while ((s = reader.readLine()) != null) {
                    buffer.append(s);
                }
                connection.disconnect();
                publishProgress(buffer.toString());
            } catch (Exception e) {
                publishProgress("{\"status\":\"ERROR\",\"message\":\"Ошибка на уровне Android-приложения\"}");
            }

            try{
                Thread.sleep(this.delay);
            }catch (Exception e){
                return ("{\"status\":\"ERROR\",\"message\":\"Ошибка на уровне Android-приложения\"}");
            }

        }while(true);
    }

    @Override
    public void onProgressUpdate(String ...result){
        this.callback.onMembersJSONGrabbed(result[0]);

    }

    @Override
    public void onPostExecute(String result){
        this.callback.onMembersJSONGrabbed(result);
    }
}
