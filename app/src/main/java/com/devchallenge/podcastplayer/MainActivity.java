package com.devchallenge.podcastplayer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.devchallenge.podcastplayer.data.Podcasts;
import com.devchallenge.podcastplayer.fragment.EmptyPlayerFragment;
import com.devchallenge.podcastplayer.fragment.PlayerFragment;
import com.devchallenge.podcastplayer.fragment.PodcastListFragment;
import com.devchallenge.podcastplayer.data.model.Podcast;
import com.devchallenge.podcastplayer.util.NavigationManager;
import com.devchallenge.podcastplayer.util.Utils;

import java.util.List;

import rx.Subscription;

public class MainActivity extends AppCompatActivity implements NavigationManager  {

    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.list_container);
        if (fragment == null) {
            fragment = new PodcastListFragment();
            fm.beginTransaction().replace(R.id.list_container, fragment).commit();
        }

        if (Utils.isTablet(this)) {
            showNoPodcastFragment();
            subscription = Podcasts.getInstance().
                    getPodcasts()
                    .subscribe(this::showPodcastPlayerFragment,
                            this::showNoPodcastFragment);
        }
    }

    @Override
    public void openPodcastInPlayer(Podcast podcast) {
        if (Utils.isTablet(this)) {
            Fragment fragment = PlayerFragment.createFor(podcast);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.player_container, fragment)
                    .commit();
        } else {
            startActivity(PlayerActivity.callingIntent(this, podcast));
        }
    }

    private void showPodcastPlayerFragment(List<Podcast> podcasts) {
        openPodcastInPlayer(podcasts.get(0));
    }

    private void showNoPodcastFragment() {
        showNoPodcastFragment(null);
    }

    private void showNoPodcastFragment(Throwable e) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.player_container, new EmptyPlayerFragment())
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }
}
