package com.startandroid.admin.myaudioplayer.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.data.RadioStationModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.StationViewHolder> {

    private Context mContext;
    private List<RadioStationModel> mStationList;
    private FragmentListener mFragmentListener;

    public StationAdapter(Context ctx, List<RadioStationModel> stationList) {
        mContext = ctx;
        mStationList = stationList;
        mFragmentListener = (FragmentListener)ctx;
    }


    @NonNull
    @Override
    public StationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.radio_station_item, parent, false);
        return new StationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StationViewHolder holder, int position) {
        holder.mStationName.setText(mStationList.get(position).getStationName());
        holder.mStationCardView.setOnClickListener(view ->
                mFragmentListener.onAddQueueItems(mStationList.get(position)));
    }

    @Override
    public int getItemCount() {
        return mStationList.size();
    }

    class StationViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.station_cardview)
        CardView mStationCardView;
        @BindView(R.id.station_icon)
        ImageView mStationIcon;
        @BindView(R.id.station_name)
        TextView mStationName;
        @BindView(R.id.btn_station_options)
        ImageButton mBtnPlayPause;
        @BindView(R.id.btn_favorite)
        ImageButton mBtnFavorite;

        StationViewHolder (View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }
}



