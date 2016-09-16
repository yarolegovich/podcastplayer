package com.devchallenge.podcastplayer.data.net;

import com.devchallenge.podcastplayer.data.model.Podcast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import rx.Observable;


/**
 * Created by MrDeveloper on 13.09.2016.
 */
public class NetworkManager {

    private static final String RSS_FEED = "http://feeds.rucast.net/radio-t";

    private PodcastRssParser parser;

    public NetworkManager() {
        parser = new PodcastRssParser();
    }

    public Observable<List<Podcast>> getRss() {
        //I'm not chaining here with .map(parser::parser), because fromCallable deals with errors
        return Observable.fromCallable(() ->
                parser.parseRssInputStream(
                        openRssInputStream()));
    }

    private InputStream openRssInputStream() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(RSS_FEED).openConnection();
        conn.setRequestMethod("GET");
        conn.addRequestProperty("Accept", "application/xml");
        conn.setDoInput(true);
        conn.connect();
        return conn.getInputStream();
    }
}
