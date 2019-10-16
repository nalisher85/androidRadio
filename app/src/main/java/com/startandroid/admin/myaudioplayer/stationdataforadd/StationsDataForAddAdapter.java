package com.startandroid.admin.myaudioplayer.stationdataforadd;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.data.model.RadioStation;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

public class StationsDataForAddAdapter extends RecyclerView.Adapter<StationsDataForAddAdapter.StationViewHolder> {

    private List<StationWrapper> mStations;
    private OnItemViewCheckedListener onItemViewCheckedListener;

    StationsDataForAddAdapter(OnItemViewCheckedListener listener) {
        mStations = new ArrayList<>();
        onItemViewCheckedListener = listener;
    }

    void updateData(List<RadioStation> data){
        mStations.clear();
        for (RadioStation station : data) {
            mStations.add(new StationWrapper(station, false));
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.station_item_for_add, parent, false);
        return new StationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StationViewHolder holder, int position) {
        holder.bind(mStations.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mStations.size();
    }

    void setIsViewsChecked(boolean isViewsChecked) {
        for (StationWrapper station : mStations){
            station.setChecked(isViewsChecked);
        }
    }

    //-------------------------------------------------------------

    interface OnItemViewCheckedListener {
        void onItemViewChecked(RadioStation item, boolean isChecked);
    }

    class StationViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.station_for_add_title)
        TextView mTitle;
        @BindView(R.id.station_for_add_country)
        TextView mCountry;
        @BindView(R.id.station_for_add_language)
        TextView mLanguage;
        @BindView(R.id.station_for_add_chbx)
        CheckBox mCheckItem;

        private StationWrapper mStation;

        StationViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind (final StationWrapper station, int position) {
            mStation = station;
            mTitle.setText(station.getStation().getStationName());
            mCountry.setText(station.getStation().getCountry());
            mLanguage.setText(station.getStation().getLanguage());
            mCheckItem.setChecked(station.isChecked);
        }

        @OnCheckedChanged(R.id.station_for_add_chbx)
        void onCheckedChanged(CheckBox buttonView, boolean isChecked) {
            mStation.setChecked(isChecked);
            onItemViewCheckedListener.onItemViewChecked(mStation.getStation(), isChecked);
        }
    }

    class StationWrapper {

        RadioStation station;
        boolean isChecked;

        public StationWrapper() {
        }

        StationWrapper(RadioStation station, boolean isChecked) {
            this.station = station;
            this.isChecked = isChecked;
        }

        RadioStation getStation() {
            return station;
        }

        public void setStation(RadioStation station) {
            this.station = station;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }
    }
}
