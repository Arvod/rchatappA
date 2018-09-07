package com.retarcorp.rchatapp.Services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.retarcorp.rchatapp.Global;
import com.retarcorp.rchatapp.Model.Site;
import com.retarcorp.rchatapp.Net.MessageTouchTask;
import com.retarcorp.rchatapp.SiteMembersActivity;
import com.retarcorp.rchatapp.Utils.Notifier;

import java.util.List;

/**
 * Created by CaptainOsmant on 13.01.2018.
 */

public class RefreshService extends Service implements MessageReceiver, SiteProducer{

    @Override
    public void onCreate(){
        super.onCreate();
        this.task = new MessageTouchTask(this, this);
        this.task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 2000);
        if(Global.Ctx == null){
            Global.Ctx = this;
        }

    }
    private MessageTouchTask task;

    @Override
    public List<Site> touch(){
        return Site.getSites();
    }


    @Override
    public void onMessagesRefreshed(MessageTick[] messages) {
        String site = null;
        int siteId = 0;
        String text = null;

        //Toast.makeText(Global.Ctx,"!",Toast.LENGTH_SHORT).show();

        for(MessageTick tick: messages){
            if(tick == null){
                continue;
            }
            if(Global.CurrentSite!=null) {
                if (Global.CurrentSite.getId() == tick.site.getId()) {
                    if (Global.CurrentMember == null) {
                        continue;
                    }

                    if (Global.CurrentMember.getId() == tick.uid) {
                        continue;
                    }
                }
            }
            if(tick.site.mid < tick.mid){
                tick.site.mid = tick.mid;
                tick.site.update();
                site = tick.site.getTitle();
                text = tick.text;
                siteId = tick.site.getId();
            }
        }

        if(siteId > 0){
            Intent intent = new Intent();
            intent.setClass(this, SiteMembersActivity.class);

            intent.putExtra("site_id", siteId);
            Notifier.notify("Новое сообщение с сайта "+site,text,intent);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
