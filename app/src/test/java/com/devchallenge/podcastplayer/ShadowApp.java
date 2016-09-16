package com.devchallenge.podcastplayer;

import android.content.Context;

import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Created by MrDeveloper on 16.09.2016.
 */
@Implements(App.class)
public class ShadowApp {
    @Implementation
    public static Context getInstance() {
        return RuntimeEnvironment.application;
    }
}
