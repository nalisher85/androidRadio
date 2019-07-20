package com.startandroid.admin.myaudioplayer.stationdataforadd;

import android.content.res.XmlResourceParser;

import com.startandroid.admin.myaudioplayer.data.RadioStationSource;
import com.startandroid.admin.myaudioplayer.data.model.RadioStation;
import com.startandroid.admin.myaudioplayer.util.XmlData;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

public class StationForAddPresenter implements StationsDataForAddContract.Presenter {

    private RadioStationSource mRepository;
    private StationsDataForAddContract.View mView;

    private List<RadioStation> mStations;
    private List<RadioStation> mSelectedStations;
    private String[] mCountries;
    private String[] mLanguages;
    private String mFilterWithCountry = "";
    private String mFilterWithLanguage = "";
    private Disposable mDisposable;

    public StationForAddPresenter(RadioStationSource repository, StationsDataForAddContract.View view) {
        mRepository = repository;
        mView = view;
        mSelectedStations = new ArrayList<>();
    }

    @Override
    public void start() {
        loadData();

        if (mStations != null) {
            mView.showStations(mStations);
            setCountriesAndLanguages(mStations);
        }
    }

    @Override
    public void addSelectedStationsToDb() {

        for (RadioStation station : mSelectedStations) {

            mDisposable = mRepository.getRadioStationByLink(station.getPath())
                    .subscribe(respStation -> {
                                respStation.setStationName(station.getStationName());
                                respStation.setPath(station.getPath());
                                mRepository.update(respStation).subscribe();
                            },

                            Throwable::printStackTrace,

                            () -> mRepository.insert(station).subscribe()
                    );
        }
    }

    @Override
    public void filterStations() {


        List<RadioStation> filteredStations = new ArrayList<>();

        for (RadioStation station : mStations) {
            boolean isEquals = (mFilterWithLanguage.isEmpty() || mFilterWithLanguage.equals(station.getLanguage()))
                    && (mFilterWithCountry.isEmpty() || mFilterWithCountry.equals(station.getCountry()));
            if (isEquals) filteredStations.add(station);
        }

        mView.showStations(filteredStations);
    }

    @Override
    public void filterWithLanguage() {
        mView.showLanguageFilterDialog(mLanguages);
    }

    @Override
    public void filterWithCountry() {
        mView.showCountryFilterDialog(mCountries);
    }

    @Override
    public void setLanguageFilter(String language) {
        mFilterWithLanguage = language;
    }

    @Override
    public void setCountryFilter(String country) {
        mFilterWithCountry = country;
    }

    @Override
    public void addToSelectedStation(RadioStation item) {
        if (!mSelectedStations.contains(item)){
            mSelectedStations.add(item);
            mView.showAddStationsBtn();

            if (mSelectedStations.size() == mStations.size()) {
                mView.setCheckAllChbx(true);
            }
        }
    }

    @Override
    public void removeFromSelectedStation(RadioStation item) {
        mSelectedStations.remove(item);
        mView.setCheckAllChbx(false);

        if (mSelectedStations.isEmpty()) {
            mView.hideAddStationsBtn();
        }

    }

    @Override
    public void setAllStationsSelected() {
        mSelectedStations.clear();
        mSelectedStations.addAll(mStations);
        mView.showAddStationsBtn();
        mView.setCheckAllChbx(true);
    }

    @Override
    public void clearSelectedStations() {
        mView.hideAddStationsBtn();
        mView.setCheckAllChbx(false);
        mSelectedStations.clear();
    }

    @Override
    public void onDestroy() {
        mDisposable.dispose();
        mView = null;
    }

    private void loadData(){
        mStations = parseXmlToRadioStations(XmlData.getData());
    }

    private void setCountriesAndLanguages(List<RadioStation> stations){
        mCountries = new String[stations.size()];
        mLanguages = new String[stations.size()];

        for (int i = 0; i < stations.size(); i++) {
            mCountries[i] = stations.get(i).getCountry();
            mLanguages[i] = stations.get(i).getLanguage();
        }
    }

    private List<RadioStation> parseXmlToRadioStations(XmlPullParser parser) {
        List<RadioStation> stations = new ArrayList<>();
        RadioStation station;
        try {
            while (parser.getEventType() != XmlPullParser.END_DOCUMENT){

                if (parser.getEventType() == XmlPullParser.START_TAG
                        && parser.getName().equals("radiostation")) {

                    station = new RadioStation();
                    parser.next();
                    while (parser.getEventType() != XmlPullParser.END_TAG
                            && !parser.getName().equals("radiostation")) {
                        switch (parser.getName()) {
                            case "name":
                                station.setStationName(parser.nextText());
                                break;
                            case "path":
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
            ((XmlResourceParser) parser).close();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stations;
    }
}
