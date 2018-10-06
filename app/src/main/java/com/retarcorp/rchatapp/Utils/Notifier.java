package com.retarcorp.rchatapp.Utils;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;

import com.retarcorp.rchatapp.Global;
import com.retarcorp.rchatapp.R;
import com.retarcorp.rchatapp.Services.SendMessageService;

/**
 * Created by CaptainOsmant on 13.01.2018.
 */

public class Notifier {

    public static void notify(String title, String text, Intent intent) {

        PendingIntent pi = PendingIntent.getActivity(Global.Ctx.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager mgr = (NotificationManager) Global.Ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification.Builder builder = new Notification.Builder(Global.Ctx);
        builder.setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(pi);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            RemoteInput remoteInput = new RemoteInput.Builder("EXTRA_TEXT_REPLY")
                    .setLabel("Введите сообщение")
                    .build();

            Intent intentService = new Intent(Global.Ctx, SendMessageService.class);
            intentService.setAction("ACTION_REPLY");
            intentService.putExtra("EXTRA_ITEM_ID", text);
            intentService.putExtra("ID", intent.getIntExtra("member_id", 0));

            // PendingIntent
            PendingIntent replyPendingIntent =
                    PendingIntent.getService(Global.Ctx,
                            0, intentService, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Action replyAction = new Notification.Action.Builder(
                    android.R.drawable.ic_menu_save, "Ответить", replyPendingIntent)
                    .addRemoteInput(remoteInput)
                    .build();

            builder.addAction(replyAction);
        }

        Notification lastNotification = builder.build();

        lastNotification.vibrate = new long[]{600, 450, 300};
        mgr.notify(1, lastNotification);
    }

}