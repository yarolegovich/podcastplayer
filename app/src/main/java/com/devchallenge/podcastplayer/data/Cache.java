package com.devchallenge.podcastplayer.data;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import com.devchallenge.podcastplayer.data.model.Podcast;
import com.devchallenge.podcastplayer.util.Messages;

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by yarolegovich on 13.09.2016.
 */
public class Cache {

    private static final String LOG_TAG = Cache.class.getSimpleName();

    private static final String APP_FOLDER = "/podcast_player/";

    private static final String CACHE_META = "cache_meta";
    private static final String KEY_CACHE_TIME = "cache_time";

    private static final String PODCASTS_LIST = "podcasts";

    private static final long CACHE_LIFETIME = TimeUnit.HOURS.toMillis(5);

    private SharedPreferences meta;
    private Context context;

    public Cache(Context context) {
        this.context = context.getApplicationContext();
        this.meta = context.getSharedPreferences(CACHE_META, Context.MODE_PRIVATE);
    }

    public Observable<List<Podcast>> loadPodcastList() {
        return Observable.create((s) -> {
            if (isCacheValid()) {
                List<Podcast> podcasts = new ArrayList<>();
                ObjectInputStream ois = null;
                try {
                    ois = new ObjectInputStream(context.openFileInput(PODCASTS_LIST));
                    //noinspection InfiniteLoopStatement
                    while (true) {
                        podcasts.add((Podcast) ois.readObject());
                    }
                } catch (EOFException eof) {
                    s.onNext(podcasts);
                } catch (IOException | ClassNotFoundException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    s.onError(e);
                } finally {
                    close(ois);
                }
            } else {
                s.onError(new InvalidCacheException());
            }
            s.onCompleted();
        });
    }

    public void cachePodcastList(List<Podcast> podcasts) {
        Observable.just(podcasts)
                .observeOn(Schedulers.io())
                .subscribe(list -> {
                    ObjectOutputStream oos = null;
                    try {
                        oos = new ObjectOutputStream(context.openFileOutput(PODCASTS_LIST, Context.MODE_PRIVATE));
                        for (Podcast podcast : list) {
                            oos.writeObject(podcast);
                        }
                        meta.edit().putLong(KEY_CACHE_TIME, System.currentTimeMillis()).apply();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                    } finally {
                        close(oos);
                    }
                });
    }

    public void clearCache() {
        context.deleteFile(PODCASTS_LIST);
        meta.edit().putLong(KEY_CACHE_TIME, 0).apply();
    }

    public boolean isCacheValid() {
        return System.currentTimeMillis() - meta.getLong(KEY_CACHE_TIME, 0) < CACHE_LIFETIME;
    }

    public boolean isPodcastAudioCached(Podcast podcast) {
        return new File(audioCacheDir(), podcast.getTitle()).exists();
    }

    public boolean removePodcastAudioFromCache(Podcast podcast) {
        return new File(audioCacheDir(), podcast.getTitle()).delete();
    }

    public Uri getCachedAudioUri(Podcast podcast) {
        return Uri.fromFile(new File(audioCacheDir(), podcast.getTitle()));
    }

    public void cachePodcastAudio(Podcast podcast) {
        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(podcast.getAudio().getUrl());
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setTitle(podcast.getTitle());
        request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                APP_FOLDER + podcast.getTitle());
        dm.enqueue(request);
    }

    private File audioCacheDir() {
        return new File(Environment.getExternalStorageDirectory(),
                Environment.DIRECTORY_DOWNLOADS + APP_FOLDER);
    }

    private void close(@Nullable Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) { /* NOP */ }
        }
    }

    /**
     * @return true if cached, false if was already in cache
     */
    public static boolean cacheIfNotCachedYet(Context context, Podcast podcast) {
        Cache cache = new Cache(context);
        if (cache.isPodcastAudioCached(podcast)) {
            Messages.showAlreadyInCacheMessage(context);
            return false;
        } else {
            cache.cachePodcastAudio(podcast);
            return true;
        }
    }
}
