package com.retarcorp.rchatapp.Services;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.retarcorp.rchatapp.R;

import java.util.Random;

/**
 * Created by CaptainOsmant on 23.01.2018.
 */

public class BasicWidgetProvider extends AppWidgetProvider {

    @Override
    public void onDeleted(Context context, int[] widgetIds){
        super.onDeleted(context, widgetIds);
    }

    @Override
    public void onDisabled(Context ctx){
        super.onDisabled(ctx);
    }

    @Override
    public void onEnabled(Context context){
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] widgetIds){
       super.onUpdate(context, manager, widgetIds);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        Random rnd = new Random();
        views.setTextViewText(R.id.widget_text, rnd.nextInt(100000000)+99999999+"");
        Intent intent = new Intent(context,BasicWidgetProvider.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction("OPEN");
        for(int id: widgetIds) {
            Intent refreshIntent = new Intent(context, getClass());
            refreshIntent.setAction("REFRESH");
            views.setOnClickPendingIntent(R.id.widget_layout
                    , PendingIntent.getBroadcast(context, 0, refreshIntent, 0));
            views.setOnClickFillInIntent(R.id.widget_layout,intent);
            manager.updateAppWidget(id,views);
        }
    }

    @Override
    public void onReceive(Context ctx, Intent intent){
        super.onReceive(ctx, intent);
        if("REFRESH".equals(intent.getAction())) {
            AppWidgetManager manager = AppWidgetManager.getInstance(ctx);
            RemoteViews views = new RemoteViews(ctx.getPackageName(),R.layout.widget_layout);
            ComponentName widget = new ComponentName(ctx, BasicWidgetProvider.class);
            Random rnd = new Random();
            String pos = rnd.nextInt(100000000)+99999999+"";
            views.setTextViewText(R.id.widget_text,pos);
            manager.updateAppWidget(widget, views);

        }
    }

}
