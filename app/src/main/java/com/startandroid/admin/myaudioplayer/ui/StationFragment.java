package com.startandroid.admin.myaudioplayer.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.data.MyDataBase;
import com.startandroid.admin.myaudioplayer.data.MyDbHelper;
import com.startandroid.admin.myaudioplayer.data.RadioStationModel;

import java.util.List;

public class StationFragment extends Fragment implements StationAdapter.OnItemViewClickListener {


    @BindView(R.id.station_list)
    RecyclerView mStationListRecyclerView;

    private MyDbHelper mDb;
    private FragmentListener fragmentListener;
    private boolean mIsFavoriteFragment;
    private Disposable stationsSubscription;

    public StationFragment() {
        super();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        assert getArguments() != null;
        mIsFavoriteFragment = getArguments().getBoolean(MainActivity.IS_FAVORITE_FRAGMENT_KEY);


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_radio, container, false);
        ButterKnife.bind(this, view);


        mStationListRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mStationListRecyclerView.setLayoutManager(linearLayoutManager);

        if (mIsFavoriteFragment) {
            stationsSubscription = mDb.getStationsByFavoriteField(true).subscribe(
                    stations ->
                            mStationListRecyclerView.setAdapter(new StationAdapter(stations, this)));
        } else {
            stationsSubscription = mDb.getRadioStationList().subscribe(
                    stations ->
                            mStationListRecyclerView.setAdapter(new StationAdapter(stations, this)));
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_bar_menu, menu);
        menu.findItem(R.id.action_add).setVisible(true);
        menu.findItem(R.id.action_shuffle).setVisible(false);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

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

    @SuppressLint("CheckResult")
    private void updateStationData(RadioStationModel station){
        mDb.update(station).subscribe(
                () -> {},
                throwable -> {
                    Toast.makeText(getContext(), "Ошибка обновления!", Toast.LENGTH_SHORT).show();
                    throwable.printStackTrace();
                });
    }

    @SuppressLint("CheckResult")
    private void deleteStationData(RadioStationModel station) {
        mDb.delete(station).subscribe(
                () -> {},
                throwable -> {
                    Toast.makeText(getContext(), "Ошибка обновления!", Toast.LENGTH_SHORT).show();
                    throwable.printStackTrace();
                });
    }

    private void showDeleteStationDialog(RadioStationModel station){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.delete_station_dialog_title)
                .setIcon(R.drawable.ic_delete)
                .setMessage(R.string.delete_station_dialog_msg)
                .setPositiveButton(R.string.yes_btn,
                        ((dialog, which) -> {
                            deleteStationData(station);
                            dialog.cancel();
                        }))
                .setNegativeButton(R.string.cancel_btn,
                        (dialog, which) -> dialog.cancel())
                .show();
    }

    @Override
    public void onItemClickListener(RadioStationModel itemData, int viewId) {
        switch (viewId) {
            case R.id.station_cardview:
                fragmentListener.onAddQueueItem(itemData);
                break;
            case R.id.btn_favorite:
                updateStationData(itemData);
                break;
            case R.id.station_edit_menu:
                Intent intent = new Intent(getContext(), AddEditStationActivity.class);
                intent.putExtra(AddEditStationActivity.ACTIVITY_MODE_KEY, AddEditStationActivity.EDIT_MODE);
                intent.putExtra(AddEditStationActivity.STATION_KEY, itemData);
                startActivity(intent);
                break;
            case R.id.station_delete_menu:
                showDeleteStationDialog(itemData);
                break;
        }
    }
}
