package com.devchallenge.podcastplayer.view;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.devchallenge.podcastplayer.MainActivity;
import com.devchallenge.podcastplayer.R;
import com.devchallenge.podcastplayer.audio.BackgroundAudioService;
import com.devchallenge.podcastplayer.audio.Player;
import com.devchallenge.podcastplayer.audio.PlayerState;
import com.devchallenge.podcastplayer.data.model.Podcast;

import static com.devchallenge.podcastplayer.audio.BackgroundAudioService.*;

/**
 * Created by MrDeveloper on 14.09.2016.
 */
public class RemoteControllerView {

    private static final int PI_MAIN_ACTIVITY = 7587;

    @SuppressWarnings("ConstantConditions")
    public static RemoteViews create(Context c) {
        PlayerState state = Player.getInstance().getState();
        Podcast currentPodcast = state.getCurrentPodcast();
        RemoteViews views = new RemoteViews(
                c.getPackageName(),
                R.layout.view_remote_controller);
        views.setTextViewText(R.id.notif_podcast_title, currentPodcast.getTitle());
        views.setImageViewUri(R.id.rc_podcast_image, Uri.parse(currentPodcast.getImageUrl()));
        int icon = state.isPaused() ? R.drawable.ic_play_arrow_black_24dp : R.drawable.ic_pause_black_24dp;
        views.setImageViewResource(R.id.rc_btn_start_pause, icon);
        String playPauseAction = state.isPaused() ? ACTION_START : ACTION_PAUSE;
        views.setOnClickPendingIntent(R.id.rc_btn_stop, createAction(c, 4442, ACTION_STOP));
        views.setOnClickPendingIntent(R.id.rc_btn_start_pause, createAction(c, 4443, playPauseAction));
        views.setOnClickPendingIntent(R.id.rc_btn_prev, createAction(c, 4444, ACTION_PREV));
        views.setOnClickPendingIntent(R.id.rc_btn_next, createAction(c, 4445, ACTION_NEXT));
        views.setOnClickPendingIntent(R.id.container, openMainActivityAction(c));
        return views;
    }

    private static PendingIntent createAction(Context c, int piId, String action) {
        Intent intent = new Intent(c, BackgroundAudioService.class);
        intent.setAction(action);
        return PendingIntent.getService(c, piId, intent, 0);
    }

    public static PendingIntent openMainActivityAction(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context, PI_MAIN_ACTIVITY, intent, 0);
    }
}
