package com.retarcorp.rchatapp.Net;

import android.os.AsyncTask;

import com.retarcorp.rchatapp.Global;
import com.retarcorp.rchatapp.Model.Member;
import com.retarcorp.rchatapp.Model.Site;
import com.retarcorp.rchatapp.R;
import com.retarcorp.rchatapp.Utils.Response;

public class MessagesWatchTask extends AsyncTask<Member, String, String> {

    private Site site;
    private int delay;
    private MessagesGrabCallback callback;

    public MessagesWatchTask(MessagesGrabCallback callback, Site site, int delay){
        this.site = site;
        this.callback = callback;
        this.delay = delay;
    }

    @Override
    protected String doInBackground(Member ...params) {
        do {
            try {
                publishProgress("");
                Thread.sleep(this.delay);
            } catch (Exception e) {
                e.printStackTrace();
                return Response.getExceptionJSON(Global.Ctx.getResources().getString(R.string.update_error));
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
