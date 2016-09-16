package com.devchallenge.podcastplayer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.devchallenge.podcastplayer.fragment.PlayerFragment;
import com.devchallenge.podcastplayer.data.model.Podcast;
import com.devchallenge.podcastplayer.util.NavigationManager;

/**
 * Created by MrDeveloper on 13.09.2016.
 */
public class PlayerActivity extends AppCompatActivity implements NavigationManager {

    private static final String EXTRA_PODCAST = "extra_podcast";

    public static Intent callingIntent(Context context, Podcast podcast) {
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.putExtra(EXTRA_PODCAST, podcast);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        if (savedInstanceState == null) {
            Podcast podcast = (Podcast) getIntent().getSerializableExtra(EXTRA_PODCAST);
            openPodcastInPlayer(podcast);
        }
    }

    @Override
    public void openPodcastInPlayer(Podcast podcast) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = PlayerFragment.createFor(podcast);
        fm.beginTransaction().replace(R.id.player_container, fragment).commit();
    }

}
