package com.devchallenge.podcastplayer.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.devchallenge.podcastplayer.R;
import com.devchallenge.podcastplayer.audio.Player;
import com.devchallenge.podcastplayer.audio.PlayerState;
import com.devchallenge.podcastplayer.data.Cache;
import com.devchallenge.podcastplayer.data.model.Podcast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by yarolegovich on 13.09.2016.
 */
public class PodcastListAdapter extends RecyclerView.Adapter<PodcastListAdapter.ViewHolder> {

    private final DateFormat dateFormat;

    private PodcastInteractionHandler podcastInteractionHandler;
    private List<Podcast> podcasts;

    private View emptyView;

    private Cache cache;

    public PodcastListAdapter() {
        this.podcasts = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("MMM dd, hh:mm", Locale.getDefault());
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        cache = new Cache(recyclerView.getContext());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_podcast, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Podcast podcast = podcasts.get(position);

        holder.podcast = podcast;
        holder.podcastTitle.setText(podcast.getTitle());
        holder.podcastDate.setText(dateFormat.format(podcast.getPubDate()));

        if (holder.isComplexLayout()) {
            Glide.with(holder.podcastImage.getContext())
                    .load(podcast.getImageUrl())
                    .into(holder.podcastImage);
            if (cache.isPodcastAudioCached(podcast)) {
                holder.downloadButton.setText(R.string.in_cache);
                holder.downloadButton.setEnabled(false);
            } else {
                holder.downloadButton.setText(R.string.download);
                holder.downloadButton.setEnabled(true);
            }
        }

        PlayerState playerState = Player.getInstance().getState();
        Podcast currentlyPlaying = playerState.getCurrentPodcast();
        holder.setIsPlaying(podcast.equals(currentlyPlaying) && !playerState.isPaused());
    }

    @Override
    public int getItemCount() {
        return podcasts.size();
    }

    public void setNewData(List<Podcast> podcasts) {
        if (podcasts != null) {
            this.podcasts.clear();
            this.podcasts.addAll(podcasts);
            setEmptyViewVisibility();
            notifyDataSetChanged();
        }
    }

    public void setPodcastInteractionHandler(PodcastInteractionHandler podcastInteractionHandler) {
        this.podcastInteractionHandler = podcastInteractionHandler;
    }

    public void updateItem(Podcast podcast) {
        if (podcast != null) {
            int index = findIndex(podcast);
            if (index >= 0) {
                notifyItemChanged(index);
            }
        }
    }

    private int findIndex(Podcast podcast) {
        for (int i = 0; i < podcasts.size(); i++) {
            if (podcast.equals(podcasts.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
        setEmptyViewVisibility();
    }

    private void setEmptyViewVisibility() {
        if (emptyView != null) {
            emptyView.setVisibility(podcasts.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Podcast podcast;

        private ImageView podcastImage;
        private TextView podcastTitle;
        private TextView podcastDate;

        private Button downloadButton;
        private Button playButtonBtn;
        private ImageView playButtonIv;

        public ViewHolder(View itemView) {
            super(itemView);

            podcastImage = (ImageView) itemView.findViewById(R.id.li_podcast_image);
            podcastTitle = (TextView) itemView.findViewById(R.id.li_podcast_title);
            podcastDate = (TextView) itemView.findViewById(R.id.li_podcast_date);

            View playButton = itemView.findViewById(R.id.li_btn_play);
            playButton.setOnClickListener(this);
            if (playButton instanceof Button) {
                playButtonBtn = (Button) playButton;
            } else {
                playButtonIv = (ImageView) playButton;
            }

            itemView.findViewById(R.id.container).setOnClickListener(this);
            downloadButton = (Button) itemView.findViewById(R.id.li_btn_cache);
            if (downloadButton != null) {
                downloadButton.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            if (podcastInteractionHandler != null) {
                switch (v.getId()) {
                    case R.id.li_btn_play:
                        podcastInteractionHandler.onPlayPodcast(podcast);
                        break;
                    case R.id.li_btn_cache:
                        podcastInteractionHandler.onCachePodcast(podcast);
                        break;
                    case R.id.container:
                        podcastInteractionHandler.onPodcastSelected(podcast);
                        break;
                }
            }
        }

        public void setIsPlaying(boolean isPlaying) {
            if (playButtonBtn != null) {
                playButtonBtn.setText(isPlaying ? R.string.pause : R.string.play);
            } else {
                playButtonIv.setImageResource(isPlaying ?
                        R.drawable.ic_pause_black_24dp :
                        R.drawable.ic_play_arrow_black_24dp);
            }
        }

        public boolean isComplexLayout() {
            return podcastImage != null;
        }
    }

    public interface PodcastInteractionHandler {
        void onPlayPodcast(Podcast podcast);

        void onCachePodcast(Podcast podcast);

        void onPodcastSelected(Podcast podcast);
    }
}
