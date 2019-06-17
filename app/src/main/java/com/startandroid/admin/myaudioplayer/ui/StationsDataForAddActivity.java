package com.startandroid.admin.myaudioplayer.ui;

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
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.data.MyDbHelper;
import com.startandroid.admin.myaudioplayer.data.RadioStationModel;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

public class StationsDataForAddActivity extends AppCompatActivity implements StationsDataForAddAdapter.OnItemViewCheckedListener {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.language_filter)
    TextView mLanguageFilter;
    @BindView(R.id.country_filter)
    TextView mCountryFilter;
    @BindView(R.id.language_filter_btn)
    ImageButton mLanguageFilterBtn;
    @BindView(R.id.country_filter_btn)
    ImageButton mCountryFilterBtn;
    @BindView(R.id.station_list)
    RecyclerView mStationListRecyclerView;
    @BindView(R.id.check_all_chbx)
    CheckBox mCheckAllChbxBtn;

    private List<RadioStationModel> mStations;
    private List<RadioStationModel> mSelectedStations;
    private String[] mCountries;
    private String[] mLanguages;
    private String mFilterWithCountry = "";
    private String mFilterWithLanguage = "";
    private MyDbHelper myDbHelper;
    private MenuItem addStationsOptionsMenuItem;
    private StationsDataForAddAdapter adapter;


    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stations_data_for_add);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        myDbHelper = new MyDbHelper(this.getApplication());
        mSelectedStations = new ArrayList<>();
        mStations = parseXmlToRadioStations();
        setCountriesAndLanguages(mStations);
        setStationsRecyclerView(mStations);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_selected_menu, menu);
        addStationsOptionsMenuItem = menu.findItem(R.id.add_selected).setOnMenuItemClickListener(item -> {
            if (mSelectedStations != null) addStationsToDb(mSelectedStations);
            return true;
        });
        return true;
    }

    @SuppressLint("CheckResult")
    private void addStationsToDb (@NonNull List<RadioStationModel> stations) {
        for (RadioStationModel station : stations) {

            myDbHelper.getRadioStationByLink(station.getPath()).subscribe(
                    respStation -> {

                        station.setId(respStation.getId());
                        myDbHelper.update(station).subscribe(
                                () -> {},
                                e -> {
                                    String errMsg = "Error to call myDbHelper.update in line 99 in StationsDataForAddActivity.class";
                                    Log.e("myLog", errMsg);
                                    e.printStackTrace();
                                });
                    },

                    e -> {
                        String errMsg = "Error to call getRadioStationByLink in line 95 in StationsDataForAddActivity.class";
                        Log.e("myLog", errMsg);
                        e.printStackTrace();
                    },

                    () -> myDbHelper.insert(station).subscribe(
                            () -> {},
                            e -> {
                                String errMsg = "Error to call myDbHelper.insert in line 104 in StationsDataForAddActivity.class";
                                Log.e("myLog", errMsg);
                                e.printStackTrace();
                            })
            );
        }
        finish();
    }

    private void setStationsRecyclerView(List<RadioStationModel> stations){
        if (mStationListRecyclerView == null) return;
        mStationListRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mStationListRecyclerView.setHasFixedSize(true);
        mStationListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StationsDataForAddAdapter(stations, this);
        mStationListRecyclerView.setAdapter(adapter);
    }

    private List<RadioStationModel> filterStations (List<RadioStationModel> stations) {
        if(mFilterWithCountry.isEmpty() && mFilterWithLanguage.isEmpty()) return stations;
        List<RadioStationModel> filteredStations = new ArrayList<>();

        for (RadioStationModel station : stations) {
            boolean isEquals = (mFilterWithLanguage.isEmpty() || mFilterWithLanguage.equals(station.getLanguage()))
                    && (mFilterWithCountry.isEmpty() || mFilterWithCountry.equals(station.getCountry()));
            if (isEquals) filteredStations.add(station);
        }

        return filteredStations;
    }

    @OnClick({R.id.country_filter, R.id.language_filter, R.id.country_filter_btn, R.id.language_filter_btn})
    void onFilterFieldsClick(View v){
        switch (v.getId()){

            case R.id.country_filter:
                filterWithCountry(mCountries);
                break;
            case R.id.language_filter:
                filterWithLanguage(mLanguages);
                break;
            case R.id.country_filter_btn:
                if(mCountryFilterBtn.isSelected()) {
                    mCountryFilter.setText(R.string.select_country_filter_tv);
                    mFilterWithCountry = "";
                    mCountryFilterBtn.setSelected(false);
                    setStationsRecyclerView(filterStations(mStations));
                } else {
                    filterWithCountry(mCountries);
                }
                break;
            case R.id.language_filter_btn:
                if (mLanguageFilterBtn.isSelected()){
                    mLanguageFilter.setText(R.string.select_language_filter_tv);
                    mFilterWithLanguage = "";
                    mLanguageFilterBtn.setSelected(false);
                    setStationsRecyclerView(filterStations(mStations));
                } else {
                    filterWithLanguage(mLanguages);
                }
                break;

        }
    }

    private void filterWithCountry(@NonNull String[] countries) {
        DialogInterface.OnClickListener clickListener = (dialog, which) -> {
            mFilterWithCountry = mCountries[which];
            mCountryFilter.setText(mFilterWithCountry);
            mCountryFilterBtn.setSelected(true);
            setStationsRecyclerView(filterStations(mStations));
            dialog.dismiss();
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, R.layout.spinner_item, countries);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите страну");
        builder.setSingleChoiceItems(adapter, -1, clickListener);
        builder.show();
    }

    private void filterWithLanguage(@NonNull String[] languages) {
        DialogInterface.OnClickListener clickListener = (dialog, which) -> {
            mFilterWithLanguage = mLanguages[which];
            mLanguageFilter.setText(mFilterWithLanguage);
            mLanguageFilterBtn.setSelected(true);
            setStationsRecyclerView(filterStations(mStations));
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
    public void onItemViewChecked(RadioStationModel item, boolean isChecked) {
        if (isChecked) {
            mSelectedStations.add(item);
        } else {
            mSelectedStations.remove(item);
        }

        onSelectedStationsChanged();
    }

    @OnCheckedChanged(R.id.check_all_chbx)
    void checkAllChbx(CompoundButton buttonView, boolean isChecked){
        adapter.setIsViewsChecked(isChecked);
        adapter.notifyDataSetChanged();
        if (isChecked) mSelectedStations.addAll(mStations);
        else mSelectedStations.clear();

        onSelectedStationsChanged();
    }

    private void onSelectedStationsChanged(){
        if (mSelectedStations.isEmpty()) addStationsOptionsMenuItem.setVisible(false);
        else addStationsOptionsMenuItem.setVisible(true);
    }

    private List<RadioStationModel> parseXmlToRadioStations() {
        XmlPullParser parser = getResources().getXml(R.xml.radiostationdata);
        List<RadioStationModel> stations = new ArrayList<>();
        RadioStationModel station;
        try {
            while (parser.getEventType() != XmlPullParser.END_DOCUMENT){

                if (parser.getEventType() == XmlPullParser.START_TAG
                        && parser.getName().equals("radiostation")) {

                    station = new RadioStationModel();
                    parser.next();
                    while (parser.getEventType() != XmlPullParser.END_TAG
                            && !parser.getName().equals("radiostation")) {
                        switch (parser.getName()) {
                            case "name":
                                station.setStationName(parser.nextText());
                                break;
                            case "link":
                                station.setPath(parser.nextText());
                                break;
                            case "country":
                                station.setCountry(parser.nextText());
                                break;
                            case "language":
                                station.setLanguage(parser.nextText());
                                break;
                            case "isfavorite":
                                station.setFavorite(Boolean.parseBoolean(parser.nextText()));
                                break;
                        }
                        parser.next();
                    }
                    stations.add(station);

                }
                parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stations;
    }

    private void setCountriesAndLanguages(@NonNull List<RadioStationModel> stations){
        mCountries = new String[stations.size()];
        mLanguages = new String[stations.size()];

        for (int i = 0; i < stations.size(); i++) {
            mCountries[i] = stations.get(i).getCountry();
            mLanguages[i] = stations.get(i).getLanguage();
        }
    }


    //----------------------------------------------------------
    private Single<String> requestDataAsync() {
        return Single.fromCallable(this::requestData).subscribeOn(Schedulers.io());
    }

    private String requestData () {
        String strUri = getResources().getString(R.string.stations_data_link);

        URL url = null;
        try {
            url = new URL(strUri);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        HttpsURLConnection conn ;
        InputStream inputStream;
        BufferedReader buffReader;
        StringBuilder strBuilder = null;
        try {
            if (url != null) {
                conn = (HttpsURLConnection)url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setReadTimeout(3000);
                conn.setConnectTimeout(4000);
                conn.connect();
                inputStream = conn.getInputStream();
                buffReader = new BufferedReader(new InputStreamReader(inputStream));
                strBuilder = new StringBuilder();

                String line;
                while ((line = buffReader.readLine()) != null) {
                    strBuilder.append(line).append('\n');
                }
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String data = strBuilder != null ? strBuilder.toString() : null;
        return  data;
    }


}
