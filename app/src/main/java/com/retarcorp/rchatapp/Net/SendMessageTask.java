package com.retarcorp.rchatapp.Net;

import android.os.AsyncTask;

import com.retarcorp.rchatapp.Global;
import com.retarcorp.rchatapp.Model.Member;
import com.retarcorp.rchatapp.Model.Site;
import com.retarcorp.rchatapp.R;
import com.retarcorp.rchatapp.Utils.Response;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SendMessageTask extends AsyncTask<String, Void, String> {

    private Site site;
    private Member member;
    private MessageSentCallback callback;

    public SendMessageTask(Site site, Member member, MessageSentCallback callback){
        this.site = site;
        this.member = member;
        this.callback = callback;

    }

    @Override
    protected String doInBackground(String... params) {
        String message = params[0];
        try {
            URL url = new URL(site.api.getMessageSendingURL(this.member));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(),"UTF-8"));
            writer.write(message);
            writer.flush();
            writer.close();
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String s ;
            while((s = reader.readLine())!= null){
                builder.append(s);
            }
            connection.disconnect();
            return builder.toString();

        }catch (Exception e){
            return Response.getExceptionJSON(Global.Ctx.getResources().getString(R.string.send_error));
        }
    }

    @Override
    public void onPostExecute(String result){
        this.callback.onMessageSent(result);
    }
}
