package com.devchallenge.podcastplayer;

import android.app.Application;
import android.content.Context;

import com.devchallenge.podcastplayer.audio.Player;
import com.devchallenge.podcastplayer.data.Podcasts;
import com.devchallenge.podcastplayer.data.net.SyncService;
import com.karumi.dexter.Dexter;

/**
 * Created by MrDeveloper on 14.09.2016.
 */
public class App extends Application {

    private static App instance;

    private Podcasts podcasts;
    private Player player;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Dexter.initialize(this);

        SyncService.scheduleDataSync(this);

        podcasts = Podcasts.getInstance();
        player = Player.getInstance();
    }

    public static Context getInstance() {
        return instance;
    }

    public Player getPlayer() {
        return player;
    }

    public Podcasts getPodcasts() {
        return podcasts;
    }
}
