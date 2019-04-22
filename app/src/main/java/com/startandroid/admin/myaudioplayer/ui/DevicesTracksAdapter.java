package com.startandroid.admin.myaudioplayer.ui;

import android.content.Context;
import android.media.AudioFocusRequest;
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

public class DevicesTracksAdapter extends RecyclerView.Adapter<DevicesTracksAdapter.DevicesTracksViewHolder> {

    private Context mContext;
    private List<AudioModel> mTrackList;

    public DevicesTracksAdapter (Context context, List<AudioModel> trackList) {
        mContext = context;
        mTrackList = trackList;
    }


    @NonNull
    @Override
    public DevicesTracksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.device_track_item, parent, false);
        return new DevicesTracksViewHolder(view) ;
    }

    @Override
    public void onBindViewHolder(@NonNull DevicesTracksViewHolder holder, int position) {
        String singerAndSong = mTrackList.get(position).getSinger() + " - "
                + mTrackList.get(position).getSong();
        holder.mTrackName.setText(mTrackList.get(position).getName());
        holder.mSingerSong.setText(singerAndSong);
        holder.trackCardView.setOnClickListener(view ->
                Toast.makeText(mContext, "track " + position + " clicked", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return mTrackList.size();
    }

    class DevicesTracksViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.track_cardview)
        CardView trackCardView;
        @BindView(R.id.track_icon)
        ImageView mTrackIcon;
        @BindView(R.id.track_name)
        TextView mTrackName;
        @BindView(R.id.singer_song)
        TextView mSingerSong;
        @BindView(R.id.btn_track_options)
        ImageButton mBtnTrackOptions;

        public DevicesTracksViewHolder(View itemView){
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
