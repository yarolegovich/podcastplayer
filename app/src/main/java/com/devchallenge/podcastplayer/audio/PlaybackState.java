package com.devchallenge.podcastplayer.audio;

import com.devchallenge.podcastplayer.data.model.Podcast;

/**
 * Created by MrDeveloper on 15.09.2016.
 */
public interface PlaybackState {

    PlaybackState NO_PLAYBACK = new PlaybackState() {
        @Override
        public Podcast getCurrentPodcast() {
            return null;
        }

        @Override
        public int getPlaybackPosition() {
            return 0;
        }

        @Override
        public int getPlaybackDuration() {
            return 0;
        }
    };

    Podcast getCurrentPodcast();
    int getPlaybackPosition();
    int getPlaybackDuration();
}
