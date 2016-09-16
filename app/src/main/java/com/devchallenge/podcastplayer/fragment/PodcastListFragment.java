package com.devchallenge.podcastplayer.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.devchallenge.podcastplayer.CachedPodcastsActivity;
import com.devchallenge.podcastplayer.R;
import com.devchallenge.podcastplayer.adapter.PodcastListAdapter;
import com.devchallenge.podcastplayer.audio.BackgroundAudioService;
import com.devchallenge.podcastplayer.audio.Player;
import com.devchallenge.podcastplayer.audio.PlayerState;
import com.devchallenge.podcastplayer.data.Cache;
import com.devchallenge.podcastplayer.data.Podcasts;
import com.devchallenge.podcastplayer.data.model.Podcast;
import com.devchallenge.podcastplayer.util.Messages;
import com.devchallenge.podcastplayer.util.NavigationManager;
import com.devchallenge.podcastplayer.util.Permissions;
import com.devchallenge.podcastplayer.util.Utils;
import com.devchallenge.podcastplayer.view.SpacesItemDecoration;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

import static com.devchallenge.podcastplayer.util.Utils.notEqual;

/**
 * Created by MrDeveloper on 14.09.2016.
 */
public class PodcastListFragment extends Fragment implements PodcastListAdapter.PodcastInteractionHandler {

    private static final String LOG_TAG = PodcastListFragment.class.getSimpleName();

    public static final String EXTRA_CHANGED_PODCASTS = "extra_changed_podcasts";

    private static final int REQUEST_CHANGE_CACHE = 2001;

    private NavigationManager navigationManager;
    private PodcastListAdapter adapter;

    private Subscription podcastsSubscription;
    private Subscription playbackStatusSubscription;

    private SwipeRefreshLayout swipeToRefresh;

    private Podcast currentlyPlaying;
    private boolean isPaused = true;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        navigationManager = (NavigationManager) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_podcast_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        adapter = new PodcastListAdapter();
        adapter.setPodcastInteractionHandler(this);
        adapter.setEmptyView(view.findViewById(R.id.podcast_list_empty));
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.podcast_list);
        recyclerView.setLayoutManager(getLayoutManager());
        fixGluedCards(recyclerView);
        recyclerView.setAdapter(adapter);

        swipeToRefresh = (SwipeRefreshLayout) view.findViewById(R.id.podcast_list_swipe_refresh);
        swipeToRefresh.setColorSchemeColors(Utils.swipeToRefreshColors(getActivity()));
        swipeToRefresh.setOnRefreshListener(this::refreshLayout);

        getPodcasts();
        playbackStatusSubscription = Player.getInstance().onPlayerUpdates()
                .subscribe(this::updateItemInAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        podcastsSubscription.unsubscribe();
        playbackStatusSubscription.unsubscribe();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_podcast_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.mi_cache) {
            Intent intent = new Intent(getActivity(), CachedPodcastsActivity.class);
            startActivityForResult(intent, REQUEST_CHANGE_CACHE);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHANGE_CACHE && resultCode == Activity.RESULT_OK) {
            //-Knock, knock. -Who is there? -Type erasure.
            Object[] changedPodcasts = (Object[]) data.getSerializableExtra(EXTRA_CHANGED_PODCASTS);
            adapter.updateItems(changedPodcasts);
        }
    }

    private void refreshLayout() {
        swipeToRefresh.setRefreshing(true);
        Podcasts.getInstance().clearCache();
        getPodcasts();
    }

    private void getPodcasts() {
        podcastsSubscription = Podcasts.getInstance().getPodcasts()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::showPodcastList,
                        this::showNoDataMessage);
    }

    private void showPodcastList(List<Podcast> podcasts) {
        swipeToRefresh.setRefreshing(false);
        adapter.setNewData(podcasts);
    }

    private void showNoDataMessage(Throwable e) {
        Log.e(LOG_TAG, e.getMessage(), e);
        swipeToRefresh.setRefreshing(false);
        Messages.showNoInternetMessage(getActivity());
    }

    private void updateItemInAdapter(PlayerState playerState) {
        Podcast currentPodcast = playerState.getCurrentPodcast();
        if (notEqual(currentPodcast, currentlyPlaying)) {
            adapter.updateItem(currentlyPlaying);
            adapter.updateItem(currentPodcast);
            currentlyPlaying = currentPodcast;
        } else if (isPaused != playerState.isPaused()) {
            adapter.updateItem(currentlyPlaying);
            isPaused = !isPaused;
        }
    }

    @Override
    public void onPlayPodcast(Podcast podcast) {
        Intent intent = new Intent(getActivity(), BackgroundAudioService.class);
        if (isPaused) {
            intent.setAction(BackgroundAudioService.ACTION_START);
            intent.putExtra(BackgroundAudioService.EXTRA_PODCAST, podcast);
        } else {
            intent.setAction(BackgroundAudioService.ACTION_PAUSE);
        }
        getActivity().startService(intent);
    }

    @Override
    public void onCachePodcast(Podcast podcast) {
        Permissions.doIfPermitted(() -> {
                    if (Cache.cacheIfNotCachedYet(getActivity(), podcast)) {
                        adapter.updateItem(podcast);
                    }
                }, Permissions.externalStoragePermissions());
}

    @Override
    public void onPodcastSelected(Podcast podcast) {
        navigationManager.openPodcastInPlayer(podcast);
    }

    private RecyclerView.LayoutManager getLayoutManager() {
        if (Utils.isPortrait() || Utils.isTablet(getActivity())) {
            return new LinearLayoutManager(getActivity());
        } else {
            return new GridLayoutManager(getActivity(), 2);
        }
    }

    private void fixGluedCards(RecyclerView recyclerView) {
        if (Utils.isLandscape() && !Utils.isTablet(getActivity())) {
            int spaceSize = Utils.dpToPx(getActivity(), 4);
            recyclerView.addItemDecoration(new SpacesItemDecoration(spaceSize));
        }
    }
}
