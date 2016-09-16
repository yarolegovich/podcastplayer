package com.devchallenge.podcastplayer.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.devchallenge.podcastplayer.MainActivity;
import com.devchallenge.podcastplayer.R;
import com.devchallenge.podcastplayer.audio.Player;
import com.devchallenge.podcastplayer.audio.PlayerState;
import com.devchallenge.podcastplayer.data.model.Podcast;
import com.devchallenge.podcastplayer.view.RemoteControllerView;

/**
 * Created by MrDeveloper on 14.09.2016.
 */
public class WidgetManager extends AppWidgetProvider {

    public static void triggerUpdate(Context context) {
        Intent intent = new Intent(context, WidgetManager.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        ComponentName component = new ComponentName(context, WidgetManager.class);
        int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(component);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews views;
        PlayerState playerState = Player.getInstance().getState();
        if (playerState.isInitialized() && playerState.getCurrentPodcast() != null) {
            Podcast currentPodcast = playerState.getCurrentPodcast();
            views = RemoteControllerView.create(context);
            AppWidgetTarget target = new AppWidgetTarget(context, views,
                    R.id.rc_podcast_image,
                    appWidgetIds);
            Glide.with(context).load(currentPodcast.getImageUrl()).asBitmap().into(target);
        } else {
            views = new RemoteViews(context.getPackageName(), R.layout.widget_no_playback);
            views.setOnClickPendingIntent(
                    R.id.container,
                    RemoteControllerView.openMainActivityAction(context));
        }
        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }
}
