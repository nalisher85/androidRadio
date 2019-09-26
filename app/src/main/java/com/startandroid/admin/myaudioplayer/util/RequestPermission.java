package com.startandroid.admin.myaudioplayer.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import com.startandroid.admin.myaudioplayer.MyApplication;
import com.startandroid.admin.myaudioplayer.R;

import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;


public class RequestPermission {


    @SuppressLint("NewApi")
    public static void requestWriteSettingsPermission(Activity activity, int requestCode){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(R.string.request_write_settings_permission_dialog_title)
                    .setMessage(R.string.request_write_settings_permission_dialog_message)
                    .setPositiveButton(R.string.ok, ((dialog, which) -> {
                        dialog.cancel();

                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        intent.setData(Uri.parse("package:" + activity.getPackageName()));
                        activity.startActivityForResult(intent, requestCode);

                    }))
                    .setNegativeButton(R.string.no, ((dialog, which) -> {
                        Toast.makeText(activity, R.string.request_denied, Toast.LENGTH_SHORT).show();
                    })).show();
        } else {
            activity.requestPermissions(new String[]{Manifest.permission.WRITE_SETTINGS},
                    requestCode);
        }
    }

    public static void requestWriteSettingsPermission(Fragment fragment, int requestCode){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(Objects.requireNonNull(fragment.getActivity()));
            builder.setTitle(R.string.request_write_settings_permission_dialog_title)
                    .setMessage(R.string.request_write_settings_permission_dialog_message)
                    .setPositiveButton(R.string.ok, ((dialog, which) -> {
                        dialog.cancel();

                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        intent.setData(Uri.parse("package:" + MyApplication.getContext().getPackageName()));
                        fragment.startActivityForResult(intent, requestCode);

                    }))
                    .setNegativeButton(R.string.no, ((dialog, which) -> {
                        Toast.makeText(
                                fragment.getActivity(),
                                R.string.request_denied,
                                Toast.LENGTH_SHORT)
                                .show();
                    })).show();
        } else {
            fragment.requestPermissions(new String[]{Manifest.permission.WRITE_SETTINGS},
                    requestCode);
        }
    }


    @SuppressLint("NewApi")
    public static void requestExternalStoragePermission(Activity activity, int requestCode){

        if (activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            //Show dialog
            activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    requestCode);
        } else {
            activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    requestCode);
        }

    }

    @SuppressLint("NewApi")
    public static void requestExternalStoragePermission(Fragment fragment, int requestCode){

        if (fragment.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            //Show dialog
            fragment.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    requestCode);
        } else {
            fragment.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    requestCode);
        }

    }

}
