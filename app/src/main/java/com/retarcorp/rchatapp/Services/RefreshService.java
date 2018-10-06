package com.retarcorp.rchatapp.Services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.retarcorp.rchatapp.ChatActivity;
import com.retarcorp.rchatapp.Global;
import com.retarcorp.rchatapp.Model.Member;
import com.retarcorp.rchatapp.Model.Site;
import com.retarcorp.rchatapp.Net.RefreshTask;
import com.retarcorp.rchatapp.Utils.Notifier;

import java.util.List;
/**
 * Created by CaptainOsmant on 13.01.2018.
 */

public class RefreshService extends Service implements MessageReceiver, SiteProducer {

    @Override
    public void onCreate(){
        super.onCreate();
        this.task = new RefreshTask(this, this);
        this.task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 1500);

        if(Global.Ctx == null){
            Global.Ctx = this;
        }

    }

    private RefreshTask task;
    private Site currentSite = null;

    @Override
    public List<Site> touch(){
        return Site.getSites();
    }


    @Override
    public void onMessagesRefreshed(MessageTick[] messages) {
        String site = null;
        int siteId = 0;
        String text = null;
        int userId = 0;
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
                userId = tick.uid;
            }
        }
        if(siteId > 0){
            if(Global.Ctx == null){
                Global.Ctx = this;
            }
            Intent intent = new Intent();
            currentSite = new Site(siteId);
            Global.CurrentSite = currentSite;
            intent.setClass(this, ChatActivity.class);
            intent.putExtra("member_id", userId);
            Global.CurrentMember = new Member(userId);
            Notifier.notify("Новое сообщение с сайта " + site, text, intent);
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
