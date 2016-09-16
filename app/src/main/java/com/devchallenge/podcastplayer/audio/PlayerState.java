package com.devchallenge.podcastplayer.audio;

import android.support.annotation.Nullable;

import com.devchallenge.podcastplayer.data.model.Podcast;

/**
 * Created by MrDeveloper on 13.09.2016.
 */
public interface PlayerState {
    @Nullable
    Podcast getCurrentPodcast();
    boolean isPaused();
    boolean isInitialized();
}
