package com.devchallenge.podcastplayer.data.net;

import android.util.Log;
import android.util.Xml;

import com.devchallenge.podcastplayer.data.model.Audio;
import com.devchallenge.podcastplayer.data.model.Podcast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by MrDeveloper on 13.09.2016.
 */
public class PodcastRssParser {

    private static final String LOG_TAG = PodcastRssParser.class.getSimpleName();

    //Badass parsing html with regex
    private static final Pattern IMG_PATTERN = Pattern.compile("<img src=\"([^\"]+)");

    private static final String NS_ITUNES = "http://www.itunes.com/dtds/podcast-1.0.dtd";
    private static final String PREFIX_ITUNES = "itunes";

    private static final String PODCAST = "item";

    private static final String TITLE = "title";
    private static final String PUB_DATE = "pubDate";
    private static final String IMAGE = "description";
    private static final String SUMMARY = "summary";
    private static final String AUTHOR = "author";
    private static final String ENCLOSURE = "enclosure";

    private static final String ATTR_URL = "url";
    private static final String ATTR_LENGTH = "length";

    private final DateFormat dateFormat;

    public PodcastRssParser() {
        dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z", Locale.US);
    }

    @SuppressWarnings("ConstantConditions")
    public List<Podcast> parseRssInputStream(InputStream is) throws IOException, XmlPullParserException {
        try {
            List<Podcast> result = new ArrayList<>();
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            parser.setInput(is, null);
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                if (parser.getName().equals(PODCAST)) {
                    Podcast podcast = extractPodcast(parser);
                    //I don't know how often this happen, but one item in comes only with title and
                    //description. Podcast 498.
                    if (podcast.getAudio() != null) {
                        result.add(podcast);
                    }
                }
            }
            return result;
        } finally {
            is.close();
        }
    }

    private Podcast extractPodcast(XmlPullParser parser) throws IOException, XmlPullParserException {
        Podcast podcast = new Podcast();
        parser.require(XmlPullParser.START_TAG, XmlPullParser.NO_NAMESPACE, PODCAST);
        while (!(parser.next() == XmlPullParser.END_TAG && parser.getName().equals(PODCAST))) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case TITLE:
                    podcast.setTitle(readTextTag(parser, TITLE));
                    break;
                case PUB_DATE:
                    String dateString = readTextTag(parser, PUB_DATE);
                    try {
                        podcast.setPubDate(dateFormat.parse(dateString));
                    } catch (ParseException e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                    }
                    break;
                case SUMMARY:
                    if (PREFIX_ITUNES.equals(parser.getPrefix())) {
                        String description = readTextTag(parser, NS_ITUNES, SUMMARY)
                                .replaceAll("(?<=\\.) ", "\n")
                                .trim();
                        podcast.setDescription(description);
                    }
                    break;
                case IMAGE:
                    String description = readTextTag(parser, IMAGE);
                    podcast.setImageUrl(getImageUrlFromDescription(description));
                    break;
                case AUTHOR:
                    if (PREFIX_ITUNES.equals(parser.getPrefix())) {
                        podcast.setAuthors(readTextTag(parser, NS_ITUNES, AUTHOR));
                    }
                    break;
                case ENCLOSURE:
                    podcast.setAudioUrl(readLink(parser));
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        return podcast;
    }

    private String readTextTag(XmlPullParser parser, String tagName) throws IOException, XmlPullParserException {
        return readTextTag(parser, XmlPullParser.NO_NAMESPACE, tagName);
    }

    private String readTextTag(XmlPullParser parser, String tagNamespace, String tagName) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, tagNamespace, tagName);
        String summary = readText(parser);
        parser.require(XmlPullParser.END_TAG, tagNamespace, tagName);
        return summary;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private Audio readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        String link = "";
        int length = 0;
        parser.require(XmlPullParser.START_TAG, XmlPullParser.NO_NAMESPACE, ENCLOSURE);
        String tag = parser.getName();
        if (tag.equals(ENCLOSURE)) {
            link = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, ATTR_URL);
            length = Integer.parseInt(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, ATTR_LENGTH));
            parser.nextTag();
        }
        parser.require(XmlPullParser.END_TAG, XmlPullParser.NO_NAMESPACE, ENCLOSURE);
        return new Audio(link, length);
    }

    private String getImageUrlFromDescription(String description) {
        Matcher matcher = IMG_PATTERN.matcher(description);
        return matcher.find() ? matcher.group(1) : "";
    }

    private void skip(XmlPullParser parser) throws IOException, XmlPullParserException {
        int depth = 1;
        while (depth != 0) {
            int next = parser.next();
            if (next == XmlPullParser.END_TAG) depth--;
            else if (next == XmlPullParser.START_TAG) depth++;
        }
    }
}
