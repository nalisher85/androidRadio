package com.startandroid.admin.myaudioplayer.ui;


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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import android.os.Looper;
import android.os.MessageQueue;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.data.AudioModel;
import com.startandroid.admin.myaudioplayer.data.StorageAudioFiles;

import java.util.List;

public class DevicesTracksFragment extends Fragment {

    private static final int READ_WRITE_PERMISSION_REQUEST_CODE = 1;
    private static final int WRITE_SETTINGS_PERMISSION_REQUEST_CODE = 2;

    @BindView(R.id.track_list_recyclerview)
    RecyclerView mTrackListRecyclerView;
    private List<AudioModel> mAudioList;
    private Disposable mAudioListSubscriber;
    private MenuItem mMenuItem;
    private FragmentListener mFragmentListener;
    private StorageAudioFiles mStorageData;

    private boolean mShowMusicListAfterPermission = false;
    private AudioModel mRingtoneForSetAfterPermission = null;

    MessageQueue messageQueue;

    public DevicesTracksFragment() {
        messageQueue = Looper.myQueue();
    }


    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);
        Log.d("myLog", "DevicesTracksFragment -> onAttach");
        mStorageData = new StorageAudioFiles(ctx);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFragmentListener = null;
        if (mAudioListSubscriber != null) mAudioListSubscriber.dispose();
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
        if(checkExternalStoragePermission(container.getContext())) showMusicList();
        else mShowMusicListAfterPermission = true;
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_bar_menu, menu);
        menu.findItem(R.id.action_add).setVisible(false);
        mMenuItem = menu.findItem(R.id.action_shuffle).setVisible(true);
        mMenuItem.setOnMenuItemClickListener(item -> {
            mFragmentListener.onAddQueueItems(mAudioList, true);
            return true;
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFragmentListener = (FragmentListener) getActivity();
    }

    private void showMusicList() {
        if (mTrackListRecyclerView != null && mStorageData != null) {
            mTrackListRecyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            mTrackListRecyclerView.setLayoutManager(linearLayoutManager);
            mAudioListSubscriber = mStorageData.getAudiosAsync(null, null)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(audioList -> {
                                mAudioList = audioList;
                                mTrackListRecyclerView.setAdapter(
                                        new DevicesTracksAdapter(audioList, new RecyclerViewItemClickListener()));
                            },
                            err -> err.printStackTrace());
        }
    }

    private void setAudioTrackAsRingtone(AudioModel audioTrack){
        String path = audioTrack.getPath();
        Uri uriAbs = new Uri.Builder().scheme("file").path(path).build(); //Absolute uri
        RingtoneManager.setActualDefaultRingtoneUri(getContext(), RingtoneManager.TYPE_RINGTONE, uriAbs);
    }

    private void deleteAudioTrackFromStorage(AudioModel audioTrack){
        mStorageData.deleteAudioById(audioTrack.getId());
    }

    private void showDeleteAudioDialog(AudioModel audio) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.delete_audio_dialog_title)
                .setIcon(R.drawable.ic_delete)
                .setMessage(R.string.delete_audio_dialog_message)
                .setNegativeButton("Нет",
                        (dialog, which) -> dialog.cancel())
                .setPositiveButton("Да" ,
                        ((dialog, which) -> {
                            deleteAudioTrackFromStorage(audio);
                            dialog.cancel();
                        })).show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WRITE_SETTINGS_PERMISSION_REQUEST_CODE
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && Settings.System.canWrite(getContext())) {
            if (mRingtoneForSetAfterPermission != null) {
                setAudioTrackAsRingtone(mRingtoneForSetAfterPermission);
            }
        } else {
            //show dialog
        }
    }

    private boolean checkWriteSettingsPermission (@NonNull Context ctx) {
        boolean granted;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            granted = Settings.System.canWrite(ctx);
        } else {
            granted = ActivityCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_SETTINGS)
                    == PackageManager.PERMISSION_GRANTED;
        }

        if (granted){
            return true;
        } else {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    builder.setTitle(R.string.set_ringtone_dialog_title)
                            .setMessage(R.string.set_ringtone_dialog_message)
                            .setPositiveButton("Ok", ((dialog, which) -> {
                                dialog.cancel();
                                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                intent.setData(Uri.parse("package:" + ctx.getPackageName()));
                                startActivityForResult(intent, WRITE_SETTINGS_PERMISSION_REQUEST_CODE);
                            }))
                            .setNegativeButton("Нет", ((dialog, which) -> {
                                Toast.makeText(getContext(), R.string.failure_to_set_ringtone, Toast.LENGTH_SHORT).show();
                            })).show();
                } else {
                    requestPermissions(new String[]{Manifest.permission.WRITE_SETTINGS},
                            WRITE_SETTINGS_PERMISSION_REQUEST_CODE);
                }
                return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_WRITE_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mShowMusicListAfterPermission) {
                        mShowMusicListAfterPermission = false;
                        showMusicList();
                    }
                } else {
                    //Show dialog
                }
                break;
            }
            case WRITE_SETTINGS_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mRingtoneForSetAfterPermission != null) {
                        setAudioTrackAsRingtone(mRingtoneForSetAfterPermission);
                    }
                } else {
                    // show dialog
                }
            }
        }
    }

    private boolean checkExternalStoragePermission(@NonNull Context ctx) {
        if (Build.VERSION.SDK_INT < 23) return true;
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //Show dialog
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        READ_WRITE_PERMISSION_REQUEST_CODE);
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        READ_WRITE_PERMISSION_REQUEST_CODE);
            }
        } else {
            return true;
        }
        return false;
    }

    class RecyclerViewItemClickListener implements DevicesTracksAdapter.OnItemViewClickListener {

        @Override
        public void onItemClickListener(AudioModel item, int viewId) {
            switch (viewId) {
                case R.id.track_cardview:
                    mFragmentListener.onAddQueueItem(item, true);
                    break;
                case R.id.set_as_ring_menu:
                    if (checkWriteSettingsPermission(mTrackListRecyclerView.getContext()))
                        setAudioTrackAsRingtone(item);
                    else mRingtoneForSetAfterPermission = item;
                    break;
                case R.id.add_to_queue_menu:
                    mFragmentListener.onAddQueueItem(item, false);
                    break;
                case R.id.track_delete_menu:
                    showDeleteAudioDialog(item);
                    break;
            }
        }
    }


}
