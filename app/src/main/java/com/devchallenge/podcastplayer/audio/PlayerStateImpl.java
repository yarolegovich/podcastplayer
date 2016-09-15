package com.devchallenge.podcastplayer.audio;

import com.devchallenge.podcastplayer.data.model.Podcast;


import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by yarolegovich on 13.09.2016.
 */
class PlayerStateImpl implements PlayerState, PlaybackState {

    private Podcast podcast;
    private boolean isPaused;
    private boolean isInitialized;
    private int playbackPosition;
    private int playbackDuration;

    public PlayerStateImpl() {
        isPaused = true;
    }

    @Override
    public Podcast getCurrentPodcast() {
        return podcast;
    }

    @Override
    public boolean isPaused() {
        return isPaused;
    }

    public void setPodcast(Podcast podcast) {
        if (!podcast.equals(this.podcast)) {
            this.podcast = podcast;
        }
    }

    public void setPaused(boolean paused) {
        if (isPaused != paused) {
            isPaused = paused;
        }
    }

    @Override
    public int getPlaybackPosition() {
        return playbackPosition;
    }

    public void setPlaybackPosition(int playbackPosition) {
        this.playbackPosition = playbackPosition;
    }

    @Override
    public int getPlaybackDuration() {
        return playbackDuration;
    }

    public void setPlaybackDuration(int playbackDuration) {
        this.playbackDuration = playbackDuration;
    }

    public void clear() {
        isInitialized = false;
        isPaused = true;
        podcast = null;
        playbackPosition = 0;
        playbackDuration = 0;
    }

    @Override
    public String toString() {
        return "PlayerStateImpl{" +
                "podcast=" + podcast +
                ", isPaused=" + isPaused +
                '}';
    }

    @Override
    public boolean isInitialized() {
        return isInitialized;
    }

    public void setInitialized(boolean initialized) {
        isInitialized = initialized;
    }
}
