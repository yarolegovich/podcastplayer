package com.devchallenge.podcastplayer.data.model;

import android.net.Uri;

/**
 * Created by MrDeveloper on 15.09.2016.
 */
public class CachedPodcast {

    public final String title;
    public final Uri uri;

    public CachedPodcast(String title, Uri uri) {
        this.title = title;
        this.uri = uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CachedPodcast podcast = (CachedPodcast) o;

        return title != null ? title.equals(podcast.title) : podcast.title == null;

    }

    @Override
    public int hashCode() {
        return title != null ? title.hashCode() : 0;
    }
}
