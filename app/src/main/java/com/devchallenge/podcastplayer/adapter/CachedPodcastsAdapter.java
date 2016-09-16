package com.devchallenge.podcastplayer.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.devchallenge.podcastplayer.R;
import com.devchallenge.podcastplayer.data.Cache;
import com.devchallenge.podcastplayer.data.model.CachedPodcast;
import com.devchallenge.podcastplayer.util.Messages;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MrDeveloper on 15.09.2016.
 */
public class CachedPodcastsAdapter extends RecyclerView.Adapter<CachedPodcastsAdapter.ViewHolder> {

    private List<CachedPodcast> cachedPodcasts;
    private View emptyView;

    private CachedPodcastInteractionListener listener;

    public CachedPodcastsAdapter() {
        cachedPodcasts = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_cached_podcast, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CachedPodcast podcast = cachedPodcasts.get(position);
        holder.cachedPodcast = podcast;
        holder.title.setText(podcast.title);
    }

    @Override
    public int getItemCount() {
        return cachedPodcasts.size();
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
        changeEmptyViewVisibility();
    }

    public void setData(List<CachedPodcast> data) {
        if (data != null) {
            this.cachedPodcasts.clear();
            this.cachedPodcasts.addAll(data);
            changeEmptyViewVisibility();
        }
    }

    public void removeItem(int position, CachedPodcast podcast) {
        cachedPodcasts.remove(podcast);
        notifyItemRemoved(position);
        changeEmptyViewVisibility();
    }

    private void changeEmptyViewVisibility() {
        if (emptyView != null) {
            emptyView.setVisibility(cachedPodcasts.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    public void setInteractionListener(CachedPodcastInteractionListener listener) {
        this.listener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CachedPodcast cachedPodcast;

        private TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.cached_podcast_title);

            itemView.findViewById(R.id.cached_podcast_btn_delete).setOnClickListener(this);
            itemView.findViewById(R.id.container).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Cache cache = new Cache(v.getContext());
            if (cache.isCachingInProgress(cachedPodcast)) {
                Messages.showCachingInProgressMessage(v.getContext());
                return;
            }
            if (listener != null) {
                switch (v.getId()) {
                    case R.id.cached_podcast_btn_delete:
                        listener.onRemovePodcast(getAdapterPosition(), cachedPodcast);
                        break;
                    case R.id.container:
                        listener.onPodcastClicked(cachedPodcast);
                        break;
                }
            }
        }
    }

    public interface CachedPodcastInteractionListener {
        void onPodcastClicked(CachedPodcast podcast);
        void onRemovePodcast(int adapterPosition, CachedPodcast podcast);
    }
}
