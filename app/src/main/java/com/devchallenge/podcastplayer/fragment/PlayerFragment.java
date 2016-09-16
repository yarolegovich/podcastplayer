package com.devchallenge.podcastplayer.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.devchallenge.podcastplayer.R;
import com.devchallenge.podcastplayer.audio.BackgroundAudioService;
import com.devchallenge.podcastplayer.audio.PlaybackState;
import com.devchallenge.podcastplayer.audio.Player;
import com.devchallenge.podcastplayer.audio.PlayerState;
import com.devchallenge.podcastplayer.data.Cache;
import com.devchallenge.podcastplayer.data.Podcasts;
import com.devchallenge.podcastplayer.data.model.Podcast;
import com.devchallenge.podcastplayer.util.NavigationManager;
import com.devchallenge.podcastplayer.util.Permissions;
import com.devchallenge.podcastplayer.util.Utils;

import rx.Subscription;

/**
 * Created by MrDeveloper on 14.09.2016.
 */
public class PlayerFragment extends Fragment implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener {

    private static final String LOG_TAG = PlayerFragment.class.getSimpleName();

    private static final String ARG_PODCAST = "podcast";

    public static PlayerFragment createFor(Podcast podcast) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PODCAST, podcast);
        fragment.setArguments(args);
        return fragment;
    }

    private NavigationManager navigationManager;
    private Podcasts podcasts;
    private Podcast podcast;

    private BackgroundAudioService audioService;

    private Subscription playerStateSubscription;
    private Subscription playbackProgressSubscription;

    private ImageView playButton;
    private TextView playbackTime;
    private SeekBar seekBar;

    private ServiceConnection connection;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        navigationManager = (NavigationManager) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        podcast = (Podcast) getArguments().getSerializable(ARG_PODCAST);
        podcasts = Podcasts.getInstance();

        Intent intent = new Intent(getActivity(), BackgroundAudioService.class);
        connection = new AudioServiceConnection();
        getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ImageView podcastCover = (ImageView) view.findViewById(R.id.player_cover_image);
        Glide.with(getActivity()).load(podcast.getImageUrl()).into(podcastCover);

        TextView title = (TextView) view.findViewById(R.id.player_podcast_title);
        title.setText(podcast.getTitle());

        TextView authors = (TextView) view.findViewById(R.id.player_podcast_authors);
        authors.setText(podcast.getAuthors());

        TextView description = (TextView) view.findViewById(R.id.player_podcast_description);
        description.setText(podcast.getDescription());

        playbackTime = (TextView) view.findViewById(R.id.player_playback_time);

        seekBar = (SeekBar) view.findViewById(R.id.player_seekbar);
        seekBar.setOnSeekBarChangeListener(this);

        view.findViewById(R.id.player_btn_next).setOnClickListener(this);
        view.findViewById(R.id.player_btn_prev).setOnClickListener(this);
        view.findViewById(R.id.player_btn_cache).setOnClickListener(this);
        view.findViewById(R.id.player_btn_stop).setOnClickListener(this);

        playButton = (ImageView) view.findViewById(R.id.player_btn_play_pause);
        playButton.setOnClickListener(this);

        Player player = Player.getInstance();
        playerStateSubscription = player.onPlayerUpdates()
                .subscribe(playerState -> {
                    Podcast currentPodcast = playerState.getCurrentPodcast();
                    boolean isCurrentPodcastPageOpened = podcast.equals(currentPodcast);
                    if (isCurrentPodcastPageOpened || currentPodcast == null) {
                        showPlayerState(playerState);
                    }
                    seekBar.setEnabled(isCurrentPodcastPageOpened);
                });
        playbackProgressSubscription = player.onPlaybackProgress()
                .subscribe(this::showPlaybackProgress);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        playerStateSubscription.unsubscribe();
        playbackProgressSubscription.unsubscribe();

        getActivity().unbindService(connection);
        Intent intent = new Intent(getActivity(), BackgroundAudioService.class);
        intent.setAction(BackgroundAudioService.ACTION_STOP_IF_IDLE);
        getActivity().startService(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.player_btn_next:
                switchToNextPodcast();
                break;
            case R.id.player_btn_prev:
                switchToPreviousPodcast();
                break;
            case R.id.player_btn_play_pause:
                togglePlayback();
                break;
            case R.id.player_btn_cache:
                cachePodcast();
                break;
            case R.id.player_btn_stop:
                stopPlayback();
                break;
        }
    }

    private void switchToNextPodcast() {
        Podcast next = podcasts.next(podcast);
        navigationManager.openPodcastInPlayer(next);
    }

    private void switchToPreviousPodcast() {
        Podcast previous = podcasts.previous(podcast);
        navigationManager.openPodcastInPlayer(previous);
    }

    private void togglePlayback() {
        if (audioService != null) {
            audioService.startOrPauseIfPlaying(podcast);
        }
    }

    private void cachePodcast() {
        Permissions.doIfPermitted(
                () -> Cache.cacheIfNotCachedYet(getActivity(), podcast),
                Permissions.externalStoragePermissions());
    }

    private void stopPlayback() {
        if (audioService != null) {
            audioService.stopPlayback();
        }
    }

    private void showPlayerState(PlayerState state) {
        if (!podcast.equals(state.getCurrentPodcast()) || state.isPaused()) {
            playButton.setImageResource(R.drawable.ic_play_arrow_white_48dp);
        } else {
            playButton.setImageResource(R.drawable.ic_pause_white_48dp);
        }
        seekBar.setEnabled(!state.isPaused());
    }

    private void showPlaybackProgress(PlaybackState state) {
        if (podcast.equals(state.getCurrentPodcast())) {
            playbackTime.setText(Utils.toTime(state));
            seekBar.setMax(state.getPlaybackDuration());
            seekBar.setProgress(state.getPlaybackPosition());
        } else {
            playbackTime.setText(Utils.toTime(PlaybackState.NO_PLAYBACK));
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (audioService != null && fromUser) {
            audioService.seekTo(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private class AudioServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder absBinder) {
            BackgroundAudioService.LocalBinder binder = (BackgroundAudioService.LocalBinder) absBinder;
            audioService = binder.getService();
            //We want service to live longer than this connection
            getActivity().startService(new Intent(getActivity(), BackgroundAudioService.class));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
}
