package com.startandroid.admin.myaudioplayer.ui;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.model.AudioModel;

import java.util.ArrayList;
import java.util.List;

public class DevicesTracksFragment extends Fragment {

    @BindView(R.id.track_list_recyclerview)
    RecyclerView mTrackListRecyclerView;

    public DevicesTracksFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_devices_track, container, false);
        ButterKnife.bind(this, view);
        List<AudioModel> trackList = new ArrayList<>();

        for (int i = 0; i <= 10; i++) {
            AudioModel audioTrack = new AudioModel("track name " + i, "singer " + i,
                    "song " + i, R.drawable.ic_audiotrack);
            trackList.add(audioTrack);
        }

        mTrackListRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mTrackListRecyclerView.setLayoutManager(linearLayoutManager);
        mTrackListRecyclerView.setAdapter(new DevicesTracksAdapter(getActivity(), trackList));
        return view;
    }

}
