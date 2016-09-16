package com.devchallenge.podcastplayer.audio;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.devchallenge.podcastplayer.App;
import com.devchallenge.podcastplayer.data.Cache;
import com.devchallenge.podcastplayer.data.Podcasts;
import com.devchallenge.podcastplayer.data.model.Podcast;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by MrDeveloper on 13.09.2016.
 */
public class Player {

    private static final String LOG_TAG = Player.class.getSimpleName();

    private static final long PROGRESS_UPDATE_DELAY = TimeUnit.SECONDS.toMillis(1);

    private static Player instance;

    public static Player getInstance() {
        if (instance == null) {
            instance = new Player(App.getInstance());
        }
        return instance;
    }

    private Context context;

    private PlayerStateImpl currentState;

    private PlayerCallbacks callbacks;
    private MediaPlayer player;

    private Podcasts podcasts;
    private Cache cache;

    private Handler updateProgressScheduler;
    private Runnable updateProgressTask;

    private PublishSubject<PlayerState> playerStateSubject;
    private PublishSubject<PlaybackState> playbackStateSubject;

    public Player(Context context) {
        this.context = context.getApplicationContext();
        this.podcasts = Podcasts.getInstance();
        if (podcasts == null) {
            throw new IllegalStateException("Podcasts are not initialized");
        }
        this.callbacks = new PlayerCallbacks();
        this.currentState = new PlayerStateImpl();
        this.cache = new Cache(context);
        this.updateProgressScheduler = new Handler(Looper.getMainLooper());
        this.updateProgressTask = new PlaybackProgressUpdateTask();
        this.playerStateSubject = PublishSubject.create();
        this.playbackStateSubject = PublishSubject.create();
    }

    void stop() {
        releasePlayer();
        pushUpdate();
    }

    void pause() {
        currentState.setPaused(true);
        if (canPlay() && player.isPlaying()) {
            player.pause();
        }
        pushUpdate();
    }

    void start(Podcast podcast) {
        currentState.setPaused(false);
        boolean isNewPodcast = podcast != null && !podcast.equals(currentState.getCurrentPodcast());
        if (isNewPodcast) {
            initMediaPlayer(podcast);
            currentState.setPodcast(podcast);
        } else if (canPlay() && !player.isPlaying()) {
            player.start();
        }
        pushUpdate();
    }

    private boolean canPlay() {
        return player != null && currentState.isInitialized();
    }

    void next() {
        Podcast podcast = currentState.getCurrentPodcast();
        if (podcast != null) {
            start(podcasts.next(podcast));
        }
    }

    void previous() {
        Podcast podcast = currentState.getCurrentPodcast();
        if (podcast != null) {
            start(podcasts.previous(podcast));
        }
    }

    void seekTo(int ms) {
        if (currentState.isInitialized()) {
            player.seekTo(ms);
        }
        pushUpdate();
    }

    public PlayerState getState() {
        return currentState;
    }

    private void initMediaPlayer(Podcast podcast) {
        releasePlayer();
        player = new MediaPlayer();
        try {
            if (cache.isPodcastAudioCached(podcast)) {
                player.setDataSource(context, cache.getCachedAudioUri(podcast));
            } else {
                player.setDataSource(podcast.getAudio().getUrl());
            }
            player.setOnPreparedListener(callbacks);
            player.setOnCompletionListener(callbacks);
            player.setOnErrorListener(callbacks);
            currentState.setPodcast(podcast);
            currentState.setPaused(false);
            player.prepareAsync();
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        pushUpdate();
    }


    private void releasePlayer() {
        currentState.clear();
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();
            player = null;
        }
        updateProgressScheduler.removeCallbacksAndMessages(null);
    }

    public Observable<PlayerState> onPlayerUpdates() {
        return playerStateSubject.startWith(currentState);
    }

    public Observable<PlaybackState> onPlaybackProgress() {
        return playbackStateSubject.startWith(currentState);
    }

    private class PlayerCallbacks implements MediaPlayer.OnPreparedListener,
            MediaPlayer.OnErrorListener,
            MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            next();
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            if (mp == player) {
                Log.e(LOG_TAG, "error with player " + what);
            }
            return false;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            if (mp != player) {
                return;
            }
            currentState.setInitialized(true);
            if (!currentState.isPaused()) {
                player.start();
                updateProgressScheduler.postDelayed(updateProgressTask, PROGRESS_UPDATE_DELAY);
            }
            pushUpdate();
        }
    }

    private class PlaybackProgressUpdateTask implements Runnable  {

        @Override
        public void run() {
            currentState.setPlaybackPosition(player.getCurrentPosition());
            currentState.setPlaybackDuration(player.getDuration());
            playbackStateSubject.onNext(currentState);
            if (!currentState.isPaused()) {
                updateProgressScheduler.postDelayed(this, PROGRESS_UPDATE_DELAY);
            }
        }
    }

    private void pushUpdate() {
        playerStateSubject.onNext(currentState);
    }
}
