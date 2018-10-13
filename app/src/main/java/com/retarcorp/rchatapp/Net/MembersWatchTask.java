package com.retarcorp.rchatapp.Net;

import android.os.AsyncTask;

import com.retarcorp.rchatapp.Global;
import com.retarcorp.rchatapp.Model.Site;
import com.retarcorp.rchatapp.R;
import com.retarcorp.rchatapp.Utils.Response;

public class MembersWatchTask extends AsyncTask<Site, String, String> {
    private final int delay;
    private MemberGrabCallback callback;

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
                return Response.getExceptionJSON(Global.Ctx.getResources().getString(R.string.error_in_system));
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
