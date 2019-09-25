package com.startandroid.admin.myaudioplayer.devicetrack;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.data.model.Audio;
import com.startandroid.admin.myaudioplayer.util.EditorViewHitArea;
import com.startandroid.admin.myaudioplayer.util.TouchDelegateComposite;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceTrackAdapter extends RecyclerView.Adapter<DeviceTrackAdapter.DevicesTracksViewHolder> {

    private List<Audio> mTrackList = new ArrayList<>();
    private TrackItemClickListener mViewItemClickListener;


    public DeviceTrackAdapter(TrackItemClickListener listener) {
        mViewItemClickListener = listener;
    }


    @NonNull
    @Override
    public DevicesTracksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_track_item, parent, false);
        return new DevicesTracksViewHolder(view) ;
    }

    @Override
    public void onBindViewHolder(@NonNull DevicesTracksViewHolder holder, int position){
        holder.bind(mTrackList.get(position));
    }

    @Override
    public int getItemCount() {
        return mTrackList.size();
    }

    void updateData(List<Audio> listData){
        mTrackList.clear();
        mTrackList.addAll(listData);
    }

    interface TrackItemClickListener {
        void onItemClickListener(Audio itemData, int viewId);
    }

    class DevicesTracksViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.track_item)
        LinearLayout trackItem;
        @BindView(R.id.track_name)
        TextView mTrackName;
        @BindView(R.id.artist)
        TextView mSinger;
        @BindView(R.id.btn_track_options)
        ImageButton mBtnTrackOptions;


        DevicesTracksViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            TouchDelegateComposite trackItemTouchDelegate = new TouchDelegateComposite(itemView.getContext());
            EditorViewHitArea.increaseViewHitAreaPost(mBtnTrackOptions, trackItemTouchDelegate,
                    13, 5, 13, 10);
        }

        void bind(final Audio itemData) {
            mTrackName.setText(itemData.getName());
            mSinger.setText(itemData.getArtist());

            trackItem.setOnClickListener((view) ->
                    mViewItemClickListener.onItemClickListener(itemData, trackItem.getId()));

            mBtnTrackOptions.setOnClickListener(v -> {
                PopupMenu menu = new PopupMenu(trackItem.getContext(), v);
                menu.inflate(R.menu.audio_track_menu);
                menu.setOnMenuItemClickListener(menuItem -> {
                    mViewItemClickListener.onItemClickListener(itemData, menuItem.getItemId());
                    return true;
                });
                menu.show();
            });
        }
    }
}
