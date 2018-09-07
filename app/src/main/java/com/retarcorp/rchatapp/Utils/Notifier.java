package com.retarcorp.rchatapp.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.retarcorp.rchatapp.Global;
import com.retarcorp.rchatapp.R;

/**
 * Created by CaptainOsmant on 13.01.2018.
 */

public class Notifier {

    public static void notify(String title, String text, Intent intent){

        NotificationManager mgr = (NotificationManager) Global.Ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(Global.Ctx);
        builder.setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true);

        PendingIntent pi = PendingIntent.getActivity(Global.Ctx.getApplicationContext(), 0, intent, 0);
        builder.setContentIntent(pi);
        Notification lastNotification = builder.build();
        lastNotification.vibrate = new long[]{600,450,300};
        try {
            //lastNotification.color = Global.Ctx.getResources().getColor(R.color.colorPrimaryDark);
        }catch (Exception e){
            e.printStackTrace();
        }
        mgr.notify(1, lastNotification);
    }

}