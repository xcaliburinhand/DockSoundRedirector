package net.muteheadlight.docksoundredir.widget;

import net.muteheadlight.dockredir.R;
import net.muteheadlight.docksoundredir.dockRedirCentral;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class RedirWidget extends AppWidgetProvider {

	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        dockRedirCentral.logD("Widget Updated");
        
	 	Intent i = new Intent();
        i.setAction("net.muteheadlight.docksoundredir.intent.action.WIDGET_REDIRECT");
        PendingIntent x = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget); 
        view.setOnClickPendingIntent(R.id.widgetButton,x);
        appWidgetManager.updateAppWidget(new ComponentName(context,RedirWidget.class), view);
	}
}