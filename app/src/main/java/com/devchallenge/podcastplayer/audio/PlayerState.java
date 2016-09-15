package com.devchallenge.podcastplayer.audio;

import com.devchallenge.podcastplayer.data.model.Podcast;

/**
 * Created by yarolegovich on 13.09.2016.
 */
public interface PlayerState {
    Podcast getCurrentPodcast();
    boolean isPaused();
    boolean isInitialized();
}
