package com.retarcorp.rchatapp.Net;

import android.os.AsyncTask;

import com.retarcorp.rchatapp.Model.DBConnector;
import com.retarcorp.rchatapp.Model.Site;
import com.retarcorp.rchatapp.Services.MessageReceiver;
import com.retarcorp.rchatapp.Services.MessageTick;
import com.retarcorp.rchatapp.Services.SiteProducer;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by CaptainOsmant on 13.01.2018.
 */

public class RefreshTask extends AsyncTask<Integer, String, String> {

    private Site site;
    private final SiteProducer producer;
    private final MessageReceiver receiver;

    public RefreshTask(SiteProducer producer, MessageReceiver receiver) {
        this.producer = producer;
        this.receiver = receiver;
        site = new Site();
        site.protocol = "https";
        site.domain = "retarcorp.by";
        site.token = "123";
    }

    private List<Site> sites;
    @Override
    protected String doInBackground(Integer... params) {
        do {
            try {
                sites = producer.touch();
                String[] messages = new String[params.length];
                for (int i = 0; i < params.length; i++) {
                    Site site = sites.get(i);
                    String href = site.api.getMessagesTouchURL();
                    try {
                        URL url = new URL(href);
                        HttpURLConnection httpURLConnectionion = (HttpURLConnection) url.openConnection();
                        BufferedReader bufferedReaderer = new BufferedReader(new InputStreamReader(httpURLConnectionion.getInputStream()));
                        StringBuilder builder = new StringBuilder();
                        String s;
                        while ((s = bufferedReaderer.readLine()) != null) {
                            builder.append(s);
                        }
                        bufferedReaderer.close();
                        messages[i] = builder.toString();
                    } catch (Exception e) {
                        messages[i] = "{\"status\":\"ERROR\",\"message\":\"  \"}";
                        e.printStackTrace();
                        continue;
                    }
                }
                Thread.sleep(300);
                DBConnector dbConnector = new DBConnector();
                dbConnector.updateMembers(GrabMember());
                publishProgress(messages);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while (true);
    }

    @Override
    public void onProgressUpdate(String ...messages){
        MessageTick[] ticks = new MessageTick[messages.length];
        for(int i = 0; i<messages.length; i++){
            try{
                JSONObject obj = new JSONObject(messages[i]);
                String status = obj.getString("status");
                if(status.trim().equals("OK")){
                    ticks[i] = MessageTick.fromJSON(obj.getJSONObject("data"));
                    ticks[i].site = this.sites.get(i);
                }else{
                    throw new Exception(status+":"+obj.getString("message"));
                }
            }catch (Exception e){
                ticks[i] = null;
                e.printStackTrace();
            }
        }
        this.receiver.onMessagesRefreshed(ticks);
    }

    private String GrabMember() throws IOException {
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
        reader.close();
        return buffer.toString();
    }
}
