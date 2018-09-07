package com.retarcorp.rchatapp.Net;

import android.os.AsyncTask;

import com.retarcorp.rchatapp.Model.Site;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by CaptainOsmant on 10.01.2018.
 */

public class ConnectivityTask extends AsyncTask<String, Void, Boolean> {

    private ConnectivityCallback callback;
    private String protocol;
    private String domain;
    private String key;

    public ConnectivityTask(ConnectivityCallback callback){
        this.callback = callback;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        this.protocol = params[0];
        this.domain = params[1];
        this.key = params[2];

        String url = Site.getConnectivityCheckerPath(protocol, domain, key);

        try {
            URL Url = new URL(url);
            HttpURLConnection connection = (HttpURLConnection)Url.openConnection();
            connection.setInstanceFollowRedirects(true);
            connection.connect();

            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String res = reader.readLine();

            System.out.println(res);
            if(res.trim().equals("OK")){
                return true;
            }

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }


        return false;
    }

    @Override
    protected void onPostExecute(Boolean result){
        callback.onConnectivityChecked(protocol, domain, key, result);
    }
}
