package com.devchallenge.podcastplayer.audio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;
import com.devchallenge.podcastplayer.R;
import com.devchallenge.podcastplayer.data.model.Podcast;
import com.devchallenge.podcastplayer.util.Utils;
import com.devchallenge.podcastplayer.view.RemoteControllerView;
import com.devchallenge.podcastplayer.widget.WidgetManager;

import rx.Subscription;

/**
 * Created by MrDeveloper on 13.09.2016.
 */
public class BackgroundAudioService extends Service {

    private static final String LOG_TAG = BackgroundAudioService.class.getSimpleName();

    private static final int NOTIF_ID = 4222;

    private static final String PREFIX = BackgroundAudioService.class.getCanonicalName();
    public static final String ACTION_START = PREFIX + ".start";
    public static final String ACTION_PAUSE = PREFIX + ".pause";
    public static final String ACTION_STOP = PREFIX + ".close";
    public static final String ACTION_NEXT = PREFIX + ".next";
    public static final String ACTION_PREV = PREFIX + ".prev";
    public static final String ACTION_STOP_IF_IDLE = PREFIX + ".stop_if_idle";

    public static final String EXTRA_PODCAST = PREFIX + ".extra_source";

    private Player player = Player.getInstance();

    private Subscription playerStateChanges;
    private Subscription playbackProgressChanges;

    private NotificationManager nm;
    private RemoteViews notificationView;
    private Notification notification;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player = Player.getInstance();
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        playerStateChanges = player.onPlayerUpdates().subscribe(this::updateRemoteControllers);
        playbackProgressChanges = player.onPlaybackProgress().subscribe(this::updateProgress);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand: " + intent);
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(ACTION_START)) {
                startPlayback((Podcast) intent.getSerializableExtra(EXTRA_PODCAST));
            } else if (action.equals(ACTION_PAUSE)) {
                pausePlayback();
            } else if (action.equals(ACTION_STOP)) {
                stopPlayback();
            } else if (action.equals(ACTION_NEXT)) {
                next();
            } else if (action.equals(ACTION_PREV)) {
                previous();
            } else if (action.equals(ACTION_STOP_IF_IDLE)) {
                stopIfIdle();
            }
        }
        return START_STICKY_COMPATIBILITY;
    }

    public void stopIfIdle() {
        if (player.getState().isPaused()) {
            player.stop();
            stopSelf();
        }
    }

    public void startOrPauseIfPlaying(Podcast podcast) {
        Podcast current = player.getState().getCurrentPodcast();
        if (podcast.equals(current)) {
            if (player.getState().isPaused()) {
                resume();
            } else {
                pausePlayback();
            }
        } else {
            startPlayback(podcast);
        }
    }

    public void stopPlayback() {
        player.stop();
        closeNotification();
        stopSelf();
    }

    public void pausePlayback() {
        player.pause();
    }

    public void resume() {
        startPlayback(null);
    }

    public void startPlayback(Podcast podcast) {
        player.start(podcast);
    }

    public void next() {
        player.next();
    }

    public void previous() {
        player.previous();
    }

    public void seekTo(int ms) {
        player.seekTo(ms);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        playerStateChanges.unsubscribe();
        playbackProgressChanges.unsubscribe();
        closeNotification();
    }

    private void closeNotification() {
        stopForeground(true);
        nm.cancel(NOTIF_ID);
    }

    private void updateRemoteControllers(PlayerState playerState) {
        WidgetManager.triggerUpdate(this);
        if (player.getState().getCurrentPodcast() != null) {
            startForeground(NOTIF_ID, createNotificationController(playerState));
        } else {
            stopSelf();
        }
    }

    private void updateProgress(PlaybackState playbackState) {
        if (notificationView != null) {
            notificationView.setTextViewText(R.id.notif_podcast_time, Utils.toTime(playbackState));
            nm.notify(NOTIF_ID, notification);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private Notification createNotificationController(PlayerState state) {
        Podcast currentPodcast = state.getCurrentPodcast();
        notificationView = RemoteControllerView.create(this);
        notification =  new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_album_white_24dp)
                .setTicker(currentPodcast.getTitle())
                .setContentTitle(currentPodcast.getTitle())
                .setContentText(currentPodcast.getAuthors())
                .setCustomBigContentView(notificationView)
                .setAutoCancel(false)
                .build();
        NotificationTarget target = new NotificationTarget(this, notificationView,
                R.id.rc_podcast_image,
                notification, NOTIF_ID);
        Glide.with(this).load(currentPodcast.getImageUrl()).asBitmap().into(target);
        return notification;
    }

    public class LocalBinder extends Binder {
        public BackgroundAudioService getService() {
            return BackgroundAudioService.this;
        }
    }
}
