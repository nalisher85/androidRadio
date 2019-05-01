package com.startandroid.admin.myaudioplayer.ui;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.data.RadioStationModel;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    @BindView(R.id.station_list)
    RecyclerView mStationRecycleView;

    public FavoritesFragment() {
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_radio, container, false);
        ButterKnife.bind(this, view);
        List<RadioStationModel> stationList = new ArrayList<>();



        for (int i = 0; i <= 10; i++) {
            RadioStationModel station = new RadioStationModel();
            station.setStationName("Favorite Station " + i);
            stationList.add(station);
        }

        mStationRecycleView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mStationRecycleView.setLayoutManager(linearLayoutManager);

        mStationRecycleView.setAdapter(new StationAdapter(getActivity(), stationList));
        return view;
    }

}
