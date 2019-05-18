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
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    Observable<List<AudioModel>> audioListObservable;
    private Disposable mAudioListSubscriber;
    private MenuItem mMenuItem;
    private FragmentListener fragmentListener;

    public DevicesTracksFragment() {
    }


    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);
        Log.d("myLog", "DevicesTracksFragment -> onAttach");
        //mAudioList = new StorageAudioFiles(ctx).getStorageAudios(null, null);
        mAudioListSubscriber = new StorageAudioFiles(ctx).getAudiosAsync(null, null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(audioList -> {
                            mAudioList = audioList;
                            mTrackListRecyclerView.setAdapter(new DevicesTracksAdapter(getActivity(), audioList));
                        },
                        err -> err.printStackTrace());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("myLog", "DevicesTracksFragment -> onDetach");
        fragmentListener = null;
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

        View view = inflater.inflate(R.layout.fragment_devices_track, container, false);
        ButterKnife.bind(this, view);

        mTrackListRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mTrackListRecyclerView.setLayoutManager(linearLayoutManager);
        return view;
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_bar_menu, menu);
        menu.findItem(R.id.action_add).setVisible(false);
        mMenuItem = menu.findItem(R.id.action_shuffle).setVisible(true);
        mMenuItem.setOnMenuItemClickListener(item -> {
            //Log.d("myLog", "DeviceTrackFragment->onCreateOptionsMenu->setOnMenuItemClickListener");
            fragmentListener.onAddQueueItems(mAudioList, true);
            return true;
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fragmentListener = (FragmentListener) getActivity();
    }

}
