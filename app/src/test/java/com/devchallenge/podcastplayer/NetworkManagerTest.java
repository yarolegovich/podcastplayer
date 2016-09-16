package com.devchallenge.podcastplayer;

import com.devchallenge.podcastplayer.data.model.Podcast;
import com.devchallenge.podcastplayer.data.net.NetworkManager;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static junit.framework.Assert.*;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Objects;

/**
 * Created by MrDeveloper on 15.09.2016.
 */
@RunWith(RobolectricTestRunner.class)
public class NetworkManagerTest {

    private NetworkManager nm;

    @Before
    public void setUp() {
        nm = new NetworkManager();
    }

    @Test
    public void itemsThatComeFromRssCanBeParsed() {
        List<Podcast> podcasts = nm.getRss().toBlocking().first();
        assertTrue(podcasts.size() > 0);
    }

    @Test
    public void allItemsFieldsAreInitialized() {
        List<Podcast> podcasts = nm.getRss().toBlocking().first();
        for (Podcast podcast : podcasts) {
            assertNotNull(podcast.getTitle());
            assertNotNull(podcast.getAuthors());
            assertNotNull(podcast.getDescription());
            assertNotNull(podcast.getPubDate());
            assertNotNull(podcast.getImageUrl());
            assertNotNull(podcast.getAudio().getUrl());
            assertTrue(podcast.getAudio().getLength() > 0);
        }
    }

}
