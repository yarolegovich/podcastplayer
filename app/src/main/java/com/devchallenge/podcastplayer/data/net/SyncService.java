package com.devchallenge.podcastplayer.data.net;

import android.content.Context;
import android.util.Log;

import com.devchallenge.podcastplayer.data.Cache;
import com.devchallenge.podcastplayer.data.model.Podcast;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.TaskParams;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by MrDeveloper on 14.09.2016.
 */
public class SyncService extends GcmTaskService {

    private static final String LOG_TAG = SyncService.class.getSimpleName();

    @Override
    public int onRunTask(TaskParams taskParams) {
        Cache cache = new Cache(this);
        if (!cache.isCacheValid()) {
            cache.clearCache();
            try {
                List<Podcast> podcasts = new NetworkManager().getRss()
                        .toBlocking()
                        .first();
                cache.savePodcastList(podcasts);
                return GcmNetworkManager.RESULT_SUCCESS;
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                return GcmNetworkManager.RESULT_FAILURE;
            }
        } else {
            return GcmNetworkManager.RESULT_RESCHEDULE;
        }
    }

    public static void scheduleDataSync(Context context) {
        PeriodicTask updateWeatherTask = new PeriodicTask.Builder()
                .setRequiredNetwork(PeriodicTask.NETWORK_STATE_CONNECTED)
                .setPeriod(TimeUnit.HOURS.toMillis(6))
                .setService(SyncService.class)
                .setRequiresCharging(false)
                .setPersisted(true)
                .setTag(LOG_TAG)
                .build();
        GcmNetworkManager.getInstance(context).schedule(updateWeatherTask);
    }
}
