package com.startandroid.admin.myaudioplayer.stationdataforadd;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.droidsonroids.gif.GifImageView;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.data.RadioStationSource;
import com.startandroid.admin.myaudioplayer.data.firebase.FirebaseDB;
import com.startandroid.admin.myaudioplayer.data.localsource.RadioStationLocalDataSource;
import com.startandroid.admin.myaudioplayer.data.model.RadioStation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StationsDataForAddActivity extends AppCompatActivity implements
        StationsDataForAddAdapter.OnItemViewCheckedListener, StationsDataForAddContract.View {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.language_filter_tv)
    TextView mLanguageFilter;
    @BindView(R.id.country_filter_tv)
    TextView mCountryFilter;
    @BindView(R.id.language_filter_btn)
    ImageButton mLanguageFilterBtn;
    @BindView(R.id.country_filter_btn)
    ImageButton mCountryFilterBtn;
    @BindView(R.id.station_list)
    RecyclerView mStationListRecyclerView;
    @BindView(R.id.check_all_chbx)
    CheckBox mCheckAllChbxBtn;
    @BindView(R.id.loadingGif)
    GifImageView mLoadingGif;

    private MenuItem addStationsOptionsMenuItem;
    private StationsDataForAddContract.Presenter mPresenter;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stations_data_for_add);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        setStationsRecyclerView();

        //set Presenter
        RadioStationSource localRepository = RadioStationLocalDataSource.getInstance();
        RadioStationSource remoteRepository = FirebaseDB.getInstance();
        mPresenter = new StationForAddPresenter(localRepository, remoteRepository, this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_selected_menu, menu);
        addStationsOptionsMenuItem = menu.findItem(R.id.add_selected).setOnMenuItemClickListener(item -> {
            mPresenter.addSelectedStationsToLocalDb();
            finish();
            return true;
        });
        return true;
    }

    private void setStationsRecyclerView(){
        if (mStationListRecyclerView == null) return;
        mStationListRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mStationListRecyclerView.setHasFixedSize(true);
        mStationListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mStationListRecyclerView.setAdapter(new StationsDataForAddAdapter(this));
    }

    @OnClick({R.id.country_filter_tv, R.id.language_filter_tv, R.id.country_filter_btn, R.id.language_filter_btn})
    void onFilterFieldsClick(View v){
        switch (v.getId()){

            case R.id.country_filter_tv:
                mPresenter.filterWithCountry();
                break;
            case R.id.language_filter_tv:
                mPresenter.filterWithLanguage();
                break;
            case R.id.country_filter_btn:
                if(mCountryFilterBtn.isSelected()) {
                    mCountryFilter.setText(R.string.select_country_filter_tv);
                    mCountryFilterBtn.setSelected(false);

                    setAllStationsCheckedStatus(false);
                    mPresenter.setCountryFilter("");
                    mPresenter.filterStations();
                } else {
                    mPresenter.filterWithCountry();
                }
                break;
            case R.id.language_filter_btn:
                if (mLanguageFilterBtn.isSelected()){
                    mLanguageFilter.setText(R.string.select_language_filter_tv);
                    mLanguageFilterBtn.setSelected(false);

                    setAllStationsCheckedStatus(false);
                    mPresenter.setLanguageFilter("");
                    mPresenter.filterStations();
                } else {
                    mPresenter.filterWithLanguage();
                }
                break;

        }
    }


    @Override
    public void showStations(List<RadioStation> stations) {
        if (mStationListRecyclerView.getAdapter() != null) {
            ((StationsDataForAddAdapter) mStationListRecyclerView.getAdapter()).updateData(stations);
        }
    }

    @Override
    public void showAddStationsBtn() {
        addStationsOptionsMenuItem.setVisible(true);
    }

    @Override
    public void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoadingStatus(boolean isLoading) {
        if (isLoading) {
            mStationListRecyclerView.setVisibility(View.GONE);
            mLoadingGif.setVisibility(View.VISIBLE);
        } else {
            mLoadingGif.setVisibility(View.GONE);
            mStationListRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideAddStationsBtn() {
        addStationsOptionsMenuItem.setVisible(false);
    }

    @Override
    public void showCountryFilterDialog(@NonNull List<String> countries) {
        if (countries == null || countries.isEmpty()) return;
        DialogInterface.OnClickListener clickListener = (dialog, which) -> {

            mPresenter.setCountryFilter(countries.get(which));
            mPresenter.filterStations();

            mCountryFilter.setText(countries.get(which));
            mCountryFilterBtn.setSelected(true);
            dialog.dismiss();

        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, R.layout.spinner_item, countries);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите страну");
        builder.setSingleChoiceItems(adapter, -1, clickListener);
        builder.show();
    }

    @Override
    public void showLanguageFilterDialog(@NonNull List<String> languages) {
        if (languages == null || languages.isEmpty()) return;
        DialogInterface.OnClickListener clickListener = (dialog, which) -> {

            mPresenter.setLanguageFilter(languages.get(which));
            mPresenter.filterStations();

            mLanguageFilter.setText(languages.get(which));
            mLanguageFilterBtn.setSelected(true);
            dialog.dismiss();
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, R.layout.spinner_item, languages);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите язык");
        builder.setSingleChoiceItems(adapter, -1, clickListener);
        builder.show();
    }

    @Override
    public void setCheckAllChbx(boolean checked) {
        mCheckAllChbxBtn.setChecked(checked);
    }

    @Override
    public void onItemViewChecked(RadioStation item, boolean isChecked) {

        if (isChecked){
            mPresenter.addToSelectedStation(item);
        } else {
            mPresenter.removeFromSelectedStation(item);
        }
    }

    @OnClick(R.id.check_all_chbx)
    void onClickCheckAllChbxBtn(View view){
        boolean isChecked = ((CheckBox) view).isChecked();
        setAllStationsCheckedStatus(isChecked);

        if (isChecked) mPresenter.setAllStationsSelected();
        else mPresenter.clearSelectedStations();
    }

    private void setAllStationsCheckedStatus (boolean isChecked){
        StationsDataForAddAdapter adapter = (StationsDataForAddAdapter) mStationListRecyclerView.getAdapter();

        if (adapter != null){
            adapter.setIsViewsChecked(isChecked);
            adapter.notifyDataSetChanged();
        }
    }

}
