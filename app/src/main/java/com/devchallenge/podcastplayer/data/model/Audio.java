package com.devchallenge.podcastplayer.data.model;

import java.io.Serializable;

/**
 * Created by MrDeveloper on 13.09.2016.
 */
public class Audio implements Serializable {

    private String url;
    private int length;

    public Audio(String url, int length) {
        this.url = url;
        this.length = length;
    }

    public String getUrl() {
        return url;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "Audio{" +
                "url='" + url + '\'' +
                ", length=" + length +
                '}';
    }
}
