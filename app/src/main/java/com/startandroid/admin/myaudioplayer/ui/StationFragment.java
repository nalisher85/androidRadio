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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.data.MyDataBase;
import com.startandroid.admin.myaudioplayer.data.MyDbHelper;
import com.startandroid.admin.myaudioplayer.data.RadioStationModel;

import java.util.List;

/**
 * Use the {@link StationFragment#//newInstance} factory method to
 * create an instance of this fragment.
 */
public class StationFragment extends Fragment {


    @BindView(R.id.station_list)
    RecyclerView mStationListRecyclerView;
    private MyDbHelper mDb;
    FragmentListener fragmentListener;
    private Disposable stationsSubscription;

    public StationFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_radio, container, false);
        ButterKnife.bind(this, view);


        List<RadioStationModel> stationList;

        mStationListRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mStationListRecyclerView.setLayoutManager(linearLayoutManager);

        stationsSubscription = mDb.getRadioStationList().subscribe(
                stations -> {
            StationAdapter stationAdapter = new StationAdapter(getActivity(), stations);
            mStationListRecyclerView.setAdapter(stationAdapter);
            }
        );

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_bar_menu, menu);
        menu.findItem(R.id.action_add).setVisible(true);
        menu.findItem(R.id.action_shuffle).setVisible(false);
    }

    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to {@link Activity#onStart() Activity.onStart} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.  It is also useful for fragments that use
     * {@link #setRetainInstance(boolean)} to retain their instance,
     * as this callback tells the fragment when it is fully associated with
     * the new activity instance.  This is called after {@link #onCreateView}
     * and before {@link #onViewStateRestored(Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fragmentListener = (FragmentListener)getActivity();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDb = new MyDbHelper(context.getApplicationContext());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        stationsSubscription.dispose();
    }
}
