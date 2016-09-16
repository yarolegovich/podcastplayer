package com.devchallenge.podcastplayer.util;

import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

import com.devchallenge.podcastplayer.R;

/**
 * Created by MrDeveloper on 14.09.2016.
 */
public class Messages {

    public static void showAlreadyInCacheMessage(Context context) {
        Toast.makeText(context,
                R.string.msg_podcast_already_cached,
                Toast.LENGTH_SHORT)
                .show();
    }

    public static void showNoInternetMessage(Context context) {
        Toast.makeText(context,
                R.string.msg_no_internet,
                Toast.LENGTH_SHORT)
                .show();
    }

    public static void showCachingInProgressMessage(Context context) {
        Toast.makeText(context,
                R.string.msg_caching_in_progress,
                Toast.LENGTH_LONG)
                .show();
    }
}
