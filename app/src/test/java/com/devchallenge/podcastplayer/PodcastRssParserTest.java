package com.devchallenge.podcastplayer;

import com.devchallenge.podcastplayer.data.model.Audio;
import com.devchallenge.podcastplayer.data.model.Podcast;
import com.devchallenge.podcastplayer.data.net.PodcastRssParser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(RobolectricTestRunner.class)
public class PodcastRssParserTest {

    private static final String TITLE = "Радио-Т 512";
    private static final String PUB_DATE = "Sat, 10 Sep 2016 16:43:00 PDT";
    private static final String IMAGE_URL = "https://radio-t.com/images/radio-t/rt512.jpg";
    private static final String AUDIO_URL = "http://cdn.radio-t.com/rt_podcast512.mp3";
    private static final int AUDIO_SIZE = 115483969;
    private static final String AUTHORS = "Umputun, Bobuk, Gray, Ksenks";
    private static final String DESCRIPTION = "Что нам показл Apple - 00:00:08";

    private PodcastRssParser parser;
    private Podcast podcastToCompareWith;

    @Before
    public void setUp() {
        DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z", Locale.US);

        parser = new PodcastRssParser();

        podcastToCompareWith = new Podcast();
        podcastToCompareWith.setTitle(TITLE);
        try {
            podcastToCompareWith.setPubDate(dateFormat.parse(PUB_DATE));
        } catch (ParseException e) {
            fail();
        }
        podcastToCompareWith.setAudioUrl(new Audio(AUDIO_URL, AUDIO_SIZE));
        podcastToCompareWith.setDescription(DESCRIPTION);
        podcastToCompareWith.setAuthors(AUTHORS);
        podcastToCompareWith.setImageUrl(IMAGE_URL);
    }

    @Test
    public void parserParsesXmlWithSingleItem() throws IOException, XmlPullParserException {
        String xmlToParse = String.format(EXAMPLE_XML, EXAMPLE_ITEM);
        List<Podcast> parsed = parser.parseRssInputStream(stringToInputStream(xmlToParse));
        assertEquals(1, parsed.size());
        assertPodcastsEqual(podcastToCompareWith, parsed.get(0));
    }

    @Test
    public void parserParsesXmlWithMultipleItems() throws IOException, XmlPullParserException {
        String xmlToParse = String.format(EXAMPLE_XML, EXAMPLE_ITEM + EXAMPLE_ITEM + EXAMPLE_ITEM);
        List<Podcast> parsed = parser.parseRssInputStream(stringToInputStream(xmlToParse));
        assertEquals(3, parsed.size());
        for (Podcast podcast : parsed) {
            assertPodcastsEqual(podcastToCompareWith, podcast);
        }
    }

    @Test
    public void parserReturnsEmptyListIfNoItemsInXml() throws IOException, XmlPullParserException {
        String xmlToParse = String.format(EXAMPLE_XML, "");
        List<Podcast> parsed = parser.parseRssInputStream(stringToInputStream(xmlToParse));
        assertTrue(parsed.isEmpty());
    }

    @Test(expected = XmlPullParserException.class)
    public void parserThrowsExceptionIfInvalidXml() throws IOException, XmlPullParserException {
        String xmlToParse = "<rss><channel><item></channel></rss>";
        List<Podcast> parsed = parser.parseRssInputStream(stringToInputStream(xmlToParse));
        assertTrue(parsed.isEmpty());
    }

    private static void assertPodcastsEqual(Podcast p1, Podcast p2) {
        assertEquals(p1.getTitle(), p2.getTitle());
        assertEquals(p1.getPubDate(), p2.getPubDate());
        assertEquals(p1.getAudio().getUrl(), p2.getAudio().getUrl());
        assertEquals(p1.getAudio().getLength(), p2.getAudio().getLength());
        assertEquals(p1.getImageUrl(), p2.getImageUrl());
        assertEquals(p1.getDescription(), p2.getDescription());
        assertEquals(p1.getAuthors(), p2.getAuthors());
    }

    private InputStream stringToInputStream(String str) {
        try {
            return new ByteArrayInputStream(str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            fail();
            return null;
        }
    }

    private static final String EXAMPLE_ITEM = "<item>\n"
            + "<title>" + TITLE + "</title>\n"
            + "<pubDate>" + PUB_DATE + "</pubDate>\n"
            + "<description>\n"
            + "<![CDATA[<html><p><img src=\"" + IMAGE_URL + "\" alt=\"\" /></p> <ul> <li>]]>"
            + "</description>\n"
            + "<enclosure url=\"" + AUDIO_URL + "\" length=\"" + AUDIO_SIZE + "\" type=\"audio/mpeg\"/>\n"
            + "<itunes:author>" + AUTHORS + "</itunes:author>\n"
            + "<itunes:summary>\n" + DESCRIPTION + "</itunes:summary>\n" +
            "</item>\n";

    private static final String EXAMPLE_XML =
            "<rss xmlns:media=\"http://search.yahoo.com/mrss/\" xmlns:itunes=\"http://www.itunes.com/dtds/podcast-1.0.dtd\" xmlns:creativeCommons=\"http://backend.userland.com/creativeCommonsRssModule\" version=\"2.0\">\n" +
                    "<channel>\n%s</channel>" +
                    "</rss>";
}