package com.startandroid.admin.myaudioplayer.radiostation;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
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
import android.widget.ImageButton;

import com.startandroid.admin.myaudioplayer.MyApplication;
import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.client.IMediaBrowser;
import com.startandroid.admin.myaudioplayer.client.MediaBrowserHelper;
import com.startandroid.admin.myaudioplayer.data.RadioStationSource;
import com.startandroid.admin.myaudioplayer.data.localsource.RadioStationLocalDataSource;
import com.startandroid.admin.myaudioplayer.data.model.RadioStation;
import com.startandroid.admin.myaudioplayer.addeditstation.AddEditStationActivity;
import com.startandroid.admin.myaudioplayer.service.MediaService;
import com.startandroid.admin.myaudioplayer.stationdataforadd.StationsDataForAddActivity;

import java.util.List;

public class StationFragment extends Fragment implements StationAdapter.OnItemViewClickListener,
        RadioStationContract.View {

    public static final String IS_FAVORITE_FRAGMENT_KEY = "is_favorite_fragment_key";

    @BindView(R.id.station_list)
    RecyclerView mStationListRecyclerView;
    @BindView(R.id.add_btn)
    ImageButton mAddBtn;

    private RadioStationContract.Presenter mPresenter;

    public StationFragment() {
        super();
        RadioStationSource repository = RadioStationLocalDataSource.getInstance();
        //IMediaBrowser mediaBrowser = MediaBrowserHelper.getInstance(MediaService.class);
        IMediaBrowser mediaBrowser = new MediaBrowserHelper(MediaService.class);
        mPresenter = new RadioStationPresenter(repository, mediaBrowser, this);
    }

    public static StationFragment newInstance(boolean isFavoriteFragment) {
        StationFragment stationFragment = new StationFragment();
        Bundle bundle = new Bundle();

        bundle.putBoolean(IS_FAVORITE_FRAGMENT_KEY, isFavoriteFragment);
        stationFragment.setArguments(bundle);

        return stationFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        assert getArguments() != null;
        mPresenter.isStationFavorite(getArguments().getBoolean(IS_FAVORITE_FRAGMENT_KEY));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_radio, container, false);
        ButterKnife.bind(this, view);

        mAddBtn.setOnClickListener(v -> mPresenter.openStationDataForAdd());

        //set RecyclerView
        mStationListRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mStationListRecyclerView.setLayoutManager(linearLayoutManager);
        mStationListRecyclerView.addItemDecoration(new DividerItemDecoration(MyApplication.getContext(),
                LinearLayoutManager.VERTICAL));
        mStationListRecyclerView.setAdapter(new StationAdapter(this));
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_bar_menu, menu);

        menu.findItem(R.id.action_shuffle).setVisible(false);
        menu.findItem(R.id.action_add).setVisible(true)
                .setOnMenuItemClickListener(item -> {
                    showAddStationDialog();
                    return true;
                });
        menu.findItem(R.id.send_email).setOnMenuItemClickListener(
                item -> {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", "a.nuraliev85@gmail.com", null));
                    startActivity(Intent.createChooser(emailIntent, null));
                    return true;
                }
        );
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("myLog", "StationFragment->onStart");
        mPresenter.start();
        mPresenter.connectMediaBrowser();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("myLog", "StationFragment->onStop");
        mPresenter.disconnectMediaBrowser();
    }

    @Override
    public void showStationList(List<RadioStation> list) {

        StationAdapter adapter = (StationAdapter) mStationListRecyclerView.getAdapter();

        if (adapter != null) {
            adapter.setStationList(list);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void showAddEditStation(String stationId) {
        Intent intent = new Intent(getContext(), AddEditStationActivity.class);
        intent.putExtra(AddEditStationActivity.STATION_ID_KEY, stationId);
        startActivity(intent);
    }

    @Override
    public void showStationsDataForAdd() {
        Intent intent = new Intent(getContext(), StationsDataForAddActivity.class);
        startActivity(intent);
    }

    @Override
    public void setAddButtonVisibility(boolean isVisible) {
        if (isVisible) mAddBtn.setVisibility(View.VISIBLE);
        else mAddBtn.setVisibility(View.GONE);
    }

    private DialogInterface.OnClickListener addDialogItemClickListener = (dialog, which) -> {
        switch (which) {
           case 0:
               mPresenter.openAddEditStation(null);
               break;
           case 1:
               mPresenter.openStationDataForAdd();
               break;
       }
    };

    private void showAddStationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        String addUtl = getString(R.string.has_stream_link);
        String fromSite = getString(R.string.add_station_from_list);
        builder.setTitle("Добавить радио станцию");
        builder.setItems(new String[]{addUtl, fromSite}, addDialogItemClickListener);
        builder.show();
    }

    private void showDeleteStationDialog(RadioStation station){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.delete_station_dialog_title)
                .setIcon(R.drawable.ic_delete)
                .setMessage(R.string.delete_station_dialog_msg)
                .setPositiveButton(R.string.yes,
                        ((dialog, which) -> {
                            mPresenter.deleteStation(station);
                            dialog.cancel();
                        }))
                .setNegativeButton(R.string.cancel_btn,
                        (dialog, which) -> dialog.cancel())
                .show();
    }

    @Override
    public void onItemClicked(RadioStation itemData, int viewId) {
        switch (viewId) {
            case R.id.station_item:
                mPresenter.addToQueueItem(itemData);
                break;
            case R.id.btn_favorite:
                mPresenter.updateStation(itemData);
                break;
            case R.id.station_edit_menu:
                mPresenter.openAddEditStation("" + itemData.getId());
                break;
            case R.id.station_delete_menu:
                showDeleteStationDialog(itemData);
                break;
        }
    }
}
