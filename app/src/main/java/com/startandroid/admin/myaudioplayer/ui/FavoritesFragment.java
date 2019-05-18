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
import io.reactivex.disposables.Disposable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.data.MyDataBase;
import com.startandroid.admin.myaudioplayer.data.MyDbHelper;

public class FavoritesFragment extends Fragment {

    @BindView(R.id.station_list)
    RecyclerView mStationRecycleView;

    private MyDbHelper mDb;
    private FragmentListener mFragmentListner;
    private Disposable stationsSubscriber;

    public FavoritesFragment() {
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_radio, container, false);
        ButterKnife.bind(this, view);

        mStationRecycleView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mStationRecycleView.setLayoutManager(linearLayoutManager);

        stationsSubscriber = mDb.getRadioStationsByField("is_favorite", "true")
                .subscribe(stations ->
                        mStationRecycleView.setAdapter(new StationAdapter(getActivity(), stations)),
                        err -> err.printStackTrace());
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentListener fragmentListener = (FragmentListener) getActivity();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDb = new MyDbHelper(context.getApplicationContext());
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
