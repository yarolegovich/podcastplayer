package com.devchallenge.podcastplayer;

import com.devchallenge.podcastplayer.data.Cache;
import com.devchallenge.podcastplayer.data.Podcasts;
import com.devchallenge.podcastplayer.data.model.Podcast;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.*;

import java.util.List;

import rx.schedulers.Schedulers;

/**
 * Created by MrDeveloper on 15.09.2016.
 */
@Config(shadows = {ShadowApp.class})
@RunWith(RobolectricTestRunner.class)
public class PodcastsTest {

    private Podcasts podcasts;
    private List<Podcast> podcastList;
    private Cache cache;

    @Before
    public void setUp() {
        podcasts = Podcasts.getInstance();
        podcastList = PodcastTestSet.getPodcasts();
        cache = new Cache(RuntimeEnvironment.application);
    }

    @Test
    public void takesPodcastsFromCacheIfThereIsAValidOne() {
        cache.savePodcastList(podcastList, Schedulers.immediate());
        List<Podcast> cached = podcasts.getPodcasts().toBlocking().first();
        assertEquals(podcastList.size(), cached.size());
        for (Podcast podcast : podcastList) {
            assertTrue(cached.contains(podcast));
        }
    }

    @Test
    public void takesPodcastsFromNetworkIfNothingInCache() {
        assertFalse(cache.isCacheValid());
        List<Podcast> fromNetwork = podcasts.getPodcasts().toBlocking().first();
        assertFalse(fromNetwork.isEmpty());
    }

    @Test
    public void nextReturnsNextInTheListAndIsCircular() {
        cache.savePodcastList(podcastList, Schedulers.immediate());
        assertEquals(podcastList.get(1), podcasts.next(podcastList.get(0)));
        assertEquals(podcastList.get(0), podcasts.next(podcastList.get(podcastList.size() - 1)));
    }

    @Test
    public void previousReturnsPreviousInTheListAndIsCircular() {
        cache.savePodcastList(podcastList, Schedulers.immediate());
        assertEquals(podcastList.get(0), podcasts.previous(podcastList.get(1)));
        assertEquals(podcastList.get(podcastList.size() - 1), podcasts.previous(podcastList.get(0)));
    }
}
