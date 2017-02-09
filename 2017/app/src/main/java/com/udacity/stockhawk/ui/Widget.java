package com.udacity.stockhawk.ui;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.DbHelper;
import com.udacity.stockhawk.data.PrefUtils;

import java.util.Random;
import java.util.Set;

import static com.udacity.stockhawk.R.id.textView;

public class Widget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int count = appWidgetIds.length;

        for (int i = 0; i < count; i++) {

            int widgetId = appWidgetIds[i];
            String number = String.format("%03d", (new Random().nextInt(900) + 100));

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);

           remoteViews.removeAllViews( R.id.widget_scroll_layout);

            Set<String> stockPref = PrefUtils.getStocks(context);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            DbHelper dbhelper = new DbHelper(context);
            Cursor cursor = dbhelper.getReadableDatabase().query("quotes",

                    new String[] { Contract.Quote._ID, Contract.Quote.COLUMN_SYMBOL, Contract.Quote.COLUMN_PRICE, Contract.Quote.COLUMN_PERCENTAGE_CHANGE},

                    null, null, null, null, null);


            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {

                String symbols = cursor.getString(1);
                float price= cursor.getFloat(2);
                float change= cursor.getFloat(3);

                RemoteViews textView = new RemoteViews(context.getPackageName(), R.layout.list_item_quote);
                // textView.setTextViewText(symbol, stockArray[x]);
                textView.setTextViewText(R.id.symbol, symbols);
                textView.setTextViewText(R.id.price, Float.toString(price));
                textView.setTextViewText(R.id.change, Float.toString(change));
                if(change<0){
                    textView.setInt(R.id.change, "setBackgroundColor",
                        android.graphics.Color.RED);
                }
                remoteViews.addView(R.id.widget_scroll_layout, textView);

                cursor.moveToNext();
            }

            cursor.close();

 remoteViews.setTextViewText(textView, number);

            Intent intent = new Intent(context, Widget.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.actionButton, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);

        }
    }
}