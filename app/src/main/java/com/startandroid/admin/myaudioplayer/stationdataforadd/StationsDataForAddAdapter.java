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

    private List<RadioStation> mStations;
    private OnItemViewCheckedListener onItemViewCheckedListener;
    private boolean mIsViewsChecked = false;

    public StationsDataForAddAdapter(OnItemViewCheckedListener listener) {
        mStations = new ArrayList<>();
        onItemViewCheckedListener = listener;
    }

    public void updateData(List<RadioStation> data){
        mStations.clear();
        mStations.addAll(data);
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
        holder.bind(mStations.get(position));
        holder.mCheckItem.setChecked(mIsViewsChecked);
    }

    @Override
    public int getItemCount() {
        return mStations.size();
    }

    public void setIsViewsChecked(boolean isViewsChecked) {
        mIsViewsChecked = isViewsChecked;
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

        private RadioStation mStation;

        StationViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind (final RadioStation station) {
            mStation = station;
            mTitle.setText(station.getStationName());
            mCountry.setText(station.getCountry());
            mLanguage.setText(station.getLanguage());
        }

        @OnCheckedChanged(R.id.station_for_add_chbx)
        void onCheckedChanged(CheckBox buttonView, boolean isChecked) {
            onItemViewCheckedListener.onItemViewChecked(mStation, isChecked);
        }
    }
}
