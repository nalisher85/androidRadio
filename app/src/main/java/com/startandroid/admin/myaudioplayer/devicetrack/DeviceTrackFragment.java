package com.startandroid.admin.myaudioplayer.devicetrack;


import android.Manifest;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.startandroid.admin.myaudioplayer.MyApplication;
import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.client.IMediaBrowser;
import com.startandroid.admin.myaudioplayer.client.MediaBrowserHelper;
import com.startandroid.admin.myaudioplayer.data.MusicDataSource;
import com.startandroid.admin.myaudioplayer.data.model.Audio;
import com.startandroid.admin.myaudioplayer.data.storageaudiosource.MusicStorageDataSource;
import com.startandroid.admin.myaudioplayer.service.MediaService;
import com.startandroid.admin.myaudioplayer.util.RequestPermission;

import java.util.List;

public class DeviceTrackFragment extends Fragment implements DeviceTrackContract.View {

    private static final int READ_WRITE_PERMISSION_REQUEST_CODE = 1;
    private static final int WRITE_SETTINGS_PERMISSION_REQUEST_CODE = 2;

    @BindView(R.id.track_list_recyclerview)
    RecyclerView mTrackListRecyclerView;

    private DeviceTrackContract.Presenter mPresenter;
    private DeviceTrackContract.PermissionCallBack mPermissionCallBack;

    public DeviceTrackFragment() {

        MusicDataSource musicDataSource = MusicStorageDataSource.getInstance();
        //IMediaBrowser mediaBrowser = MediaBrowserHelper.getInstance(MediaService.class);
        IMediaBrowser mediaBrowser = new MediaBrowserHelper(MediaService.class);
        mPresenter = new DeviceTrackPresenter(musicDataSource, mediaBrowser, this);
    }

    public static DeviceTrackFragment newInstance(){
        return new DeviceTrackFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_devices_track, container, false);
        ButterKnife.bind(this, view);

        //set RecyclerView
        mTrackListRecyclerView.addItemDecoration(new DividerItemDecoration(mTrackListRecyclerView.getContext(),
                LinearLayoutManager.VERTICAL));
        mTrackListRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mTrackListRecyclerView.setLayoutManager(linearLayoutManager);
        mTrackListRecyclerView.setAdapter(
                new DeviceTrackAdapter(new TrackItemClickListener()));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("myLog", "DeviceTrackFragment->onStart");
        mPresenter.start();
        mPresenter.connectMediaBrowser();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("myLog", "DeviceTrackFragment->onStop");
        mPresenter.disconnectMediaBrowser();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_bar_menu, menu);
        menu.findItem(R.id.action_add).setVisible(false);
        menu.findItem(R.id.action_shuffle).setVisible(true)
                .setOnMenuItemClickListener(item -> {
                    mPresenter.addAllToQueue(true);
                    return true;
                });
    }

    @Override
    public void showMusicList(List<Audio> musicList) {

        if (musicList != null) {
            DeviceTrackAdapter adapter = (DeviceTrackAdapter) mTrackListRecyclerView.getAdapter();

            if (adapter != null) {
                adapter.updateData(musicList);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void updateMusicList(List<Audio> list) {
        if (mTrackListRecyclerView != null && mTrackListRecyclerView.getAdapter() != null){
            DeviceTrackAdapter adapter = (DeviceTrackAdapter)mTrackListRecyclerView.getAdapter();
            adapter.updateData(list);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setAudioTrackAsRingtone(@NonNull Audio audioTrack){
        String path = audioTrack.getPath();
        Uri uriAbs = new Uri.Builder().scheme("file").path(path).build(); //Absolute uri
        RingtoneManager.setActualDefaultRingtoneUri(getContext(), RingtoneManager.TYPE_RINGTONE, uriAbs);
    }

    private void showDialogDeleteMusic(@NonNull String musicId){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.delete_audio_dialog_title)
                .setIcon(R.drawable.ic_delete)
                .setMessage(R.string.delete_audio_dialog_message)
                .setNegativeButton("Нет",
                        (dialog, which) -> dialog.cancel())
                .setPositiveButton("Да" ,
                        ((dialog, which) -> {
                            mPresenter.deleteMusic(musicId);
                            dialog.cancel();
                        })).show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WRITE_SETTINGS_PERMISSION_REQUEST_CODE) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && Settings.System.canWrite(MyApplication.getContext())) {

                mPermissionCallBack.WriteSettingsPermission(true);

            } else {
                mPermissionCallBack.WriteSettingsPermission(false);
                //show dialog
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_WRITE_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPermissionCallBack.onExternalStoragePermission(true);
                } else {
                    mPermissionCallBack.onExternalStoragePermission(false);
                }
                break;
            }
            case WRITE_SETTINGS_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPermissionCallBack.WriteSettingsPermission(true);
                } else {
                    mPermissionCallBack.WriteSettingsPermission(false);
                }
            }
        }
    }


    @Override
    public boolean checkWriteSettingsPermission () {

        Context ctx = MyApplication.getContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.System.canWrite(ctx);
        } else {
            return ActivityCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_SETTINGS)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    @Override
    public void requestWriteSettingsPermission() {
        RequestPermission.requestWriteSettingsPermission(
                this,
                WRITE_SETTINGS_PERMISSION_REQUEST_CODE
        );
    }

    /*
     * TODO:  Проверить работоспособность checkExternalStoragePermission пункт
     * RequestPermission.requestExternalStoragePermission(getActivity(), READ_WRITE_PERMISSION_REQUEST_CODE);
    */
    @Override
    public boolean checkExternalStoragePermission() {
        if (Build.VERSION.SDK_INT < 23) return true;
        Context ctx = MyApplication.getContext();

        return ActivityCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void requestExternalStoragePermission() {

        RequestPermission.requestExternalStoragePermission(
                this,
                READ_WRITE_PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void setPermissionCallBack(DeviceTrackContract.PermissionCallBack callBack){
        mPermissionCallBack = callBack;
    }

    class TrackItemClickListener implements DeviceTrackAdapter.TrackItemClickListener {

        @Override
        public void onItemClickListener(Audio item, int viewId) {
            switch (viewId) {
                case R.id.track_item:
                    mPresenter.addQueueItem(item, true);
                    break;
                case R.id.set_as_ring_menu:
                    if (checkWriteSettingsPermission())
                        setAudioTrackAsRingtone(item);
                    else {
                        mPresenter.setRingtoneForSetAfterPermission(item);
                        requestWriteSettingsPermission();
                    }
                    break;
                case R.id.add_to_queue_menu:
                    mPresenter.addQueueItem(item, false);
                    break;
                case R.id.track_delete_menu:
                    showDialogDeleteMusic(item.getId());
                    break;
            }
        }
    }


}
