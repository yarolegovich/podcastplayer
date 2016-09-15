package com.devchallenge.podcastplayer.audio;

import com.devchallenge.podcastplayer.data.model.Podcast;

/**
 * Created by yarolegovich on 15.09.2016.
 */
public interface PlaybackState {
    Podcast getCurrentPodcast();
    int getPlaybackPosition();
    int getPlaybackDuration();
}
