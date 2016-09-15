package com.devchallenge.podcastplayer.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.devchallenge.podcastplayer.R;

/**
 * Created by yarolegovich on 14.09.2016.
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
}
