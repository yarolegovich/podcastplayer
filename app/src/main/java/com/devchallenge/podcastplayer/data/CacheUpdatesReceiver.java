package com.devchallenge.podcastplayer.data;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by MrDeveloper on 15.09.2016.
 */
public class CacheUpdatesReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = CacheUpdatesReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, intent.toString());
        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        if (id != -1) {
            new Cache(context).markDownloadCompleted(id);
        }
    }
}
