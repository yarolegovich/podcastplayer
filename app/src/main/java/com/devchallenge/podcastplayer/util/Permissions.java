package com.devchallenge.podcastplayer.util;

import android.Manifest;
import android.os.Build;

import com.devchallenge.podcastplayer.data.Cache;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.EmptyMultiplePermissionsListener;

import java.util.List;

import rx.functions.Action0;

/**
 * Created by yarolegovich on 14.09.2016.
 */
public class Permissions {

    public static void doIfPermitted(Action0 action, String[] permissions) {
        Dexter.checkPermissions(new EmptyMultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                    action.call();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }, permissions);
    }

    public static String[] externalStoragePermissions() {
        return new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                readExternalStoragePermission()};
    }

    private static String readExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            return Manifest.permission.READ_EXTERNAL_STORAGE;
        } else {
            return "";
        }
    }


}
