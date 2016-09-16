package com.devchallenge.podcastplayer.data.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by MrDeveloper on 13.09.2016.
 */
public class Podcast implements Serializable {

    private String title;
    private Date pubDate;
    private String description;
    private String imageUrl;
    private String authors;
    private Audio audio;

    public Podcast() { }

    public Podcast(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getPubDate() {
        return pubDate;
    }

    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public Audio getAudio() {
        return audio;
    }

    public void setAudioUrl(Audio audio) {
        this.audio = audio;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof Podcast && title.equals(((Podcast) obj).getTitle());
    }

    @Override
    public String toString() {
        return "Podcast{" +
                "title='" + title + '\'' +
                ", pubDate='" + pubDate + '\'' +
                ", audioUrl='" + audio + '\'' +
                '}';
    }
}
