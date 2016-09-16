package com.devchallenge.podcastplayer.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;

import com.devchallenge.podcastplayer.App;
import com.devchallenge.podcastplayer.R;
import com.devchallenge.podcastplayer.audio.PlaybackState;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by MrDeveloper on 14.09.2016.
 */
public class Utils {

    public static boolean isLandscape() {
        return Resources.getSystem().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE;
    }

    public static boolean isPortrait() {
        return Resources.getSystem().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT;
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) (dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static boolean isTablet(Context context) {
        return context.getResources().getBoolean(R.bool.is_tablet);
    }

    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    public static boolean notEqual(Object a, Object b) {
        return !equals(a, b);
    }

    public static int[] swipeToRefreshColors(Context context) {
        return new int[]{
                ContextCompat.getColor(context, R.color.materialPrimaryRed),
                ContextCompat.getColor(context, R.color.materialPrimaryAmber),
                ContextCompat.getColor(context, R.color.materialPrimaryGreen),
                ContextCompat.getColor(context, R.color.materialPrimaryBlue)
        };
    }

    public static String millisToTime(long millis) {
        return String.format(Locale.US, "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis)
                        - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    public static String toTime(PlaybackState playbackState) {
        return App.getInstance().getString(
                R.string.time_passed_total,
                Utils.millisToTime(playbackState.getPlaybackPosition()),
                Utils.millisToTime(playbackState.getPlaybackDuration()));
    }
}
