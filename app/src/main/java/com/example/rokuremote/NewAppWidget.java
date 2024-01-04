package com.example.rokuremote;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class NewAppWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static PendingIntent getPendingSelfIntent(Context context, String command) {
        Intent intent = new Intent(context, NewAppWidget.class);
        intent.putExtra("cmd: ", command);
        new HttpAsyncTask().doInBackground(" " + "/keypress/" + command, "");
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
            views.setOnClickPendingIntent(R.id.pushButtonOn, getPendingSelfIntent(context, "ON"));
            views.setOnClickPendingIntent(R.id.pushButtonOff, getPendingSelfIntent(context, "OFF"));
            views.setOnClickPendingIntent(R.id.pushButtonPlay, getPendingSelfIntent(context, "PLAY"));
            views.setOnClickPendingIntent(R.id.pushButtonHome, getPendingSelfIntent(context, "HOME"));
            views.setOnClickPendingIntent(R.id.pushButtonBack, getPendingSelfIntent(context, "BACK"));
            views.setOnClickPendingIntent(R.id.pushButtonUp, getPendingSelfIntent(context, "UP"));
            views.setOnClickPendingIntent(R.id.pushButtonDown, getPendingSelfIntent(context, "DOWN"));
            views.setOnClickPendingIntent(R.id.pushButtonLeft, getPendingSelfIntent(context, "LEFT"));
            views.setOnClickPendingIntent(R.id.pushButtonRight, getPendingSelfIntent(context, "RIGHT"));
            views.setOnClickPendingIntent(R.id.pushButtonOk, getPendingSelfIntent(context, "Select"));
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}