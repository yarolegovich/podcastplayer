package com.devchallenge.podcastplayer;

import android.os.SystemClock;

import com.devchallenge.podcastplayer.data.Cache;
import com.devchallenge.podcastplayer.data.InvalidCacheException;
import com.devchallenge.podcastplayer.data.model.Podcast;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.schedulers.Schedulers;

import static junit.framework.Assert.*;

/**
 * Created by MrDeveloper on 15.09.2016.
 */
@RunWith(RobolectricTestRunner.class)
public class CacheTest {

    private Cache cache;
    private List<Podcast> podcasts;

    @Before
    public void setUp() {
        cache = new Cache(RuntimeEnvironment.application);
        podcasts = PodcastTestSet.getPodcasts();
    }

    @Test
    public void cacheIsValidAfterDataIsSaved() {
        cache.savePodcastList(podcasts, Schedulers.immediate());
        assertEquals(true, cache.isCacheValid());
    }

    @Test
    public void cacheLoadsTheSameListAfterSavingIt() throws InterruptedException {
        cache.savePodcastList(podcasts, Schedulers.immediate());
        List<Podcast> cached = cache.loadPodcastList().toBlocking().first();
        assertEquals(podcasts.size(), cached.size());
        for (Podcast podcast : podcasts) {
            assertEquals(true, cached.contains(podcast));
        }
    }

    @Test
    public void cacheIsInvalidAfterItWasCleared() {
        cache.savePodcastList(podcasts, Schedulers.immediate());
        assertEquals(true, cache.isCacheValid());
        cache.clearCache();
        assertEquals(false, cache.isCacheValid());
    }

    @Test
    public void cacheThrowsExceptionIfInvalid() {
        cache.savePodcastList(podcasts, Schedulers.immediate());
        cache.clearCache();
        final boolean[] propagatedError = {false};
        cache.loadPodcastList().subscribe(list -> {}, error -> {
            propagatedError[0] = error instanceof InvalidCacheException;
        });
        assertEquals(true, propagatedError[0]);
    }
}
