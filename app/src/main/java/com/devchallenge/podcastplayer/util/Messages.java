package com.devchallenge.podcastplayer.util;

import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

import com.devchallenge.podcastplayer.R;

/**
 * Created by yarolegovich on 14.09.2016.
 */
public class Messages {

    public static void showAlreadyInCacheMessage(Context context) {
        showText(context, R.string.msg_podcast_already_cached);
    }

    public static void showNoInternetMessage(Context context) {
        showText(context, R.string.no_internet);
    }

    private static void showText(Context context, @StringRes int text) {
        Toast.makeText(context,
                text, Toast.LENGTH_SHORT)
                .show();
    }
}
