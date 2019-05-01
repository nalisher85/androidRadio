package com.startandroid.admin.myaudioplayer.ui;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.data.AudioModel;
import com.startandroid.admin.myaudioplayer.data.StorageAudioFiles;

import java.util.List;

public class DevicesTracksFragment extends Fragment {

    @BindView(R.id.track_list_recyclerview)
    RecyclerView mTrackListRecyclerView;
    private List<AudioModel> mAudioList;

    public DevicesTracksFragment() {
    }


    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);
        Log.d("myLog", "DevicesTracksFragment -> onAttach");
        mAudioList = new StorageAudioFiles(ctx).getStorageAudio();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("myLog", "DevicesTracksFragment -> onDetach");

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("myLog", "DevicesTracksFragment -> onCreate");
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("myLog", "DevicesTracksFragment -> onCreateView");

        View view = inflater.inflate(R.layout.fragment_devices_track, container, false);
        ButterKnife.bind(this, view);


        mTrackListRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mTrackListRecyclerView.setLayoutManager(linearLayoutManager);
        mTrackListRecyclerView.setAdapter(new DevicesTracksAdapter(getActivity(), mAudioList));
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_bar_menu, menu);
        menu.findItem(R.id.action_add).setVisible(false);
        menu.findItem(R.id.action_shuffle).setVisible(true);
    }
}
