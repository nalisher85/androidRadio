package com.startandroid.admin.myaudioplayer.stationdataforadd;

import android.content.res.XmlResourceParser;

import com.startandroid.admin.myaudioplayer.data.RadioStationSource;
import com.startandroid.admin.myaudioplayer.data.model.RadioStation;
import com.startandroid.admin.myaudioplayer.util.XmlData;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.disposables.CompositeDisposable;


public class StationForAddPresenter implements StationsDataForAddContract.Presenter {

    private RadioStationSource mLocalRepository;
    private RadioStationSource mRemoteRepository;
    private StationsDataForAddContract.View mView;

    private List<RadioStation> mStationsForShow;
    private List<RadioStation> mSelectedStations;
    private ArrayList<String> mCountries;
    private ArrayList<String> mLanguages;
    private String mFilterWithCountry = "";
    private String mFilterWithLanguage = "";
    private CompositeDisposable mDisposable = new CompositeDisposable();

    public StationForAddPresenter(RadioStationSource localRepository,
                                  RadioStationSource remoteRepasitory,
                                  StationsDataForAddContract.View view) {
        mLocalRepository = localRepository;
        mRemoteRepository = remoteRepasitory;
        mView = view;
        mSelectedStations = new ArrayList<>();
    }

    @Override
    public void start() {
        mView.showMessage("Заргузка...");
        mDisposable.add(
                mRemoteRepository.getAllRadioStation().subscribe(
                        remoteStations -> {

                            if (remoteStations.isEmpty())
                                mView.showMessage("Нет данных.");
                            mDisposable.add(
                                    mLocalRepository.getAllRadioStation().subscribe(
                                            localStations -> {

                                                mStationsForShow = filterRemoteData(remoteStations, localStations);
                                                mView.showStations(mStationsForShow);
                                                setCountriesAndLanguages(mStationsForShow);

                                                if (mStationsForShow.isEmpty())
                                                    mView.showMessage("Нет новых станции.");
                                            },
                                            err -> {
                                                mView.showMessage(err.getMessage());
                                                err.printStackTrace();
                                            }
                                    )
                            );
                        },
                        err -> {
                            mView.showMessage(err.getMessage());
                            err.printStackTrace();
                        }
                )
        );
    }

    private List<RadioStation> filterRemoteData(List<RadioStation> remoteData, List<RadioStation> localData){

        List<RadioStation> forRemove = new ArrayList<>();

        for (RadioStation remoteStation : remoteData) {

            for (RadioStation localStation : localData) {
                if (remoteStation.getPath().equals(localStation.getPath())) {
                    forRemove.add(remoteStation);
                }
            }

        }

        remoteData.removeAll(forRemove);

        return remoteData;
    }


    @Override
    public void addSelectedStationsToLocalDb() {

        for (RadioStation station : mSelectedStations) {

            mDisposable.add(
                    mLocalRepository.getRadioStationByLink(station.getPath())
                            .subscribe(respStation -> {
                                        respStation.setStationName(station.getStationName());
                                        respStation.setPath(station.getPath());
                                        mLocalRepository.update(respStation).subscribe();
                                    },

                                    Throwable::printStackTrace,

                                    () -> mLocalRepository.insert(station).subscribe()
                            )
            );
        }
    }

    @Override
    public void filterStations() {

        List<RadioStation> filteredStations = new ArrayList<>();

        for (RadioStation station : mStationsForShow) {
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

            if (mSelectedStations.size() == mStationsForShow.size()) {
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
        if (mStationsForShow == null) return;
        mSelectedStations.clear();
        mSelectedStations.addAll(mStationsForShow);
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
        mDisposable.clear();
        mView = null;
    }

    private void setCountriesAndLanguages(List<RadioStation> stations){
        mCountries = new ArrayList<>();
        mLanguages = new ArrayList<>();
        Set<String> uniqCountries = new HashSet<>();
        Set<String> uniqLanguage = new HashSet<>();


        for (int i = 0; i < stations.size(); i++) {
            uniqCountries.add(stations.get(i).getCountry());
            uniqLanguage.add(stations.get(i).getLanguage());
        }

        mCountries.addAll(uniqCountries);
        mLanguages.addAll(uniqLanguage);
    }

    //----------------------------------------------------------------------

    private void loadData(){
        mStationsForShow = parseXmlToRadioStations(XmlData.getData());
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
