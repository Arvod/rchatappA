package com.retarcorp.rchatapp.Net;

import android.os.AsyncTask;

import com.retarcorp.rchatapp.Model.Member;
import com.retarcorp.rchatapp.Model.Site;


public class GrabMessagesTask extends AsyncTask<Member, Void, String> {

    private MessagesGrabCallback callback;
    public GrabMessagesTask(MessagesGrabCallback callback, Site site){
        Site site1 = site;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(Member ...params) {
        try {
            return "";
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onPostExecute(String result){
        this.callback.onMessagesGrabbed(result);
    }
}
