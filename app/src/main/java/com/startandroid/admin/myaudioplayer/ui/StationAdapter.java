package com.startandroid.admin.myaudioplayer.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.model.AudioModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.StationViewHolder> {

    private Context mContext;
    private List<AudioModel> mStationList;

    public StationAdapter(Context ctx, List<AudioModel> stationList) {
        mContext = ctx;
        mStationList = stationList;
    }


    @NonNull
    @Override
    public StationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.radio_station_item, parent, false);
        return new StationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StationViewHolder holder, int position) {
        String singerAndSong = mStationList.get(position).getSinger() + " - "
                + mStationList.get(position).getSinger();
        String stationName = mStationList.get(position).getName();

        holder.mStationName.setText(stationName);
        holder.mSingerSong.setText(singerAndSong);

        holder.mStationCardView.setOnClickListener(view -> {
            Toast.makeText(mContext, stationName, Toast.LENGTH_SHORT).show();
        });
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
        @BindView(R.id.singer_song)
        TextView mSingerSong;
        @BindView(R.id.btn_play_pause)
        ImageButton mBtnPlayPause;
        @BindView(R.id.btn_favorite)
        ImageButton mBtnFavorite;

        StationViewHolder (View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }
}



