package com.retarcorp.rchatapp.Net;

import android.os.AsyncTask;
import android.widget.Toast;

import com.retarcorp.rchatapp.Global;
import com.retarcorp.rchatapp.Model.Site;
import com.retarcorp.rchatapp.Services.MessageReceiver;
import com.retarcorp.rchatapp.Services.MessageTick;
import com.retarcorp.rchatapp.Services.SiteProducer;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by CaptainOsmant on 13.01.2018.
 */

public class MessageTouchTask extends AsyncTask<Integer, String, String> {


    private final SiteProducer producer;
    private final MessageReceiver receiver;

    public MessageTouchTask(SiteProducer producer, MessageReceiver receiver){
        this.producer = producer;
        this.receiver = receiver;
    }

    private List<Site> sites;
    @Override
    protected String doInBackground(Integer... params) {
        try {
            do{
                Thread.sleep(params[0]);
                sites = producer.touch();
                String[] messages = new String[params.length];
                for (int i = 0; i < params.length; i++) {
                    Site site = sites.get(i);
                    String href = site.api.getMessagesTouchURL();
                    try {
                        URL url = new URL(href);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String s;
                        StringBuilder builder = new StringBuilder();
                        while ((s = reader.readLine()) != null) {
                            builder.append(s);
                        }
                        messages[i]=builder.toString();


                    } catch (Exception e) {
                        messages[i] = "{\"status\":\"ERROR\",\"message\":\""+e.getMessage()+"\"}";
                        e.printStackTrace();
                        continue;
                    }

                }
                publishProgress(messages);
            }while(true);
        }catch (Exception e){
            return null;
        }
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

    @Override
    public void onPostExecute(String result){
        //Toast.makeText(Global.Ctx,"!!",Toast.LENGTH_SHORT).show();
    }
}
