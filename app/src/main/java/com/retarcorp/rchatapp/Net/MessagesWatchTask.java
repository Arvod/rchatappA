package com.retarcorp.rchatapp.Net;

import android.os.AsyncTask;

import com.retarcorp.rchatapp.Model.Member;
import com.retarcorp.rchatapp.Model.Site;
/**
 * Created by CaptainOsmant on 12.01.2018.
 */

public class MessagesWatchTask extends AsyncTask<Member, String, String> {

    Site site;
    int delay;
    MessagesGrabCallback callback;

    public MessagesWatchTask(MessagesGrabCallback callback, Site site, int delay){
        this.site = site;
        this.callback = callback;
        this.delay = delay;
    }

    @Override
    protected String doInBackground(Member ...params) {
//        Member member = params[0];
        do {
            try {
//                URL url = new URL(site.api.getMessagesURL(member));
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.connect();
//
//                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                StringBuilder builder = new StringBuilder();
//                String s;
//                while ((s = reader.readLine()) != null) {
//                    builder.append(s);
//                }
//                connection.disconnect();
//                reader.close();
//                publishProgress(builder.toString());
                publishProgress("");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try{
                Thread.sleep(this.delay);
            }catch (Exception e){

                return ("{\"status\":\"ERROR\",\"message\":\"Ошибка обновления сообщений!\"}");
            }
        }while(true);
    }

    @Override
    public void onProgressUpdate(String ...params){
        this.callback.onMessagesGrabbed(params[0]);
    }

    @Override
    public void onPostExecute(String result){
        this.callback.onMessagesGrabbed(result);
    }
}
