package com.retarcorp.rchatapp.Net;

import android.os.AsyncTask;

import com.retarcorp.rchatapp.Model.Site;

/**
 * Created by CaptainOsmant on 12.01.2018.
 */

public class MembersWatchTask extends AsyncTask<Site, String, String> {
    private final int delay;
    MemberGrabCallback callback;

    public MembersWatchTask( MemberGrabCallback callback, int delay){
        this.delay = delay;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(Site... params) {
        do {
            try {
                publishProgress("");
                Thread.sleep(this.delay);
            } catch (Exception e) {
                e.printStackTrace();
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
