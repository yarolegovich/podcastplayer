package com.devchallenge.podcastplayer;

import android.app.Application;

import com.devchallenge.podcastplayer.audio.Player;
import com.devchallenge.podcastplayer.data.Podcasts;
import com.devchallenge.podcastplayer.data.net.SyncService;
import com.karumi.dexter.Dexter;

/**
 * Created by yarolegovich on 14.09.2016.
 */
public class App extends Application {

    private Podcasts podcasts;
    private Player player;

    /*
     * //TODO:
     * 2. Cache screen, removal and saving meta info (cover link, title)
     * 3. Localisation.
     * 4. Unit tests... maybe.
     */

    @Override
    public void onCreate() {
        super.onCreate();
        Podcasts.init(this);
        Player.init(this);
        Dexter.initialize(this);

        SyncService.scheduleDataSync(this);

        podcasts = Podcasts.getInstance();
        player = Player.getInstance();
    }

    public Player getPlayer() {
        return player;
    }

    public Podcasts getPodcasts() {
        return podcasts;
    }
}
