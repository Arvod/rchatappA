package com.retarcorp.rchatapp.Services;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.RemoteInput;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import com.retarcorp.rchatapp.Global;
import com.retarcorp.rchatapp.Net.MessageSentCallback;
import com.retarcorp.rchatapp.Net.SendMessageTask;

public class SendMessageService extends Service implements MessageSentCallback {

    public void onCreate() {
        super.onCreate();
        if (Global.Ctx == null) {
            Global.Ctx = this;
        }
    }

    public void onDestroy() {
        super.onDestroy();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ("ACTION_REPLY".equals(intent.getAction())) {

            CharSequence replyText = null;
            Bundle results = RemoteInput.getResultsFromIntent(intent);
            if (results != null) {
                replyText = results.getCharSequence("EXTRA_TEXT_REPLY");
            }
            (new SendMessageTask(Global.CurrentSite, Global.CurrentMember, this)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(replyText));
        }
        return START_NOT_STICKY;
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onMessageSent(String json) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(1);
    }
}
