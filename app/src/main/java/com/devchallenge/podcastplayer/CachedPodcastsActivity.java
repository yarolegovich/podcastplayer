package com.devchallenge.podcastplayer;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.devchallenge.podcastplayer.adapter.CachedPodcastsAdapter;
import com.devchallenge.podcastplayer.data.Cache;
import com.devchallenge.podcastplayer.data.model.CachedPodcast;
import com.devchallenge.podcastplayer.data.model.Podcast;
import com.devchallenge.podcastplayer.fragment.PodcastListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MrDeveloper on 15.09.2016.
 */
public class CachedPodcastsActivity extends AppCompatActivity implements
        CachedPodcastsAdapter.CachedPodcastInteractionListener {

    private CachedPodcastsAdapter adapter;
    private List<Podcast> removedPodcasts;

    private Cache cache;

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cached_podcasts);

        removedPodcasts = new ArrayList<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setTitle(R.string.cache);
        ab.setDisplayHomeAsUpEnabled(true);

        cache = new Cache(this);
        List<CachedPodcast> cachedPodcasts = cache.getCachedPodcasts();
        adapter = new CachedPodcastsAdapter();
        adapter.setEmptyView(findViewById(R.id.cached_podcasts_list_empty));
        adapter.setInteractionListener(this);
        adapter.setData(cachedPodcasts);

        RecyclerView list = (RecyclerView) findViewById(R.id.cached_podcasts_list);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPodcastClicked(CachedPodcast podcast) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(podcast.uri, "audio/mpeg");
        startActivity(intent);
    }

    @Override
    public void finish() {
        Intent result = new Intent();
        Podcast[] changed = removedPodcasts.toArray(new Podcast[removedPodcasts.size()]);
        result.putExtra(PodcastListFragment.EXTRA_CHANGED_PODCASTS, changed);
        setResult(RESULT_OK, result);
        super.finish();
    }

    @Override
    public void onRemovePodcast(int adapterPosition, CachedPodcast podcast) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.d_title_remove_from_cache)
                .setMessage(R.string.d_msg_remove_from_cache)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    cache.removePodcastAudioFromCache(podcast);
                    adapter.removeItem(adapterPosition, podcast);
                    removedPodcasts.add(new Podcast(podcast.title));
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
