package com.devchallenge.podcastplayer;

import com.devchallenge.podcastplayer.data.model.Podcast;

import java.util.Arrays;
import java.util.List;

/**
 * Created by MrDeveloper on 15.09.2016.
 */
public class PodcastTestSet {

    public static List<Podcast> getPodcasts() {
        return Arrays.asList(
                new Podcast("Hello world"),
                new Podcast("Who is bad in writing tests?"),
                new Podcast("I'm bad"),
                new Podcast("Will this tests help my project?"));
    }
}
